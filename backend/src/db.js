const Database = require('better-sqlite3');
const path = require('path');
const fs = require('fs');

const dbPath = process.env.DATABASE_PATH || './data/chatlink.db';
const absPath = path.resolve(__dirname, '..', dbPath);
fs.mkdirSync(path.dirname(absPath), { recursive: true });

const db = new Database(absPath);
db.pragma('journal_mode = WAL');

const schema = [
  "CREATE TABLE IF NOT EXISTS users (id TEXT PRIMARY KEY, email TEXT UNIQUE NOT NULL, password_hash TEXT NOT NULL, nickname TEXT NOT NULL DEFAULT '', avatar_url TEXT NOT NULL DEFAULT '', signature TEXT NOT NULL DEFAULT '', created_at INTEGER NOT NULL DEFAULT (strftime('%s','now') * 1000), updated_at INTEGER NOT NULL DEFAULT (strftime('%s','now') * 1000))",
  "CREATE TABLE IF NOT EXISTS contacts (user_id TEXT NOT NULL, contact_id TEXT NOT NULL, remark TEXT NOT NULL DEFAULT '', created_at INTEGER NOT NULL DEFAULT (strftime('%s','now') * 1000), PRIMARY KEY (user_id, contact_id))",
  "CREATE TABLE IF NOT EXISTS friend_requests (id TEXT PRIMARY KEY, from_user_id TEXT NOT NULL, to_user_id TEXT NOT NULL, status TEXT NOT NULL DEFAULT 'PENDING', created_at INTEGER NOT NULL DEFAULT (strftime('%s','now') * 1000))",
  "CREATE TABLE IF NOT EXISTS conversations (id TEXT PRIMARY KEY, type TEXT NOT NULL DEFAULT 'DIRECT', created_at INTEGER NOT NULL DEFAULT (strftime('%s','now') * 1000))",
  "CREATE TABLE IF NOT EXISTS conversation_participants (conversation_id TEXT NOT NULL, user_id TEXT NOT NULL, role TEXT NOT NULL DEFAULT 'member', is_pinned INTEGER NOT NULL DEFAULT 0, is_muted INTEGER NOT NULL DEFAULT 0, unread_count INTEGER NOT NULL DEFAULT 0, PRIMARY KEY (conversation_id, user_id))",
  "CREATE TABLE IF NOT EXISTS groups (conversation_id TEXT PRIMARY KEY, name TEXT NOT NULL DEFAULT '', avatar_url TEXT NOT NULL DEFAULT '', owner_id TEXT NOT NULL, created_at INTEGER NOT NULL DEFAULT (strftime('%s','now') * 1000))",
  "CREATE TABLE IF NOT EXISTS messages (id TEXT PRIMARY KEY, conversation_id TEXT NOT NULL, sender_id TEXT NOT NULL, content TEXT NOT NULL DEFAULT '', type TEXT NOT NULL DEFAULT 'TEXT', status TEXT NOT NULL DEFAULT 'SENT', created_at INTEGER NOT NULL DEFAULT (strftime('%s','now') * 1000))",
  "CREATE INDEX IF NOT EXISTS idx_messages_conv ON messages(conversation_id, created_at)",
  "CREATE INDEX IF NOT EXISTS idx_fr_to ON friend_requests(to_user_id, status)",
  "CREATE INDEX IF NOT EXISTS idx_contacts_user ON contacts(user_id)",
];

// Migration: add 'role' column to conversation_participants if missing
try {
  const cols = db.prepare("PRAGMA table_info(conversation_participants)").all();
  if (!cols.some(c => c.name === 'role')) {
    db.exec("ALTER TABLE conversation_participants ADD COLUMN role TEXT NOT NULL DEFAULT 'member'");
  }
} catch (e) {
  // table might not exist yet on first run, that's fine
}

schema.forEach(sql => db.exec(sql));

module.exports = db;
