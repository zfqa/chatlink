const express = require('express');
const { v4: uuidv4 } = require('uuid');
const db = require('../db');
const { authMiddleware } = require('../middleware/auth');

const router = express.Router();

// POST /api/v1/friends/requests
router.post('/requests', authMiddleware, (req, res) => {
  const { toUserId } = req.body;
  if (!toUserId) return res.status(400).json({ code: 1001, message: 'toUserId is required', data: null });
  if (toUserId === req.userId) return res.status(400).json({ code: 1001, message: 'Cannot add yourself', data: null });
  const target = db.prepare('SELECT id, email, nickname, avatar_url as avatarUrl, signature FROM users WHERE id = ?').get(toUserId);
  if (!target) return res.status(404).json({ code: 1004, message: 'User not found', data: null });
  const existing = db.prepare('SELECT id FROM friend_requests WHERE from_user_id = ? AND to_user_id = ? AND status = ?').get(req.userId, toUserId, 'PENDING');
  if (existing) return res.status(409).json({ code: 1005, message: 'Request already sent', data: null });
  const already = db.prepare('SELECT user_id FROM contacts WHERE user_id = ? AND contact_id = ?').get(req.userId, toUserId);
  if (already) return res.status(409).json({ code: 1005, message: 'Already friends', data: null });
  const id = 'fr_' + uuidv4().slice(0, 8);
  db.prepare('INSERT INTO friend_requests (id, from_user_id, to_user_id) VALUES (?,?,?)').run(id, req.userId, toUserId);
  const me = db.prepare('SELECT id, email, nickname, avatar_url as avatarUrl, signature FROM users WHERE id = ?').get(req.userId);
  const request = { id, fromUser: me, toUserId, timestamp: Date.now(), status: 'PENDING' };
  res.json({ code: 0, message: 'success', data: { request } });
});

// GET /api/v1/friends/requests?status=PENDING
router.get('/requests', authMiddleware, (req, res) => {
  const status = req.query.status || 'PENDING';
  const rows = db.prepare('SELECT fr.id, fr.from_user_id, fr.to_user_id, fr.status, fr.created_at, u.id as f_id, u.email as f_email, u.nickname as f_nickname, u.avatar_url as f_avatar, u.signature as f_sig FROM friend_requests fr JOIN users u ON u.id = fr.from_user_id WHERE fr.to_user_id = ? AND fr.status = ? ORDER BY fr.created_at DESC').all(req.userId, status);
  const requests = rows.map(r => ({ id: r.id, fromUser: { id: r.f_id, email: r.f_email || '', nickname: r.f_nickname, avatarUrl: r.f_avatar, signature: r.f_sig }, toUserId: r.to_user_id, timestamp: r.created_at, status: r.status }));
  res.json({ code: 0, message: 'success', data: { requests } });
});

// PUT /api/v1/friends/requests/:requestId/accept
router.put('/requests/:requestId/accept', authMiddleware, (req, res) => {
  const row = db.prepare('SELECT id, from_user_id, to_user_id, status FROM friend_requests WHERE id = ? AND to_user_id = ?').get(req.params.requestId, req.userId);
  if (!row) return res.status(404).json({ code: 1004, message: 'Request not found', data: null });
  if (row.status !== 'PENDING') return res.status(400).json({ code: 1001, message: 'Request already processed', data: null });
  const now = Date.now();
  db.prepare('UPDATE friend_requests SET status = ? WHERE id = ?').run('ACCEPTED', row.id);
  db.prepare('INSERT OR IGNORE INTO contacts (user_id, contact_id, created_at) VALUES (?,?,?)').run(row.to_user_id, row.from_user_id, now);
  db.prepare('INSERT OR IGNORE INTO contacts (user_id, contact_id, created_at) VALUES (?,?,?)').run(row.from_user_id, row.to_user_id, now);
  res.json({ code: 0, message: 'success', data: null });
});

// PUT /api/v1/friends/requests/:requestId/reject
router.put('/requests/:requestId/reject', authMiddleware, (req, res) => {
  const row = db.prepare('SELECT id, status FROM friend_requests WHERE id = ? AND to_user_id = ?').get(req.params.requestId, req.userId);
  if (!row) return res.status(404).json({ code: 1004, message: 'Request not found', data: null });
  if (row.status !== 'PENDING') return res.status(400).json({ code: 1001, message: 'Request already processed', data: null });
  db.prepare('UPDATE friend_requests SET status = ? WHERE id = ?').run('REJECTED', row.id);
  res.json({ code: 0, message: 'success', data: null });
});

module.exports = router;
