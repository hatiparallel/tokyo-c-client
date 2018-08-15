package com.tokyoc.line_client

import android.content.Context
import android.widget.BaseAdapter
import android.view.View
import android.view.ViewGroup

class MessageListAdapter(private val context: Context) : BaseAdapter() {

    var messages: MutableList<Message> = mutableListOf()

    override fun getCount(): Int = messages.size
    override fun getItem(position: Int): Any? = messages[position]
    override fun getItemId(position: Int): Long = 0
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View =
            ((convertView as? MessageView) ?: MessageView(context)).apply { setMessage(messages[position]) }
}

