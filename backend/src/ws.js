const { WebSocketServer } = require('ws');
const jwt = require('jsonwebtoken');
const db = require('./db');

// userId -> Set<ws>
const onlineUsers = new Map();

function initWebSocket(server) {
  const wss = new WebSocketServer({ server, path: '/ws' });

  wss.on('connection', (ws, req) => {
    // Extract token from query: ws://host/ws?token=JWT
    const url = new URL(req.url, 'http://localhost');
    const token = url.searchParams.get('token');

    if (!token) {
      ws.close(4001, 'Missing token');
      return;
    }

    let userId;
    try {
      const payload = jwt.verify(token, process.env.JWT_ACCESS_SECRET);
      userId = payload.userId;
    } catch (err) {
      ws.close(4002, 'Invalid or expired token');
      return;
    }

    // Register connection
    if (!onlineUsers.has(userId)) {
      onlineUsers.set(userId, new Set());
    }
    onlineUsers.get(userId).add(ws);
    ws.userId = userId;

    console.log(`[WS] connected userId=${userId} total=${onlineUsers.get(userId).size}`);

    // Send connected confirmation
    ws.send(JSON.stringify({
      type: 'connected',
      data: { userId, serverTime: Date.now() },
    }));

    // Heartbeat
    ws.isAlive = true;
    ws.on('pong', () => { ws.isAlive = true; });

    ws.on('close', () => {
      const conns = onlineUsers.get(userId);
      if (conns) {
        conns.delete(ws);
        if (conns.size === 0) {
          onlineUsers.delete(userId);
        }
      }
      console.log(`[WS] disconnected userId=${userId} remaining=${conns ? conns.size : 0}`);
    });

    ws.on('error', (err) => {
      console.error(`[WS] error userId=${userId}:`, err.message);
    });
  });

  // Heartbeat interval — terminate dead connections every 30s
  const heartbeat = setInterval(() => {
    wss.clients.forEach((ws) => {
      if (!ws.isAlive) return ws.terminate();
      ws.isAlive = false;
      ws.ping();
    });
  }, 30000);

  wss.on('close', () => clearInterval(heartbeat));

  console.log('[WS] WebSocket server initialized on /ws');
  return wss;
}

/**
 * Send a message to all online members of a conversation (except the sender).
 */
function broadcastToConversation(conversationId, senderId, payload) {
  // Get all participant user IDs for this conversation
  const members = db.prepare(
    'SELECT user_id FROM conversation_participants WHERE conversation_id = ?'
  ).all(conversationId);

  const data = JSON.stringify(payload);
  console.log(`[WS] broadcast: conversation=${conversationId} senderId=${senderId} members=${members.map(m => m.user_id).join(',')}`);
  console.log(`[WS] broadcast: onlineUsers keys=[${[...onlineUsers.keys()].join(',')}]`);

  let sent = 0;
  for (const member of members) {
    if (member.user_id === senderId) {
      console.log(`[WS] broadcast: skip sender ${member.user_id}`);
      continue;
    }
    const conns = onlineUsers.get(member.user_id);
    if (!conns || conns.size === 0) {
      console.log(`[WS] broadcast: user ${member.user_id} not online`);
      continue;
    }

    for (const ws of conns) {
      console.log(`[WS] broadcast: user=${member.user_id} readyState=${ws.readyState}`);
      if (ws.readyState === 1) { // WebSocket.OPEN
        try {
          ws.send(data, (err) => {
            if (err) console.error(`[WS] send error for user=${member.user_id}:`, err.message);
            else console.log(`[WS] send OK for user=${member.user_id}`);
          });
          sent++;
        } catch (e) {
          console.error(`[WS] send exception for user=${member.user_id}:`, e.message);
        }
      }
    }
  }

  console.log(`[WS] message:new pushed to ${sent} connection(s) for conversation=${conversationId}`);
  return sent;
}

/**
 * Check if a user is currently online.
 */
function isUserOnline(userId) {
  const conns = onlineUsers.get(userId);
  return conns ? conns.size > 0 : false;
}

/**
 * Get count of online users.
 */
function getOnlineCount() {
  return onlineUsers.size;
}

module.exports = { initWebSocket, broadcastToConversation, isUserOnline, getOnlineCount };
