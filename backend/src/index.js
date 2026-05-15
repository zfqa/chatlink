require('dotenv').config({ path: require('path').resolve(__dirname, '..', '.env') });
const http = require('http');
const express = require('express');
const cors = require('cors');
const { initWebSocket } = require('./ws');

const authRoutes = require('./routes/auth');
const userRoutes = require('./routes/users');
const contactRoutes = require('./routes/contacts');
const friendRoutes = require('./routes/friends');
const conversationRoutes = require('./routes/conversations');
const messageRoutes = require('./routes/messages');

const app = express();
app.use(cors());
app.use(express.json());

app.use('/api/v1/auth', authRoutes);
app.use('/api/v1/users', userRoutes);
app.use('/api/v1/contacts', contactRoutes);
app.use('/api/v1/friends', friendRoutes);
app.use('/api/v1/conversations', conversationRoutes);
app.use('/api/v1/conversations', messageRoutes);

app.get('/api/v1/health', (req, res) => {
  res.json({ code: 0, message: 'ChatLink backend is running', data: null });
});

app.use((req, res) => {
  res.status(404).json({ code: 1004, message: 'Route not found', data: null });
});

const PORT = process.env.PORT || 3000;
const server = http.createServer(app);

// Initialize WebSocket on the same server
initWebSocket(server);

server.listen(PORT, () => {
  console.log('ChatLink backend running on port ' + PORT);
});
