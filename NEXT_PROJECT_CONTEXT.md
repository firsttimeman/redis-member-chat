# Next Project Context: Chat + Notification

## 프로젝트 목적

다음 프로젝트에서는 채팅 기능과 알림 기능을 함께 구현한다.

이번 프로젝트의 핵심 목표는 단순히 메시지를 주고받는 것을 넘어서, 실시간 서비스에서 자주 쓰이는 아래 구조를 직접 학습하는 것이다.

```text
DB = 기록 저장
WebSocket = 접속 중인 사용자에게 실시간 전달
Redis Pub/Sub = 서버 간 이벤트 전달
```

모바일 앱은 우선 고려하지 않는다. 따라서 FCM 같은 모바일 푸시 알림보다는 웹 서비스 기준의 인앱 알림 구조에 집중한다.

## 핵심 학습 주제

```text
채팅 = 방 단위 실시간 broadcast
알림 = 사용자 단위 실시간 전달
```

채팅은 특정 채팅방을 구독 중인 여러 사용자에게 메시지를 뿌리는 구조이고, 알림은 특정 사용자에게만 전달되는 구조다.

## 추천 기술 스택

```text
Spring Boot
Spring Web
Spring WebSocket
Spring Data JPA
Spring Data Redis
MySQL
Redis Pub/Sub
React 테스트 프론트엔드
```

## 구현할 도메인

### User

```text
id
username
nickname
password
createdAt
```

### ChatRoom

```text
id
name
createdAt
```

### ChatMessage

```text
id
ChatRoom room
User sender
content
sentAt
```

### Notification

```text
id
User receiver
NotificationType type
title
content
targetUrl
read
createdAt
```

예시 타입:

```text
CHAT_MESSAGE
INVITE
SYSTEM
```

## 구현할 API

### User API

```text
POST /api/users
GET  /api/users
```

### ChatRoom API

```text
POST /api/chat-rooms
GET  /api/chat-rooms
```

### ChatMessage API

```text
GET /api/chat-rooms/{roomId}/messages
```

최근 메시지 조회는 처음에는 최근 50개 정도로 구현한다.

### Notification API

```text
GET   /api/users/{userId}/notifications
GET   /api/users/{userId}/notifications/unread-count
PATCH /api/notifications/{notificationId}/read
PATCH /api/users/{userId}/notifications/read-all
```

## WebSocket Destination 설계

### 클라이언트 -> 서버

```text
STOMP SEND /app/chat.send
```

채팅 메시지 전송 요청.

요청 DTO:

```text
roomId
senderId
content
```

### 서버 -> 클라이언트

채팅방 메시지:

```text
STOMP SUBSCRIBE /topic/rooms/{roomId}
```

사용자 알림:

```text
STOMP SUBSCRIBE /topic/users/{userId}/notifications
```

처음에는 `/topic/users/{userId}/notifications` 방식으로 구현한다.

추후 Spring Security를 붙인 뒤에는 `convertAndSendToUser`와 `/user/queue/notifications` 구조도 학습한다.

## Redis Topic 설계

채팅과 알림 topic은 분리한다.

```yaml
chat:
  redis:
    topic: chatroom

notification:
  redis:
    topic: notification
```

## 채팅 메시지 흐름

```text
Client
-> STOMP SEND /app/chat.send
-> ChatMessageWebSocketController
-> ChatMessageService
-> User, ChatRoom 조회
-> ChatMessage DB 저장
-> ChatMessagePublisher가 Redis chat topic으로 publish
-> ChatMessageSubscriber가 Redis 메시지 수신
-> SimpMessagingTemplate
-> /topic/rooms/{roomId}
-> 채팅방 구독자들이 메시지 수신
```

## 알림 흐름

예: 채팅 메시지를 보냈을 때 채팅방의 다른 사용자에게 알림을 보낸다.

```text
채팅 메시지 발생
-> NotificationService
-> Notification DB 저장
-> NotificationPublisher가 Redis notification topic으로 publish
-> NotificationSubscriber가 Redis 메시지 수신
-> SimpMessagingTemplate
-> /topic/users/{receiverId}/notifications
-> 해당 사용자가 알림 수신
```

## 알림 저장이 필요한 이유

알림은 단순히 실시간으로 보내고 끝나는 데이터가 아니다.

DB에 저장해야 아래 기능을 만들 수 있다.

```text
알림 목록 조회
읽음 처리
안 읽은 알림 개수
나중에 다시 보기
```

따라서 WebSocket은 실시간 전달용이고, MySQL은 알림 기록 저장용이다.

## Redis Pub/Sub 역할

Redis Pub/Sub은 클라이언트에게 직접 보내는 기술이 아니라 서버 내부 이벤트 전달 통로다.

서버가 한 대일 때는 Redis 없이도 WebSocket으로 바로 보낼 수 있다.

하지만 서버가 여러 대라면 어떤 서버에 WebSocket 연결이 붙어 있는지 알 수 없기 때문에 Redis Pub/Sub으로 이벤트를 모든 서버에 전달하고, 각 서버가 자신에게 연결된 클라이언트에게 WebSocket으로 보내는 구조가 필요하다.

## 처음 구현 순서

1. User, ChatRoom, ChatMessage 기본 구조 만들기
2. 채팅방 생성/조회 REST API 만들기
3. 메시지 저장/최근 메시지 조회 API 만들기
4. WebSocketConfig 만들기
5. `/app/chat.send` 메시지 전송 구현
6. RedisConfig 만들기
7. ChatMessagePublisher/Subscriber 만들기
8. 채팅 실시간 전송 확인
9. Notification 엔티티 만들기
10. 알림 목록/읽음 처리 REST API 만들기
11. NotificationPublisher/Subscriber 만들기
12. 사용자별 알림 WebSocket 전송 구현
13. 채팅 메시지 발생 시 알림도 함께 생성하기
14. React 테스트 프론트엔드로 채팅 + 알림 확인하기

## 나중에 확장할 주제

```text
Spring Security
JWT
convertAndSendToUser
/user/queue destination
알림 권한 설정
채팅방 참여자 관리
읽지 않은 채팅 메시지 수
Redis Stream
Kafka
FCM Web Push
```

## Codex에게 요청할 때 시작 문장 예시

```text
NEXT_PROJECT_CONTEXT.md를 읽고, 채팅 + 알림 프로젝트를 만들고 싶어.
우선 Spring Boot 프로젝트 구조를 잡고, User/ChatRoom/ChatMessage/Notification 도메인부터 단계적으로 구현해줘.
모바일은 고려하지 않고 WebSocket + Redis Pub/Sub + MySQL 기반의 웹 인앱 알림 구조로 진행하고 싶어.
```
