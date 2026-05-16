const express = require('express');
const { v4: uuidv4 } = require('uuid');
const db = require('../db');
const { authMiddleware } = require('../middleware/auth');

const router = express.Router();

function getPeer(convId, userId) {
  const peer = db.prepare('SELECT u.id, u.nickname, u.avatar_url as avatarUrl, u.signature FROM conversation_participants cp JOIN users u ON u.id = cp.user_id WHERE cp.conversation_id = ? AND cp.user_id != ?').get(convId, userId);
  return peer || { id: '', nickname: '', avatarUrl: '', signature: '' };
}

function getLastMessage(convId) {
  return db.prepare('SELECT content, created_at FROM messages WHERE conversation_id = ? ORDER BY created_at DESC LIMIT 1').get(convId);
}

// GET /api/v1/conversations
router.get('/', authMiddleware, (req, res) => {
  const rows = db.prepare('SELECT cp.conversation_id, cp.unread_count, cp.is_pinned, cp.is_muted FROM conversation_participants cp WHERE cp.user_id = ?').all(req.userId);
  const conversations = rows.map(r => {
    const peer = getPeer(r.conversation_id, req.userId);
    const last = getLastMessage(r.conversation_id);
    return { id: r.conversation_id, peer, lastMessage: last ? last.content : '', lastMessageTime: last ? last.created_at : 0, unreadCount: r.unread_count, isPinned: !!r.is_pinned, isMuted: !!r.is_muted };
  }).sort((a, b) => b.lastMessageTime - a.lastMessageTime);
  res.json({ code: 0, message: 'success', data: { conversations } });
});

// POST /api/v1/conversations/:conversationId/read
router.post('/:conversationId/read', authMiddleware, (req, res) => {
  const { conversationId } = req.params;
  const participant = db.prepare('SELECT user_id FROM conversation_participants WHERE conversation_id = ? AND user_id = ?').get(conversationId, req.userId);
  if (!participant) return res.status(403).json({ code: 1003, message: 'Not a participant', data: null });
  db.prepare('UPDATE conversation_participants SET unread_count = 0 WHERE conversation_id = ? AND user_id = ?').run(conversationId, req.userId);
  res.json({ code: 0, message: 'success', data: null });
});

// POST /api/v1/conversations/direct
router.post('/direct', authMiddleware, (req, res) => {
  const { peerId } = req.body;
  if (!peerId) return res.status(400).json({ code: 1001, message: 'peerId is required', data: null });
  if (peerId === req.userId) return res.status(400).json({ code: 1001, message: 'Cannot chat with yourself', data: null });
  const peer = db.prepare('SELECT id, nickname, avatar_url as avatarUrl, signature FROM users WHERE id = ?').get(peerId);
  if (!peer) return res.status(404).json({ code: 1004, message: 'User not found', data: null });
  // Find existing direct conversation between two users
  const existing = db.prepare('SELECT cp1.conversation_id FROM conversation_participants cp1 JOIN conversation_participants cp2 ON cp1.conversation_id = cp2.conversation_id JOIN conversations c ON c.id = cp1.conversation_id WHERE cp1.user_id = ? AND cp2.user_id = ? AND c.type = ?').get(req.userId, peerId, 'DIRECT');
  if (existing) {
    const convId = existing.conversation_id;
    const peerData = getPeer(convId, req.userId);
    const last = getLastMessage(convId);
    return res.json({ code: 0, message: 'success', data: { conversation: { id: convId, peer: peerData, lastMessage: last ? last.content : '', lastMessageTime: last ? last.created_at : 0, unreadCount: 0, isPinned: false, isMuted: false } } });
  }
  // Create new conversation
  const convId = 'conv_' + uuidv4().slice(0, 8);
  db.prepare('INSERT INTO conversations (id, type) VALUES (?,?)').run(convId, 'DIRECT');
  db.prepare('INSERT INTO conversation_participants (conversation_id, user_id) VALUES (?,?)').run(convId, req.userId);
  db.prepare('INSERT INTO conversation_participants (conversation_id, user_id) VALUES (?,?)').run(convId, peerId);
  res.json({ code: 0, message: 'success', data: { conversation: { id: convId, peer, lastMessage: '', lastMessageTime: 0, unreadCount: 0, isPinned: false, isMuted: false } } });
});

module.exports = router;
