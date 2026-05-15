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
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebSocketManager @Inject constructor(
    private val tokenStore: TokenStore,
) {
    private var ws: WebSocket? = null
    private var isConnected = false
    private var reconnectAttempts = 0
    private val maxReconnects = 3
    private val gson = Gson()

    var onMessageReceived: ((Message) -> Unit)? = null

    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS) // no read timeout for WS
        .build()

    fun connect() {
        val token = tokenStore.getAccessToken() ?: return
        if (isConnected) return

        val wsUrl = NetworkConfig.BASE_URL
            .replace("http://", "ws://")
            .replace("https://", "wss://") + "/ws?token=$token"

        Log.d(TAG, "Connecting to WS (token=${token.take(8)}...)")

        val request = Request.Builder()
            .url(wsUrl)
            .build()

        ws = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "Connected")
                isConnected = true
                reconnectAttempts = 0
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(TAG, "Received: ${text.take(100)}")
                try {
                    val json = gson.fromJson(text, JsonObject::class.java)
                    val type = json.get("type")?.asString
                    if (type == "message:new") {
                        handleMessageNew(json.getAsJsonObject("data"))
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Parse error", e)
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "Closing: code=$code reason=$reason")
                webSocket.close(1000, null)
                handleDisconnect()
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "Closed: code=$code")
                handleDisconnect()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "Failure: ${t.message}")
                handleDisconnect()
            }
        })
    }

    fun disconnect() {
        Log.d(TAG, "Disconnecting")
        reconnectAttempts = maxReconnects // prevent reconnect
        ws?.close(1000, "logout")
        ws = null
        isConnected = false
    }

    private fun handleDisconnect() {
        isConnected = false
        if (reconnectAttempts < maxReconnects) {
            reconnectAttempts++
            val delay = (1000L * reconnectAttempts)
            Log.d(TAG, "Reconnecting in ${delay}ms (attempt $reconnectAttempts/$maxReconnects)")
            Thread {
                Thread.sleep(delay)
                if (!isConnected && tokenStore.getAccessToken() != null) {
                    connect()
                }
            }.start()
        } else {
            Log.d(TAG, "Max reconnect attempts reached")
        }
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
            Log.d(TAG, "New message: conv=${msg.conversationId} from=${msg.senderId}")
            onMessageReceived?.invoke(msg)
        } catch (e: Exception) {
            Log.e(TAG, "handleMessageNew error", e)
        }
    }

    companion object {
        private const val TAG = "WebSocket"
    }
}
