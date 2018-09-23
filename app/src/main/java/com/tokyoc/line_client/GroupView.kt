package com.tokyoc.line_client

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView

// FrameLayoutにより独自のViewを作っている
class GroupView : FrameLayout {

    // セカンダリコンストラクタ
    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    var groupImageView: ImageView
    var groupNameTextView: TextView
    var groupLatestTextView: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.view_group, this)
        groupImageView = findViewById<ImageView>(R.id.group_image_view)
        groupNameTextView = findViewById<TextView>(R.id.group_name_view)
        groupLatestTextView = findViewById<TextView>(R.id.group_latest_view)
    }
}
