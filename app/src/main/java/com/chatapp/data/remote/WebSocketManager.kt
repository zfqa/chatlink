package com.chatapp.data.remote

import android.util.Log
import com.chatapp.core.model.Message
import com.chatapp.core.model.MessageStatus
import com.chatapp.core.model.MessageType
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebSocketManager @Inject constructor(
    private val tokenStore: TokenStore,
) {
    private enum class State { DISCONNECTED, CONNECTING, CONNECTED, DISCONNECTING }

    private var ws: WebSocket? = null
    private var state = State.DISCONNECTED
    private var reconnectAttempts = 0
    private val maxReconnects = 3
    private val gson = Gson()
    private val reconnectExecutor = Executors.newSingleThreadScheduledExecutor()

    var onMessageReceived: ((Message) -> Unit)? = null

    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    @Synchronized
    fun connect() {
        if (state == State.CONNECTED || state == State.CONNECTING) return
        val token = tokenStore.getAccessToken() ?: return

        state = State.CONNECTING
        val wsUrl = NetworkConfig.BASE_URL
            .replace("http://", "ws://")
            .replace("https://", "wss://") + "/ws?token=$token"

        Log.d(TAG, "Connecting...")

        val request = Request.Builder().url(wsUrl).build()

        ws = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                synchronized(this@WebSocketManager) {
                    if (state == State.DISCONNECTING || state == State.DISCONNECTED) {
                        webSocket.close(1000, "cancelled")
                        return
                    }
                    state = State.CONNECTED
                    reconnectAttempts = 0
                }
                Log.d(TAG, "Connected")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(TAG, "Received: ${text.take(80)}")
                try {
                    val json = gson.fromJson(text, JsonObject::class.java)
                    val type = json.get("type")?.asString
                    when (type) {
                        "message:new" -> handleMessageNew(json.getAsJsonObject("data"))
                        "connected" -> Log.d(TAG, "Server confirmed connection")
                        else -> Log.d(TAG, "Event: $type")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Parse error: ${e.message}")
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
                handleDisconnect()
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                handleDisconnect()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "Failure: ${t.message}")
                handleDisconnect()
            }
        })
    }

    @Synchronized
    fun disconnect() {
        Log.d(TAG, "Disconnecting")
        state = State.DISCONNECTING
        reconnectAttempts = maxReconnects
        ws?.close(1000, "logout")
        ws = null
        state = State.DISCONNECTED
    }

    @Synchronized
    private fun handleDisconnect() {
        if (state == State.DISCONNECTING || state == State.DISCONNECTED) return
        state = State.DISCONNECTED
        ws = null

        if (reconnectAttempts >= maxReconnects) {
            Log.d(TAG, "Max reconnect attempts reached")
            return
        }
        if (tokenStore.getAccessToken() == null) {
            Log.d(TAG, "No token, skipping reconnect")
            return
        }

        reconnectAttempts++
        val delay = 1000L * reconnectAttempts
        Log.d(TAG, "Reconnecting in ${delay}ms (attempt $reconnectAttempts/$maxReconnects)")
        reconnectExecutor.schedule({
            if (state == State.DISCONNECTED && tokenStore.getAccessToken() != null) {
                connect()
            }
        }, delay, TimeUnit.MILLISECONDS)
    }

    private fun handleMessageNew(data: JsonObject) {
        try {
            val msg = Message(
                id = data.get("id").asString,
                conversationId = data.get("conversationId").asString,
                senderId = data.get("senderId").asString,
                content = data.get("content").asString,
                type = when (data.get("type")?.asString) {
                    "image" -> MessageType.IMAGE
                    else -> MessageType.TEXT
                },
                timestamp = data.get("createdAt").asLong,
                status = MessageStatus.SENT,
            )
            Log.d(TAG, "message:new conv=${msg.conversationId}")
            onMessageReceived?.invoke(msg)
        } catch (e: Exception) {
            Log.e(TAG, "handleMessageNew error: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "WebSocket"
    }
}
