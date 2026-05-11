package com.chatapp.data.repository

import com.chatapp.core.model.*

import com.chatapp.core.model.FriendRequest
import com.chatapp.core.model.FriendRequestStatus

/** Central mock data store for all fakes. */
object FakeData {

    val me = User(id = "u_me", nickname = "我", avatarUrl = "", signature = "这个人很懒")

    val alice = User(id = "u_alice", nickname = "Alice", avatarUrl = "", signature = "今天天气真好")
    val bob = User(id = "u_bob", nickname = "Bob", avatarUrl = "", signature = "努力写代码")
    val carol = User(id = "u_carol", nickname = "Carol", avatarUrl = "", signature = "旅行爱好者")
    val dave = User(id = "u_dave", nickname = "Dave", avatarUrl = "", signature = "咖啡续命中")
    val eve = User(id = "u_eve", nickname = "Eve", avatarUrl = "", signature = "读书使人明智")
    val frank = User(id = "u_frank", nickname = "Frank", avatarUrl = "", signature = "健身达人")

    val allUsers = listOf(alice, bob, carol, dave, eve, frank)

    // Mutable contacts list so new friends can be added
    val _contacts = mutableListOf(
        Contact(user = alice, isOnline = true, pinyinInitial = "A"),
        Contact(user = carol, isOnline = true, pinyinInitial = "C"),
        Contact(user = eve, isOnline = false, pinyinInitial = "E"),
    )
    val contacts: List<Contact> get() = _contacts

    val friendIds = mutableSetOf("u_alice", "u_carol", "u_eve")

    private val _friendRequests = mutableListOf<FriendRequest>()
    val friendRequests: List<FriendRequest> get() = _friendRequests
    private var frCounter = 100

    init {
        // Pre-populate some incoming friend requests for testing
        addFriendRequest(bob, me.id)
        addFriendRequest(dave, me.id)
        addFriendRequest(frank, me.id)
    }

    fun addFriendRequest(fromUser: User, toUserId: String): FriendRequest {
        frCounter++
        val req = FriendRequest(
            id = "fr_$frCounter",
            fromUser = fromUser,
            toUserId = toUserId,
            timestamp = System.currentTimeMillis(),
        )
        _friendRequests.add(req)
        return req
    }

    fun acceptFriendRequest(requestId: String) {
        val idx = _friendRequests.indexOfFirst { it.id == requestId }
        if (idx >= 0) {
            val req = _friendRequests[idx]
            _friendRequests[idx] = req.copy(status = FriendRequestStatus.ACCEPTED)
            val user = req.fromUser
            if (_contacts.none { it.user.id == user.id }) {
                _contacts.add(Contact(user = user, isOnline = false, pinyinInitial = user.nickname.first().toString().uppercase()))
            }
            friendIds.add(user.id)
        }
    }

    fun rejectFriendRequest(requestId: String) {
        val idx = _friendRequests.indexOfFirst { it.id == requestId }
        if (idx >= 0) {
            val req = _friendRequests[idx]
            _friendRequests[idx] = req.copy(status = FriendRequestStatus.REJECTED)
        }
    }

    private val now = System.currentTimeMillis()

    private val _conversations = mutableListOf(
        Conversation(id = "conv_1", peer = alice, lastMessage = "明天见！", lastMessageTime = now - 120_000, unreadCount = 2),
        Conversation(id = "conv_3", peer = carol, lastMessage = "[图片]", lastMessageTime = now - 3_600_000, unreadCount = 1),
        Conversation(id = "conv_5", peer = eve, lastMessage = "那本书推荐给你", lastMessageTime = now - 172_800_000, unreadCount = 0, isPinned = true),
    )
    val conversations: List<Conversation> get() = _conversations

    private var convCounter = 100

    fun getOrCreateConversation(peerId: String): Conversation {
        _conversations.find { it.peer.id == peerId }?.let { return it }
        convCounter++
        val peer = allUsers.find { it.id == peerId } ?: me
        val conv = Conversation(
            id = "conv_$convCounter",
            peer = peer,
            lastMessage = "",
            lastMessageTime = System.currentTimeMillis(),
        )
        _conversations.add(0, conv)
        return conv
    }

    val messagesMap = mutableMapOf(
        "conv_1" to mutableListOf(
            Message(id = "m1", conversationId = "conv_1", senderId = "u_alice", content = "你好呀", timestamp = now - 300_000),
            Message(id = "m2", conversationId = "conv_1", senderId = "u_me", content = "嗨，最近怎么样？", timestamp = now - 280_000),
            Message(id = "m3", conversationId = "conv_1", senderId = "u_alice", content = "挺好的，明天有空吗？", timestamp = now - 200_000),
            Message(id = "m4", conversationId = "conv_1", senderId = "u_me", content = "有空啊，什么事？", timestamp = now - 150_000),
            Message(id = "m5", conversationId = "conv_1", senderId = "u_alice", content = "明天见！", timestamp = now - 120_000),
        ),
        "conv_3" to mutableListOf(
            Message(id = "m9", conversationId = "conv_3", senderId = "u_carol", content = "看我拍的照片", timestamp = now - 3_700_000),
            Message(id = "m10", conversationId = "conv_3", senderId = "u_me", content = "好漂亮！在哪拍的？", timestamp = now - 3_650_000),
            Message(id = "m11", conversationId = "conv_3", senderId = "u_carol", content = "[图片]", timestamp = now - 3_600_000),
        ),
        "conv_5" to mutableListOf(
            Message(id = "m13", conversationId = "conv_5", senderId = "u_eve", content = "那本书推荐给你", timestamp = now - 172_800_000),
        ),
    )

    private var msgCounter = 100

    fun addMessage(conversationId: String, content: String): Message {
        msgCounter++
        val msg = Message(
            id = "m_",
            conversationId = conversationId,
            senderId = me.id,
            content = content,
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.SENT,
        )
        messagesMap.getOrPut(conversationId) { mutableListOf() }.add(msg)
        return msg
    }
}
