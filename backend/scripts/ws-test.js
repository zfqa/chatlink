#!/usr/bin/env node
/**
 * Minimal WebSocket test for ChatLink.
 *
 * Usage:
 *   node scripts/ws-test.js <JWT_TOKEN>
 */

const WebSocket = require('ws');

const token = process.argv[2];
if (!token) {
  console.error('Usage: node scripts/ws-test.js <JWT_TOKEN>');
  process.exit(1);
}

const url = `ws://localhost:3000/ws?token=${token}`;
console.log('[TEST] Connecting to', url.slice(0, 50) + '...');

const ws = new WebSocket(url);

ws.on('open', () => {
  console.log('[TEST] Connected to WebSocket server');
});

ws.on('message', (data, isBinary) => {
  const raw = data.toString();
  console.log('[TEST] Raw message received, length=', raw.length, 'isBinary=', isBinary);
  console.log('[TEST] Raw content:', raw);
  try {
    const msg = JSON.parse(raw);
    console.log('[TEST] Parsed:', JSON.stringify(msg, null, 2));
  } catch (e) {
    console.error('[TEST] JSON parse error:', e.message);
  }
});

ws.on('close', (code, reason) => {
  console.log(`[TEST] Disconnected: code=${code} reason=${reason.toString()}`);
});

ws.on('error', (err) => {
  console.error('[TEST] Error:', err.message);
});

ws.on('unexpected-response', (req, res) => {
  console.error('[TEST] Unexpected response:', res.statusCode);
});

console.log('[TEST] Waiting for messages... (Ctrl+C to exit)');
