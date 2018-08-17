package com.tokyoc.line_client

import java.util.*

// MessageデータのFormat
data class TestMessage(val Text: String)
//senderは仮置き。実際はMember型。sender=0なら自分、1なら相手。