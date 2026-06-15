# Redis Member Chat

Spring Boot, MySQL, Redis Pub/Sub, WebSocket STOMP를 사용한 학습용 회원 채팅 프로젝트입니다.

## 주요 기능

- 회원 생성 및 회원 목록 조회
- 채팅방 생성 및 채팅방 목록 조회
- 채팅 메시지 MySQL 저장
- 채팅방별 최근 메시지 조회
- Redis Pub/Sub 기반 메시지 publish/subscribe
- STOMP WebSocket 기반 실시간 채팅
- React 테스트 프론트엔드

## 기술 스택

- Java 17
- Spring Boot 3.5
- Spring Web
- Spring WebSocket
- Spring Data JPA
- Spring Data Redis
- MySQL
- Redis
- React + Vite

## 실행 준비

로컬 MySQL을 사용할 경우 `member_chat` 데이터베이스를 먼저 생성합니다.

```sql
CREATE DATABASE IF NOT EXISTS member_chat
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

기본 설정은 로컬 MySQL `localhost:3306`, 계정 `root/root`를 사용합니다. 다른 값을 쓰려면 환경변수로 변경할 수 있습니다.

```bash
export DB_URL='jdbc:mysql://localhost:3306/member_chat?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul&characterEncoding=utf8'
export DB_USERNAME='root'
export DB_PASSWORD='root'
```

Redis는 기본적으로 `localhost:6379`를 사용합니다.

## 백엔드 실행

```bash
./gradlew bootRun
```

테스트:

```bash
./gradlew test
```

## Docker로 MySQL/Redis 실행

Docker Compose를 사용할 경우 MySQL은 로컬 MySQL과 충돌을 피하기 위해 host `3307` 포트로 노출됩니다.

```bash
docker compose up -d redis mysql
```

Docker MySQL에 연결하려면 다음 환경변수를 사용합니다.

```bash
export DB_URL='jdbc:mysql://localhost:3307/member_chat?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul&characterEncoding=utf8'
export DB_USERNAME='member_chat_user'
export DB_PASSWORD='member_chat_password'
./gradlew bootRun
```

## 테스트 프론트엔드 실행

React 테스트 화면은 `frontend` 폴더에 있습니다.

```bash
cd frontend
npm install
npm run dev
```

브라우저에서 `http://localhost:5173`으로 접속하면 유저 생성, 채팅방 생성, 최근 메시지 조회, WebSocket 메시지 전송을 테스트할 수 있습니다.

## API

```text
POST /api/users
GET  /api/users

POST /api/chat-rooms
GET  /api/chat-rooms

GET  /api/chat-rooms/{roomId}/messages

STOMP SEND      /app/chat.send
STOMP SUBSCRIBE /topic/rooms/{roomId}
```

## 메시지 흐름

```text
Client
-> STOMP SEND /app/chat.send
-> Spring WebSocket Controller
-> ChatMessage DB 저장
-> Redis publish
-> Redis subscriber
-> SimpMessagingTemplate
-> /topic/rooms/{roomId}
-> 구독 중인 클라이언트 수신
```
