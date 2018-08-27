package com.tokyoc.line_client

import java.util.*

// MessageデータのFormat
data class Message(
        val id: Int = 0,
        val channel: Int = 0,
        val author: Int = 0,
        val isEvent: Int = 0,
        val postedAt: Date = Date(),
        val content: String)