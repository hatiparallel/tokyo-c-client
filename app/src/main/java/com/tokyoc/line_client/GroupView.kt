package com.tokyoc.line_client

import android.widget.FrameLayout
import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.TextView
import android.view.LayoutInflater
import android.graphics.Color

// FrameLayoutにより独自のViewを作っている
class GroupView : FrameLayout {

    // セカンダリコンストラクタ
    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    var groupImageView: ImageView
    var groupNameTextView: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.view_group, this)
        groupImageView = findViewById<ImageView>(R.id.group_image_view)
        groupNameTextView = findViewById<TextView>(R.id.group_name_view)
    }

    fun setGroup(group: Group) {
        groupImageView.setImageResource(R.drawable.img001)
        groupNameTextView.text = group.name
    }
}
