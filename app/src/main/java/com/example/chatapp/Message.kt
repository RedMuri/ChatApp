package com.example.chatapp

import android.net.Uri

data class Message(
    val author: String? = null,
    val message: String? = null,
    val date: Long? = null,
    val urlToImage: String? = null
)
