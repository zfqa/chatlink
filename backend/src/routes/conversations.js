const express = require('express');
const { v4: uuidv4 } = require('uuid');
const db = require('../db');
const { authMiddleware } = require('../middleware/auth');

const router = express.Router();

function getPeer(convId, userId) {
  const peer = db.prepare('SELECT u.id, u.email, u.nickname, u.avatar_url as avatarUrl, u.signature FROM conversation_participants cp JOIN users u ON u.id = cp.user_id WHERE cp.conversation_id = ? AND cp.user_id != ?').get(convId, userId);
  return peer || { id: '', email: '', nickname: '', avatarUrl: '', signature: '' };
}

function getLastMessage(convId) {
  return db.prepare('SELECT content, created_at FROM messages WHERE conversation_id = ? ORDER BY created_at DESC LIMIT 1').get(convId);
}

function getGroupInfo(convId) {
  return db.prepare('SELECT name, avatar_url as avatarUrl, owner_id as ownerId FROM groups WHERE conversation_id = ?').get(convId);
}

// GET /api/v1/conversations
router.get('/', authMiddleware, (req, res) => {
  const rows = db.prepare('SELECT cp.conversation_id, cp.unread_count, cp.is_pinned, cp.is_muted FROM conversation_participants cp WHERE cp.user_id = ?').all(req.userId);
  const conversations = rows.map(r => {
    const conv = db.prepare('SELECT type FROM conversations WHERE id = ?').get(r.conversation_id);
    const last = getLastMessage(r.conversation_id);
    const base = {
      id: r.conversation_id,
      type: conv ? conv.type : 'DIRECT',
      lastMessage: last ? last.content : '',
      lastMessageTime: last ? last.created_at : 0,
      unreadCount: r.unread_count,
      isPinned: !!r.is_pinned,
      isMuted: !!r.is_muted,
    };
    if (conv && conv.type === 'GROUP') {
      const group = getGroupInfo(r.conversation_id);
      return {
        ...base,
        groupName: group ? group.name : 'Group',
        groupAvatarUrl: group ? group.avatarUrl : '',
        ownerId: group ? group.ownerId : '',
      };
    } else {
      const peer = getPeer(r.conversation_id, req.userId);
      return { ...base, peer };
    }
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
  const peer = db.prepare('SELECT id, email, nickname, avatar_url as avatarUrl, signature FROM users WHERE id = ?').get(peerId);
  if (!peer) return res.status(404).json({ code: 1004, message: 'User not found', data: null });
  // Find existing direct conversation between two users
  const existing = db.prepare('SELECT cp1.conversation_id FROM conversation_participants cp1 JOIN conversation_participants cp2 ON cp1.conversation_id = cp2.conversation_id JOIN conversations c ON c.id = cp1.conversation_id WHERE cp1.user_id = ? AND cp2.user_id = ? AND c.type = ?').get(req.userId, peerId, 'DIRECT');
  if (existing) {
    const convId = existing.conversation_id;
    const peerData = getPeer(convId, req.userId);
    const last = getLastMessage(convId);
    return res.json({ code: 0, message: 'success', data: { conversation: { id: convId, type: 'DIRECT', peer: peerData, lastMessage: last ? last.content : '', lastMessageTime: last ? last.created_at : 0, unreadCount: 0, isPinned: false, isMuted: false } } });
  }
  // Create new conversation
  const convId = 'conv_' + uuidv4().slice(0, 8);
  db.prepare('INSERT INTO conversations (id, type) VALUES (?,?)').run(convId, 'DIRECT');
  db.prepare('INSERT INTO conversation_participants (conversation_id, user_id, role) VALUES (?,?,?)').run(convId, req.userId, 'member');
  db.prepare('INSERT INTO conversation_participants (conversation_id, user_id, role) VALUES (?,?,?)').run(convId, peerId, 'member');
  res.json({ code: 0, message: 'success', data: { conversation: { id: convId, type: 'DIRECT', peer, lastMessage: '', lastMessageTime: 0, unreadCount: 0, isPinned: false, isMuted: false } } });
});

// POST /api/v1/conversations/group — create group chat
router.post('/group', authMiddleware, (req, res) => {
  const { name, memberIds } = req.body;
  if (!name || !memberIds || !Array.isArray(memberIds) || memberIds.length === 0) {
    return res.status(400).json({ code: 1001, message: 'name and memberIds are required', data: null });
  }
  // Deduplicate and include creator
  const allMembers = [...new Set([req.userId, ...memberIds])];
  if (allMembers.length < 2) {
    return res.status(400).json({ code: 1001, message: 'At least 2 members required', data: null });
  }
  const convId = 'conv_' + uuidv4().slice(0, 8);
  const now = Date.now();
  db.prepare('INSERT INTO conversations (id, type, created_at) VALUES (?,?,?)').run(convId, 'GROUP', now);
  db.prepare('INSERT INTO groups (conversation_id, name, owner_id, created_at) VALUES (?,?,?,?)').run(convId, name, req.userId, now);
  const insertMember = db.prepare('INSERT INTO conversation_participants (conversation_id, user_id, role) VALUES (?,?,?)');
  for (const memberId of allMembers) {
    const role = memberId === req.userId ? 'owner' : 'member';
    insertMember.run(convId, memberId, role);
  }
  const group = getGroupInfo(convId);
  res.json({
    code: 0, message: 'success',
    data: {
      conversation: {
        id: convId,
        type: 'GROUP',
        groupName: group.name,
        groupAvatarUrl: group.avatarUrl,
        ownerId: group.ownerId,
        lastMessage: '',
        lastMessageTime: now,
        unreadCount: 0,
        isPinned: false,
        isMuted: false,
      }
    }
  });
});

// GET /api/v1/conversations/:conversationId/members
router.get('/:conversationId/members', authMiddleware, (req, res) => {
  const { conversationId } = req.params;
  const participant = db.prepare('SELECT user_id FROM conversation_participants WHERE conversation_id = ? AND user_id = ?').get(conversationId, req.userId);
  if (!participant) return res.status(403).json({ code: 1003, message: 'Not a participant', data: null });
  const members = db.prepare('SELECT u.id, u.email, u.nickname, u.avatar_url as avatarUrl, u.signature, cp.role FROM conversation_participants cp JOIN users u ON u.id = cp.user_id WHERE cp.conversation_id = ?').all(conversationId);
  res.json({ code: 0, message: 'success', data: { members } });
});

// POST /api/v1/conversations/:conversationId/members — add members
router.post('/:conversationId/members', authMiddleware, (req, res) => {
  const { conversationId } = req.params;
  const { userIds } = req.body;
  if (!userIds || !Array.isArray(userIds) || userIds.length === 0) {
    return res.status(400).json({ code: 1001, message: 'userIds are required', data: null });
  }
  // Check requester is a member
  const participant = db.prepare('SELECT role FROM conversation_participants WHERE conversation_id = ? AND user_id = ?').get(conversationId, req.userId);
  if (!participant) return res.status(403).json({ code: 1003, message: 'Not a participant', data: null });
  // Check it's a group
  const conv = db.prepare('SELECT type FROM conversations WHERE id = ?').get(conversationId);
  if (!conv || conv.type !== 'GROUP') return res.status(400).json({ code: 1001, message: 'Not a group conversation', data: null });
  const insertMember = db.prepare('INSERT OR IGNORE INTO conversation_participants (conversation_id, user_id, role) VALUES (?,?,?)');
  let added = 0;
  for (const userId of userIds) {
    const result = insertMember.run(conversationId, userId, 'member');
    if (result.changes > 0) added++;
  }
  res.json({ code: 0, message: 'success', data: { added } });
});

module.exports = router;
