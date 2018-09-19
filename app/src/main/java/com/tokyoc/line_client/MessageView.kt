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

    var authorProfileImageView: ImageView
    var authorNameTextView: TextView
    var messageTextView: TextView
    var messagePostedAt: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.view_message, this)
        authorProfileImageView = findViewById<ImageView>(R.id.author_profile_image_view)
        authorNameTextView = findViewById<TextView>(R.id.author_name_text_view)
        messageTextView = findViewById<TextView>(R.id.message_text_view)
        messagePostedAt = findViewById<TextView>(R.id.message_send_time)
    }

    fun setMessage(message: Message) {
        authorProfileImageView.setBackgroundColor(Color.GREEN)
        authorNameTextView.text = "author"
        messageTextView.text = message.content
        messagePostedAt.text = message.postedAt.toString()
    }
}
