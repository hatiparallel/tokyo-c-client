package com.tokyoc.line_client

import java.util.*

// MessageデータのFormat
data class Message(val id: Long = 0, val channel: Long = 0, val author: Long = 0, val isevent: Int = 0, val postedat: Date = Date(), val content: String)
//senderは仮置き。実際はMember型。sender=0なら自分、1なら相手。