package com.tokyoc.line_client

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView

// FrameLayoutにより独自のViewを作っている
class MessageView : FrameLayout {

    // セカンダリコンストラクタ
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    var messageProfileImageView: ImageView
    var messageTextView: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.view_message, this)
        messageProfileImageView = findViewById<ImageView>(R.id.message_profile_image_view)
        messageTextView = findViewById<TextView>(R.id.message_text_view)
    }

    fun setMessage(message: Message) {
        messageTextView.text = message.content
        messageProfileImageView.setBackgroundColor(Color.RED)
    }
}
