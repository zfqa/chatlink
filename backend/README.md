# ChatLink Backend

Lightweight Express + SQLite + JWT backend for ChatLink app with WebSocket real-time messaging.

## Quick Start

1. Install dependencies: `npm install`

2. Copy environment config: `cp .env.example .env`

3. Start server: `npm run dev` (dev) or `npm start` (prod)

## API Endpoints

Base URL: `http://localhost:3000/api/v1`

### Auth
- `POST /auth/register` - Register
- `POST /auth/login` - Login
- `POST /auth/logout` - Logout (token required)
- `POST /auth/refresh` - Refresh tokens

### Users
- `GET /users/me` - Current user
- `PUT /users/me` - Update profile
- `GET /users/search?q=` - Search users
- `GET /users/:id` - Get user by ID

### Contacts
- `GET /contacts` - Contacts list

### Friends
- `POST /friends/requests` - Send request
- `GET /friends/requests?status=PENDING` - Incoming requests
- `PUT /friends/requests/:id/accept` - Accept
- `PUT /friends/requests/:id/reject` - Reject

### Conversations
- `GET /conversations` - List conversations
- `POST /conversations/direct` - Create/get direct conversation

### Messages
- `GET /conversations/:id/messages?before=&limit=` - Get messages
- `POST /conversations/:id/messages` - Send text message

## WebSocket

Connect to receive real-time message notifications.

### Connection

```
ws://localhost:3000/ws?token=JWT_ACCESS_TOKEN
```

- Token is required and verified against `JWT_ACCESS_SECRET`
- Invalid or missing token will close the connection with code 4001/4002

### Server Events

#### `connected` — sent immediately after successful connection
```json
{
  "type": "connected",
  "data": { "userId": "u_xxxx", "serverTime": 1710000000000 }
}
```

#### `message:new` — sent when a new message arrives in a conversation you belong to
```json
{
  "type": "message:new",
  "data": {
    "id": "m_xxxx",
    "conversationId": "conv_xxxx",
    "senderId": "u_xxxx",
    "content": "hello",
    "type": "text",
    "createdAt": 1710000000000
  }
}
```

### Testing WebSocket

1. Start the server: `npm run dev`

2. Login to get a token:
   ```bash
   curl -X POST http://localhost:3000/api/v1/auth/login \
     -H "Content-Type: application/json" \
     -d '{"email":"alice@test.com","password":"123456"}'
   ```

3. Connect with the test script:
   ```bash
   node scripts/ws-test.js <TOKEN_FROM_LOGIN>
   ```

4. From another terminal, send a message as a different user:
   ```bash
   curl -X POST http://localhost:3000/api/v1/conversations/<CONV_ID>/messages \
     -H "Authorization: Bearer <OTHER_TOKEN>" \
     -H "Content-Type: application/json" \
     -d '{"content":"hello from curl"}'
   ```

5. The test script should print the `message:new` event.

### Implementation Notes

- One user can have multiple WebSocket connections (e.g., multiple devices)
- Connections are stored in memory: `userId -> Set<WebSocket>`
- Heartbeat pings every 30 seconds; dead connections are terminated
- Messages are broadcast to all conversation members except the sender
- Offline users receive nothing (no message queue; they'll get messages via HTTP on next fetch)

## Environment Variables

See `.env.example`.

## Database

SQLite at `./data/chatlink.db`. Auto-created on first run.

## API Contract

See `docs/API_CONTRACT.md` for full specification.
