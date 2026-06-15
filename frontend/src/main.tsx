import React from "react";
import ReactDOM from "react-dom/client";
import { Client, type IMessage } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import "./styles.css";

type UserResponse = {
  userId: number;
  username: string;
  nickname: string;
};

type ChatRoomResponse = {
  roomId: number;
  name: string;
  createdAt: string;
};

type ChatMessageResponse = {
  messageId: number;
  roomId: number;
  senderId: number;
  senderNickname: string;
  content: string;
  sentAt: string;
};

const API_BASE_URL = "http://localhost:8080";

function App() {
  const [users, setUsers] = React.useState<UserResponse[]>([]);
  const [rooms, setRooms] = React.useState<ChatRoomResponse[]>([]);
  const [messages, setMessages] = React.useState<ChatMessageResponse[]>([]);
  const [selectedUserId, setSelectedUserId] = React.useState("");
  const [selectedRoomId, setSelectedRoomId] = React.useState("");
  const [username, setUsername] = React.useState("");
  const [password, setPassword] = React.useState("1234");
  const [nickname, setNickname] = React.useState("");
  const [roomName, setRoomName] = React.useState("");
  const [messageContent, setMessageContent] = React.useState("");
  const [status, setStatus] = React.useState("disconnected");
  const [errorMessage, setErrorMessage] = React.useState("");
  const clientRef = React.useRef<Client | null>(null);

  const selectedRoom = rooms.find((room) => String(room.roomId) === selectedRoomId);
  const selectedUser = users.find((user) => String(user.userId) === selectedUserId);

  const request = React.useCallback(async <T,>(path: string, options?: RequestInit): Promise<T> => {
    const response = await fetch(`${API_BASE_URL}${path}`, {
      headers: {
        "Content-Type": "application/json",
        ...options?.headers
      },
      ...options
    });

    if (!response.ok) {
      const body = await response.text();
      throw new Error(body || `HTTP ${response.status}`);
    }

    return response.json() as Promise<T>;
  }, []);

  const refreshUsers = React.useCallback(async () => {
    const nextUsers = await request<UserResponse[]>("/api/users");
    setUsers(nextUsers);
    setSelectedUserId((current) => current || String(nextUsers[0]?.userId ?? ""));
  }, [request]);

  const refreshRooms = React.useCallback(async () => {
    const nextRooms = await request<ChatRoomResponse[]>("/api/chat-rooms");
    setRooms(nextRooms);
    setSelectedRoomId((current) => current || String(nextRooms[0]?.roomId ?? ""));
  }, [request]);

  const refreshMessages = React.useCallback(async (roomId: string) => {
    if (!roomId) {
      setMessages([]);
      return;
    }

    const recentMessages = await request<ChatMessageResponse[]>(`/api/chat-rooms/${roomId}/messages`);
    setMessages([...recentMessages].reverse());
  }, [request]);

  React.useEffect(() => {
    Promise.all([refreshUsers(), refreshRooms()]).catch((error: unknown) => {
      setErrorMessage(error instanceof Error ? error.message : "초기 데이터를 불러오지 못했습니다.");
    });
  }, [refreshRooms, refreshUsers]);

  React.useEffect(() => {
    refreshMessages(selectedRoomId).catch((error: unknown) => {
      setErrorMessage(error instanceof Error ? error.message : "메시지를 불러오지 못했습니다.");
    });
  }, [refreshMessages, selectedRoomId]);

  React.useEffect(() => {
    if (!selectedRoomId) {
      return;
    }

    const client = new Client({
      webSocketFactory: () => new SockJS(`${API_BASE_URL}/ws-chat`),
      reconnectDelay: 3000,
      onConnect: () => {
        setStatus("connected");
        client.subscribe(`/topic/rooms/${selectedRoomId}`, (message: IMessage) => {
          const nextMessage = JSON.parse(message.body) as ChatMessageResponse;
          setMessages((current) => [...current, nextMessage]);
        });
      },
      onDisconnect: () => setStatus("disconnected"),
      onStompError: (frame) => setErrorMessage(frame.headers.message ?? "STOMP 오류가 발생했습니다."),
      onWebSocketClose: () => setStatus("disconnected")
    });

    setStatus("connecting");
    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
      clientRef.current = null;
    };
  }, [selectedRoomId]);

  async function createUser(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setErrorMessage("");

    const createdUser = await request<UserResponse>("/api/users", {
      method: "POST",
      body: JSON.stringify({ username, password, nickname })
    });

    setUsers((current) => [...current, createdUser]);
    setSelectedUserId(String(createdUser.userId));
    setUsername("");
    setNickname("");
  }

  async function createRoom(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setErrorMessage("");

    const createdRoom = await request<ChatRoomResponse>("/api/chat-rooms", {
      method: "POST",
      body: JSON.stringify({ name: roomName })
    });

    setRooms((current) => [...current, createdRoom]);
    setSelectedRoomId(String(createdRoom.roomId));
    setRoomName("");
  }

  function sendMessage(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setErrorMessage("");

    if (!clientRef.current?.connected || !selectedRoomId || !selectedUserId || !messageContent.trim()) {
      return;
    }

    clientRef.current.publish({
      destination: "/app/chat.send",
      body: JSON.stringify({
        roomId: Number(selectedRoomId),
        senderId: Number(selectedUserId),
        content: messageContent.trim()
      })
    });

    setMessageContent("");
  }

  return (
    <main className="app-shell">
      <section className="workspace">
        <aside className="sidebar" aria-label="채팅 테스트 설정">
          <div className="panel">
            <div className="panel-title">
              <span>Users</span>
              <button type="button" onClick={() => void refreshUsers()}>새로고침</button>
            </div>
            <form className="stack" onSubmit={(event) => void createUser(event)}>
              <input value={username} onChange={(event) => setUsername(event.target.value)} placeholder="username" required />
              <input value={password} onChange={(event) => setPassword(event.target.value)} placeholder="password" required />
              <input value={nickname} onChange={(event) => setNickname(event.target.value)} placeholder="nickname" required />
              <button type="submit">유저 생성</button>
            </form>
            <select value={selectedUserId} onChange={(event) => setSelectedUserId(event.target.value)}>
              <option value="">유저 선택</option>
              {users.map((user) => (
                <option key={user.userId} value={user.userId}>
                  {user.nickname} ({user.username})
                </option>
              ))}
            </select>
          </div>

          <div className="panel">
            <div className="panel-title">
              <span>Rooms</span>
              <button type="button" onClick={() => void refreshRooms()}>새로고침</button>
            </div>
            <form className="stack" onSubmit={(event) => void createRoom(event)}>
              <input value={roomName} onChange={(event) => setRoomName(event.target.value)} placeholder="room name" required />
              <button type="submit">방 생성</button>
            </form>
            <div className="room-list">
              {rooms.map((room) => (
                <button
                  className={String(room.roomId) === selectedRoomId ? "room-item active" : "room-item"}
                  key={room.roomId}
                  type="button"
                  onClick={() => setSelectedRoomId(String(room.roomId))}
                >
                  <span>{room.name}</span>
                  <small>#{room.roomId}</small>
                </button>
              ))}
            </div>
          </div>
        </aside>

        <section className="chat-surface" aria-label="채팅방">
          <header className="chat-header">
            <div>
              <h1>{selectedRoom?.name ?? "채팅방을 선택하세요"}</h1>
              <p>{selectedUser ? `${selectedUser.nickname}으로 접속 중` : "메시지를 보내려면 유저를 선택하세요"}</p>
            </div>
            <span className={`status ${status}`}>{status}</span>
          </header>

          {errorMessage && <div className="error-box">{errorMessage}</div>}

          <div className="message-list">
            {messages.length === 0 ? (
              <p className="empty">아직 메시지가 없습니다.</p>
            ) : (
              messages.map((message) => (
                <article
                  key={`${message.messageId}-${message.sentAt}`}
                  className={message.senderId === Number(selectedUserId) ? "message mine" : "message"}
                >
                  <div className="message-meta">
                    <strong>{message.senderNickname}</strong>
                    <time>{formatTime(message.sentAt)}</time>
                  </div>
                  <p>{message.content}</p>
                </article>
              ))
            )}
          </div>

          <form className="composer" onSubmit={sendMessage}>
            <input
              value={messageContent}
              onChange={(event) => setMessageContent(event.target.value)}
              placeholder="메시지를 입력하세요"
              disabled={!selectedRoomId || !selectedUserId || status !== "connected"}
            />
            <button type="submit" disabled={!selectedRoomId || !selectedUserId || status !== "connected"}>
              전송
            </button>
          </form>
        </section>
      </section>
    </main>
  );
}

function formatTime(value: string) {
  return new Intl.DateTimeFormat("ko-KR", {
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit"
  }).format(new Date(value));
}

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
