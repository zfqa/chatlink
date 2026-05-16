const express = require('express');
const db = require('../db');
const { authMiddleware } = require('../middleware/auth');

const router = express.Router();

// GET /api/v1/users/me
router.get('/me', authMiddleware, (req, res) => {
  const user = db.prepare('SELECT id, email, nickname, avatar_url as avatarUrl, signature FROM users WHERE id = ?').get(req.userId);
  if (!user) return res.status(404).json({ code: 1004, message: 'User not found', data: null });
  res.json({ code: 0, message: 'success', data: user });
});

// PUT /api/v1/users/me
router.put('/me', authMiddleware, (req, res) => {
  const { nickname, signature } = req.body;
  const sets = [];
  const params = [];
  if (nickname !== undefined) { sets.push('nickname = ?'); params.push(nickname); }
  if (signature !== undefined) { sets.push('signature = ?'); params.push(signature); }
  if (sets.length === 0) return res.status(400).json({ code: 1001, message: 'Nothing to update', data: null });
  sets.push('updated_at = ?'); params.push(Date.now());
  params.push(req.userId);
  db.prepare('UPDATE users SET ' + sets.join(', ') + ' WHERE id = ?').run(...params);
  const user = db.prepare('SELECT id, nickname, avatar_url as avatarUrl, signature FROM users WHERE id = ?').get(req.userId);
  res.json({ code: 0, message: 'success', data: user });
});

// GET /api/v1/users/search?q=
router.get('/search', authMiddleware, (req, res) => {
  const q = req.query.q || '';
  let users;
  if (q.trim() === '') {
    users = db.prepare('SELECT id, nickname, avatar_url as avatarUrl, signature FROM users WHERE id != ? LIMIT 50').all(req.userId);
  } else {
    users = db.prepare('SELECT id, nickname, avatar_url as avatarUrl, signature FROM users WHERE id != ? AND (nickname LIKE ? OR id LIKE ?) LIMIT 50').all(req.userId, '%' + q + '%', '%' + q + '%');
  }
  res.json({ code: 0, message: 'success', data: { users } });
});

// GET /api/v1/users/:userId
router.get('/:userId', authMiddleware, (req, res) => {
  const user = db.prepare('SELECT id, nickname, avatar_url as avatarUrl, signature FROM users WHERE id = ?').get(req.params.userId);
  if (!user) return res.status(404).json({ code: 1004, message: 'User not found', data: null });
  res.json({ code: 0, message: 'success', data: user });
});

module.exports = router;
