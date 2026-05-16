const express = require('express');
const db = require('../db');
const { authMiddleware } = require('../middleware/auth');

const router = express.Router();

// GET /api/v1/contacts
router.get('/', authMiddleware, (req, res) => {
  const rows = db.prepare('SELECT u.id, u.email, u.nickname, u.avatar_url as avatarUrl, u.signature, c.remark FROM contacts c JOIN users u ON u.id = c.contact_id WHERE c.user_id = ?').all(req.userId);
  const contacts = rows.map(r => ({ user: { id: r.id, email: r.email || '', nickname: r.nickname, avatarUrl: r.avatarUrl, signature: r.signature }, isOnline: false, remark: r.remark, pinyinInitial: r.nickname.charAt(0).toUpperCase() }));
  res.json({ code: 0, message: 'success', data: { contacts } });
});

module.exports = router;
