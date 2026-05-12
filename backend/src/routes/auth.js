const express = require('express');
const bcrypt = require('bcryptjs');
const { v4: uuidv4 } = require('uuid');
const db = require('../db');
const { signAccessToken, signRefreshToken, authMiddleware } = require('../middleware/auth');

const router = express.Router();

// POST /api/v1/auth/register
router.post('/register', (req, res) => {
  const { email, password, nickname } = req.body;
  if (!email || !password || !nickname) {
    return res.status(400).json({ code: 1001, message: 'email, password, nickname are required', data: null });
  }
  const existing = db.prepare('SELECT id FROM users WHERE email = ?').get(email);
  if (existing) {
    return res.status(409).json({ code: 1005, message: 'Email already registered', data: null });
  }
  const id = 'u_' + uuidv4().slice(0, 8);
  const hash = bcrypt.hashSync(password, 10);
  const now = Date.now();
  db.prepare('INSERT INTO users (id, email, password_hash, nickname, created_at, updated_at) VALUES (?,?,?,?,?,?)').run(id, email, hash, nickname, now, now);
  const user = db.prepare('SELECT id, nickname, avatar_url as avatarUrl, signature FROM users WHERE id = ?').get(id);
  const accessToken = signAccessToken(id);
  const refreshToken = signRefreshToken(id);
  res.json({ code: 0, message: 'success', data: { user, accessToken, refreshToken } });
});

// POST /api/v1/auth/login
router.post('/login', (req, res) => {
  const { email, password } = req.body;
  if (!email || !password) {
    return res.status(400).json({ code: 1001, message: 'email and password are required', data: null });
  }
  const row = db.prepare('SELECT id, nickname, avatar_url as avatarUrl, signature, password_hash FROM users WHERE email = ?').get(email);
  if (!row || !bcrypt.compareSync(password, row.password_hash)) {
    return res.status(401).json({ code: 1002, message: 'Invalid email or password', data: null });
  }
  const user = { id: row.id, nickname: row.nickname, avatarUrl: row.avatarUrl, signature: row.signature };
  const accessToken = signAccessToken(row.id);
  const refreshToken = signRefreshToken(row.id);
  res.json({ code: 0, message: 'success', data: { user, accessToken, refreshToken } });
});

// POST /api/v1/auth/logout
router.post('/logout', authMiddleware, (req, res) => {
  res.json({ code: 0, message: 'success', data: null });
});

// POST /api/v1/auth/refresh
router.post('/refresh', (req, res) => {
  const { refreshToken } = req.body;
  if (!refreshToken) {
    return res.status(400).json({ code: 1001, message: 'refreshToken is required', data: null });
  }
  try {
    const payload = require('jsonwebtoken').verify(refreshToken, process.env.JWT_REFRESH_SECRET);
    const accessToken = signAccessToken(payload.userId);
    const newRefresh = signRefreshToken(payload.userId);
    res.json({ code: 0, message: 'success', data: { accessToken, refreshToken: newRefresh } });
  } catch (err) {
    return res.status(401).json({ code: 1002, message: 'Invalid refresh token', data: null });
  }
});

module.exports = router;
