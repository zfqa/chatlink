const express = require('express');
const { v4: uuidv4 } = require('uuid');
const db = require('../db');
const { authMiddleware } = require('../middleware/auth');

const router = express.Router();

// GET /api/v1/conversations/:conversationId/messages?before=&limit=50
router.get('/:conversationId/messages', authMiddleware, (req, res) => {
  const { conversationId } = req.params;
  const before = req.query.before ? Number(req.query.before) : Date.now();
  const limit = Math.min(Number(req.query.limit) || 50, 100);
  const participant = db.prepare('SELECT user_id FROM conversation_participants WHERE conversation_id = ? AND user_id = ?').get(conversationId, req.userId);
  if (!participant) return res.status(403).json({ code: 1003, message: 'Not a participant', data: null });
  const rows = db.prepare('SELECT id, conversation_id as conversationId, sender_id as senderId, content, type, created_at as timestamp, status FROM messages WHERE conversation_id = ? AND created_at < ? ORDER BY created_at DESC LIMIT ?').all(conversationId, before, limit);
  const messages = rows.reverse();
  res.json({ code: 0, message: 'success', data: { messages, hasMore: rows.length === limit } });
});

// POST /api/v1/conversations/:conversationId/messages
router.post('/:conversationId/messages', authMiddleware, (req, res) => {
  const { conversationId } = req.params;
  const { content, type } = req.body;
  if (!content) return res.status(400).json({ code: 1001, message: 'content is required', data: null });
  const participant = db.prepare('SELECT user_id FROM conversation_participants WHERE conversation_id = ? AND user_id = ?').get(conversationId, req.userId);
  if (!participant) return res.status(403).json({ code: 1003, message: 'Not a participant', data: null });
  const id = 'm_' + uuidv4().slice(0, 8);
  const now = Date.now();
  db.prepare('INSERT INTO messages (id, conversation_id, sender_id, content, type, status, created_at) VALUES (?,?,?,?,?,?,?)').run(id, conversationId, req.userId, content, type || 'TEXT', 'SENT', now);
  // Increment unread count for other participants
  db.prepare('UPDATE conversation_participants SET unread_count = unread_count + 1 WHERE conversation_id = ? AND user_id != ?').run(conversationId, req.userId);
  const message = { id, conversationId, senderId: req.userId, content, type: type || 'TEXT', timestamp: now, status: 'SENT' };
  res.json({ code: 0, message: 'success', data: { message } });
});

module.exports = router;
