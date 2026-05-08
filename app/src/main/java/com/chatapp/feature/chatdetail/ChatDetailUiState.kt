package com.chatapp.feature.chatdetail

import com.chatapp.core.model.Message

data class ChatDetailUiData(
    val messages: List<Message>,
    val peerName: String,
    val inputText: String = "",
)
