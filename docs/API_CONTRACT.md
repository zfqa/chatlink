# ChatLink API Contract

> Mock 阶段文档。后续接入真实后端时，所有 FakeRepository 将替换为 RealRepository，
> 对应接口应严格遵守本合约定义的请求/响应格式。

---

## 1. 通用约定

### 1.1 Base URL

```
http://<host>:<port>/api/v1
```

### 1.2 认证方式

除登录和注册外，所有接口需要在 HTTP Header 中携带 Bearer Token：

```
Authorization: Bearer <access_token>
```

### 1.3 通用响应结构

**成功响应：**
```json
{
  "code": 0,
  "message": "success",
  "data": { ... }
}
```

**错误响应：**
```json
{
  "code": <error_code>,
  "message": "<error_description>",
  "data": null
}
```

**错误码定义：**
| code | HTTP Status | 含义 |
|------|------------|------|
| 0 | 200 | 成功 |
| 1001 | 400 | 参数校验失败 |
| 1002 | 401 | 未登录 / Token 过期 |
| 1003 | 403 | 无权限 |
| 1004 | 404 | 资源不存在 |
| 1005 | 409 | 资源冲突（如重复注册） |
| 5000 | 500 | 服务器内部错误 |

---

## 2. 认证模块 (Auth)

### 2.1 注册

`POST /auth/register`

**请求：**
```json
{
  "email": "user@example.com",
  "password": "123456",
  "nickname": "Alice"
}
```

**响应：**
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "user": {
      "id": "u_abc123",
      "nickname": "Alice",
      "avatarUrl": "",
      "signature": ""
    },
    "accessToken": "eyJhbGci...",
    "refreshToken": "eyJhbGci..."
  }
}
```

### 2.2 登录

`POST /auth/login`

**请求：**
```json
{
  "email": "user@example.com",
  "password": "123456"
}
```

**响应：**
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "user": {
      "id": "u_abc123",
      "nickname": "Alice",
      "avatarUrl": "",
      "signature": ""
    },
    "accessToken": "eyJhbGci...",
    "refreshToken": "eyJhbGci..."
  }
}
```

### 2.3 退出登录

`POST /auth/logout`

**请求：** 无 Body

**响应：**
```json
{
  "code": 0,
  "message": "success",
  "data": null
}
```

### 2.4 刷新 Token

`POST /auth/refresh`

**请求：**
```json
{
  "refreshToken": "eyJhbGci..."
}
```

**响应：**
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGci...",
    "refreshToken": "eyJhbGci..."
  }
}
```

---

## 3. 用户模块 (User)

### 3.1 获取当前用户信息

`GET /users/me`

**响应：**
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "u_abc123",
    "nickname": "Alice",
    "avatarUrl": "https://cdn.example.com/avatar.jpg",
    "signature": "Hello World"
  }
}
```

### 3.2 更新当前用户信息

`PUT /users/me`

**请求：**
```json
{
  "nickname": "NewName",
  "signature": "New Signature"
}
```

**响应：**
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "u_abc123",
    "nickname": "NewName",
    "avatarUrl": "https://cdn.example.com/avatar.jpg",
    "signature": "New Signature"
  }
}
```

### 3.3 上传头像

`POST /users/me/avatar`

**请求：** `multipart/form-data`，字段 `file`

**响应：**
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "avatarUrl": "https://cdn.example.com/avatar_new.jpg"
  }
}
```

### 3.4 搜索用户

`GET /users/search?q={query}`

**响应：**
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "users": [
      {
        "id": "u_def456",
        "nickname": "Bob",
        "avatarUrl": "",
        "signature": "Hello"
      }
    ]
  }
}
```

### 3.5 根据 ID 获取用户

`GET /users/{userId}`

**响应：**
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "u_def456",
    "nickname": "Bob",
    "avatarUrl": "",
    "signature": "Hello"
  }
}
```

---

## 4. 联系人模块 (Contact)

### 4.1 获取联系人列表

`GET /contacts`

**响应：**
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "contacts": [
      {
        "user": {
          "id": "u_def456",
          "nickname": "Bob",
          "avatarUrl": "",
          "signature": "Hello"
        },
        "isOnline": true,
        "remark": "",
        "pinyinInitial": "B"
      }
    ]
  }
}
```

---

## 5. 好友申请模块 (Friend Request)

### 5.1 发送好友申请

`POST /friends/requests`

**请求：**
```json
{
  "toUserId": "u_def456"
}
```

**响应：**
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "request": {
      "id": "fr_001",
      "fromUser": {
        "id": "u_abc123",
        "nickname": "Alice",
        "avatarUrl": "",
        "signature": ""
      },
      "toUserId": "u_def456",
      "timestamp": 1700000000000,
      "status": "PENDING"
    }
  }
}
```

### 5.2 获取收到的好友申请列表

`GET /friends/requests?status=PENDING`

**响应：**
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "requests": [
      {
        "id": "fr_001",
        "fromUser": {
          "id": "u_def456",
          "nickname": "Bob",
          "avatarUrl": "",
          "signature": ""
        },
        "toUserId": "u_abc123",
        "timestamp": 1700000000000,
        "status": "PENDING"
      }
    ]
  }
}
```

### 5.3 接受好友申请

`PUT /friends/requests/{requestId}/accept`

**响应：**
```json
{
  "code": 0,
  "message": "success",
  "data": null
}
```

### 5.4 拒绝好友申请

`PUT /friends/requests/{requestId}/reject`

**响应：**
```json
{
  "code": 0,
  "message": "success",
  "data": null
}
```

---

## 6. 会话模块 (Conversation)

### 6.1 获取会话列表

`GET /conversations`

**响应：**
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "conversations": [
      {
        "id": "conv_1",
        "peer": {
          "id": "u_def456",
          "nickname": "Bob",
          "avatarUrl": "",
          "signature": ""
        },
        "lastMessage": "Hello!",
        "lastMessageTime": 1700000000000,
        "unreadCount": 2,
        "isPinned": false,
        "isMuted": false
      }
    ]
  }
}
```

### 6.2 获取或创建单聊会话

`POST /conversations/direct`

**请求：**
```json
{
  "peerId": "u_def456"
}
```

**响应：**
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "conversation": {
      "id": "conv_1",
      "peer": {
        "id": "u_def456",
        "nickname": "Bob",
        "avatarUrl": "",
        "signature": ""
      },
      "lastMessage": "",
      "lastMessageTime": 1700000000000,
      "unreadCount": 0,
      "isPinned": false,
      "isMuted": false
    }
  }
}
```

---

## 7. 消息模块 (Message)

### 7.1 获取消息列表

`GET /conversations/{conversationId}/messages?before={timestamp}&limit=50`

**参数：**
- `before`（可选）：分页游标，返回此时间戳之前的消息
- `limit`（可选）：每页条数，默认 50

**响应：**
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "messages": [
      {
        "id": "m_001",
        "conversationId": "conv_1",
        "senderId": "u_def456",
        "content": "Hello!",
        "type": "TEXT",
        "timestamp": 1700000000000,
        "status": "SENT"
      }
    ],
    "hasMore": true
  }
}
```

### 7.2 发送文本消息

`POST /conversations/{conversationId}/messages`

**请求：**
```json
{
  "content": "Hello Bob!",
  "type": "TEXT"
}
```

**响应：**
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "message": {
      "id": "m_002",
      "conversationId": "conv_1",
      "senderId": "u_abc123",
      "content": "Hello Bob!",
      "type": "TEXT",
      "timestamp": 1700000000000,
      "status": "SENT"
    }
  }
}
```

---

## 8. 数据模型对照

### 8.1 User
| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 用户唯一标识 |
| nickname | String | 昵称 |
| avatarUrl | String | 头像 URL |
| signature | String | 个性签名 |

### 8.2 Contact
| 字段 | 类型 | 说明 |
|------|------|------|
| user | User | 联系人用户信息 |
| isOnline | Boolean | 是否在线 |
| remark | String | 备注名 |
| pinyinInitial | String | 昵称拼音首字母（客户端生成） |

### 8.3 Conversation
| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 会话唯一标识 |
| peer | User | 对方用户 |
| lastMessage | String | 最后一条消息内容 |
| lastMessageTime | Long | 最后消息时间戳 (ms) |
| unreadCount | Int | 未读消息数 |
| isPinned | Boolean | 是否置顶 |
| isMuted | Boolean | 是否免打扰 |

### 8.4 Message
| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 消息唯一标识 |
| conversationId | String | 所属会话 ID |
| senderId | String | 发送者 ID |
| content | String | 消息内容 |
| type | MessageType | TEXT / IMAGE |
| timestamp | Long | 发送时间戳 (ms) |
| status | MessageStatus | SENDING / SENT / FAILED |

### 8.5 FriendRequest
| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 申请唯一标识 |
| fromUser | User | 申请人用户信息 |
| toUserId | String | 被申请人 ID |
| timestamp | Long | 申请时间戳 (ms) |
| status | FriendRequestStatus | PENDING / ACCEPTED / REJECTED |

---

## 9. Repository 接口与 API 映射

| Repository 方法 | API 接口 |
|----------------|----------|
| `AuthRepository.login()` | `POST /auth/login` |
| `AuthRepository.register()` | `POST /auth/register` |
| `AuthRepository.logout()` | `POST /auth/logout` |
| `AuthRepository.isLoggedIn()` | 客户端检查本地 Token |
| `AuthRepository.checkSavedSession()` | 客户端读取本地 Token，可选调用 `GET /users/me` 校验 |
| `UserRepository.getCurrentUser()` | `GET /users/me` |
| `ContactRepository.getContacts()` | `GET /contacts` |
| `ContactRepository.getContact()` | `GET /users/{userId}` + 组合 |
| `ContactRepository.refreshContacts()` | `GET /contacts` |
| `FriendRepository.getPendingRequests()` | `GET /friends/requests?status=PENDING` |
| `FriendRepository.searchUsers()` | `GET /users/search?q=` |
| `FriendRepository.searchAllUsers()` | `GET /users/search?q=`（空 query） |
| `FriendRepository.sendRequest()` | `POST /friends/requests` |
| `FriendRepository.acceptRequest()` | `PUT /friends/requests/{id}/accept` |
| `FriendRepository.rejectRequest()` | `PUT /friends/requests/{id}/reject` |
| `FriendRepository.isFriend()` | 客户端缓存判断 |
| `ConversationRepository.getConversations()` | `GET /conversations` |
| `ConversationRepository.getMessages()` | `GET /conversations/{id}/messages` |
| `ConversationRepository.sendMessage()` | `POST /conversations/{id}/messages` |
| `ConversationRepository.getOrCreateConversationForPeer()` | `POST /conversations/direct` |

---

## 10. WebSocket 实时消息

### 连接地址

```
ws://<host>:<port>/ws?token=<access_token>
```

- Token 通过 URL query 参数传递，使用 `JWT_ACCESS_SECRET` 校验
- 缺少 token 关闭码 `4001`，token 无效/过期关闭码 `4002`
- 一个用户可持有多个连接（多设备）

### 服务端事件

#### `connected` — 连接成功后立即推送

```json
{
  "type": "connected",
  "data": {
    "userId": "u_abc123",
    "serverTime": 1710000000000
  }
}
```

#### `message:new` — 会话中有新消息时推送给会话内其他在线成员

```json
{
  "type": "message:new",
  "data": {
    "id": "m_003",
    "conversationId": "conv_1",
    "senderId": "u_def456",
    "content": "Hi!",
    "type": "text",
    "createdAt": 1710000001000
  }
}
```

触发方式：通过 `POST /conversations/:id/messages` 发送消息后自动推送。

### 行为说明

- 消息发送者不会收到自己的推送（跳过 senderId）
- 离线用户不会收到推送（无消息队列），上线后通过 HTTP 拉取历史消息
- 服务端每 30 秒发送 ping 心跳，客户端需以 pong 响应
- 无响应的连接将被终止

### 客户端当前限制

当前 Android 客户端尚未接入 WebSocket，消息通过 HTTP 轮询获取。
WebSocket 客户端接入将在后续阶段实现。
