# ChatLink Deployment Guide

## Prerequisites

- Node.js 18+ (tested with 20.x)
- npm
- A Linux VPS (Ubuntu 22.04+ recommended)
- A domain name (optional, for HTTPS)

## 1. Server Setup

### Install Node.js

```bash
curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
sudo apt install -y nodejs
node --version
```

### Clone & Install

```bash
git clone https://github.com/zfqa/chatlink.git
cd chatlink/backend
npm install
```

### Configure Environment

```bash
cp .env.example .env
nano .env
```

**Production `.env`:**

```env
PORT=3000
NODE_ENV=production

# IMPORTANT: Generate strong random secrets
JWT_ACCESS_SECRET=<random-64-char-string>
JWT_REFRESH_SECRET=<random-64-char-string>
JWT_ACCESS_EXPIRES_IN=24h
JWT_REFRESH_EXPIRES_IN=30d

DATABASE_PATH=./data/chatlink.db

# CORS — your frontend domain, or * for all
ALLOWED_ORIGINS=*
```

Generate secrets with:
```bash
node -e "console.log(require('crypto').randomBytes(48).toString('hex'))"
```

### Start Server

```bash
# Development (auto-restart on changes)
npm run dev

# Production (no auto-restart)
npm start

# Production (with pm2 for auto-restart)
npm install -g pm2
pm2 start src/index.js --name chatlink
pm2 save
pm2 startup
```

### Verify

```bash
curl http://localhost:3000/api/v1/health
# Should return: {"code":0,"message":"ChatLink backend is running","data":null}
```

## 2. Reverse Proxy (Nginx)

WebSocket requires special Nginx config. Example:

```nginx
server {
    listen 80;
    server_name api.yourdomain.com;

    location / {
        proxy_pass http://127.0.0.1:3000;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # WebSocket support
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_read_timeout 86400;
    }
}
```

For HTTPS, add certbot:
```bash
sudo apt install certbot python3-certbot-nginx
sudo certbot --nginx -d api.yourdomain.com
```

## 3. Android Client Configuration

### Debug (local testing)

In `local.properties`:
```properties
API_BASE_URL=http://127.0.0.1:3000
```

With USB debugging:
```bash
adb reverse tcp:3000 tcp:3000
```

### Release (production server)

In `local.properties`:
```properties
API_BASE_URL=https://api.yourdomain.com
```

WebSocket URL is derived automatically: `wss://api.yourdomain.com/ws`

Build release APK:
```bash
./gradlew assembleRelease
```

## 4. Database

SQLite database is at the path specified by `DATABASE_PATH` (default: `./data/chatlink.db`).

### Backup

```bash
# Stop the server first, or use SQLite's backup command
cp ./data/chatlink.db ./data/chatlink.db.backup-$(date +%Y%m%d)
```

### Restore

```bash
# Stop the server
cp ./data/chatlink.db.backup-20260101 ./data/chatlink.db
# Restart the server
```

## 5. Firewall

```bash
# Allow SSH
sudo ufw allow 22

# Allow HTTP/HTTPS
sudo ufw allow 80
sudo ufw allow 443

# Enable
sudo ufw enable
```

Do NOT expose port 3000 directly — use Nginx as reverse proxy.

## 6. Troubleshooting

### WebSocket not connecting through Nginx

Ensure these headers are set:
```
proxy_set_header Upgrade $http_upgrade;
proxy_set_header Connection "upgrade";
```

### SQLite locked errors

Ensure only one Node.js process accesses the database. Use `pm2` instead of running multiple instances.

### Android can't connect

- Debug: check `adb reverse tcp:3000 tcp:3000`
- Release: check `API_BASE_URL` in `local.properties` matches your server
- Check server logs for incoming requests

## 7. Security Checklist

- [ ] Change JWT secrets in `.env`
- [ ] Set `NODE_ENV=production`
- [ ] Configure `ALLOWED_ORIGINS` (not `*`)
- [ ] Enable HTTPS via certbot
- [ ] Firewall enabled, only 22/80/443 open
- [ ] `.env` not committed to Git
- [ ] SQLite database backed up regularly
