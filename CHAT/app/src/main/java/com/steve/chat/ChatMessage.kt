package com.steve.chat

data class ChatMessage(
    val packageName: String,
    val contact: String,
    val message: String,
    val timestamp: Long
)
