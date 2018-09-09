package com.tokyoc.line_client

import com.google.gson.annotations.SerializedName

// PinEventデータのFormat
open class PinEvent {
    val type: String = ""
    @SerializedName("PIN")
    val pin: Int = 0
    val person: String = ""
}