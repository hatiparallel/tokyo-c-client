package com.tokyoc.line_client

import android.widget.FrameLayout
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.view.LayoutInflater
import android.graphics.Color

// FrameLayoutにより独自のViewを作っている
class GroupMessageView : FrameLayout {

    // セカンダリコンストラクタ
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    var authorProfileImageView: ImageView
    var authorNameTextView: TextView
    var messageTextView: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.view_group_message, this)
        authorProfileImageView = findViewById<ImageView>(R.id.author_profile_image_view)
        authorNameTextView = findViewById<TextView>(R.id.author_name_text_view)
        messageTextView = findViewById<TextView>(R.id.message_text_view)
    }

    fun setMessage(message: Message) {
        authorProfileImageView.setBackgroundColor(Color.GREEN)
        authorNameTextView.text = "author"
        messageTextView.text = message.content
    }
}
