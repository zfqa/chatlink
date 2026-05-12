# ChatLink Backend

Lightweight Express + SQLite + JWT backend for ChatLink app.

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

## Environment Variables

See `.env.example`.

## Database

SQLite at `./data/chatlink.db`. Auto-created on first run.

## API Contract

See `docs/API_CONTRACT.md` for full specification.