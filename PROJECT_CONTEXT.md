# Redis Member Chat Project Context

## 프로젝트 목적

회원, 채팅방, 메시지 저장, Redis Pub/Sub, WebSocket STOMP 흐름을 직접 구현해보는 학습용 Spring Boot 프로젝트입니다.

JWT 없이 단순한 회원 식별 구조로 시작하고, 메시지는 MySQL에 저장하며 Redis Pub/Sub은 실시간 전달 경로로 사용합니다.

## 현재 구현 상태

구현된 기능:

```text
회원 생성
회원 목록 조회
채팅방 생성
채팅방 목록 조회
채팅 메시지 DB 저장
최근 메시지 조회
Redis Pub/Sub 메시지 publish/subscribe
STOMP WebSocket 실시간 broadcast
React 테스트 프론트엔드
```

패키지 구조:

```text
src/main/java/com/example/memberchat
├── api
├── config
├── domain
│   ├── chatroom
│   ├── message
│   └── user
├── redis
└── websocket
```

## 실행

로컬 MySQL을 사용할 경우:

```sql
CREATE DATABASE IF NOT EXISTS member_chat
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

```bash
./gradlew bootRun
```

Docker MySQL/Redis를 사용할 경우:

```bash
docker compose up -d redis mysql
export DB_URL='jdbc:mysql://localhost:3307/member_chat?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul&characterEncoding=utf8'
export DB_USERNAME='member_chat_user'
export DB_PASSWORD='member_chat_password'
./gradlew bootRun
```

프론트엔드:

```bash
cd frontend
npm install
npm run dev
```

## 핵심 API

```text
POST /api/users
GET  /api/users

POST /api/chat-rooms
GET  /api/chat-rooms

GET  /api/chat-rooms/{roomId}/messages

STOMP SEND      /app/chat.send
STOMP SUBSCRIBE /topic/rooms/{roomId}
```

## 핵심 흐름

```text
클라이언트
-> STOMP SEND /app/chat.send
-> Spring WebSocket Controller
-> User, ChatRoom 조회
-> ChatMessage DB 저장
-> Redis publish
-> Redis subscribe
-> SimpMessagingTemplate
-> /topic/rooms/{roomId}
-> 구독 중인 클라이언트 수신
```

## 학습용 제한

```text
JWT 없음
비밀번호 암호화 없음
클라이언트가 senderId 전송
Redis Pub/Sub은 실시간 전달용
MySQL은 메시지 기록 저장용
```
