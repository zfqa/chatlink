package com.chatapp.feature.chatdetail

import com.chatapp.core.model.Message

data class ChatDetailUiData(
    val messages: List<Message>,
    val peerName: String,
    val isGroup: Boolean = false,
    val senderNames: Map<String, String> = emptyMap(),
    val inputText: String = "",
)
