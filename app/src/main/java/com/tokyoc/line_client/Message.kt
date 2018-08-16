package com.tokyoc.line_client

import java.util.*

// MessageデータのFormat
data class Message(val textmessage: String, val sender: Int, val date: Date)
//senderは仮置き。実際はMember型。sender=0なら自分、1なら相手。