package com.tokyoc.line_client

import android.widget.FrameLayout
import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.TextView
import android.view.LayoutInflater
import android.graphics.Color

// FrameLayoutにより独自のViewを作っている
class MemberView : FrameLayout {

    // セカンダリコンストラクタ
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    var profileImageView: ImageView? = null
    var userNameTextView: TextView? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_member, this)
        profileImageView = findViewById<ImageView>(R.id.profile_image_view)
        userNameTextView = findViewById<TextView>(R.id.user_name_view)
    }

    fun setMember(member: Member) {
        userNameTextView?.text = member.name
        profileImageView?.setBackgroundColor(Color.RED)
    }
}