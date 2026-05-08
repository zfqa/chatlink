package com.chatapp.data.local

import android.content.Context
import com.chatapp.core.model.Message
import com.chatapp.core.model.MessageStatus
import com.chatapp.core.model.MessageType
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persists chat messages as JSON files in app internal storage.
 * Each conversation gets its own file: filesDir/messages/conv_xxx.json
 */
@Singleton
class MessageStorage @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val dir: File
        get() = File(context.filesDir, "messages").also { it.mkdirs() }

    fun loadMessages(conversationId: String): List<Message> {
        val file = File(dir, "$conversationId.json")
        if (!file.exists()) return emptyList()
        return try {
            val json = JSONArray(file.readText())
            (0 until json.length()).mapNotNull { i ->
                parseMessage(json.getJSONObject(i))
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveMessages(conversationId: String, messages: List<Message>) {
        val file = File(dir, "$conversationId.json")
        val json = JSONArray()
        for (msg in messages) {
            json.put(toJson(msg))
        }
        file.writeText(json.toString())
    }

    private fun toJson(msg: Message): JSONObject = JSONObject().apply {
        put("id", msg.id)
        put("conversationId", msg.conversationId)
        put("senderId", msg.senderId)
        put("content", msg.content)
        put("type", msg.type.name)
        put("timestamp", msg.timestamp)
        put("status", msg.status.name)
    }

    private fun parseMessage(obj: JSONObject): Message? {
        return try {
            Message(
                id = obj.getString("id"),
                conversationId = obj.getString("conversationId"),
                senderId = obj.getString("senderId"),
                content = obj.getString("content"),
                type = try { MessageType.valueOf(obj.getString("type")) } catch (_: Exception) { MessageType.TEXT },
                timestamp = obj.getLong("timestamp"),
                status = try { MessageStatus.valueOf(obj.getString("status")) } catch (_: Exception) { MessageStatus.SENT },
            )
        } catch (e: Exception) {
            null
        }
    }
}
