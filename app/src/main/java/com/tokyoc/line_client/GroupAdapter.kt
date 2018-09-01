package com.tokyoc.line_client

import android.content.Context
import android.widget.BaseAdapter
import android.view.View
import android.view.ViewGroup

class GroupListAdapter(private val context: Context) : BaseAdapter() {

    var groups: List<Group> = emptyList()

    override fun getCount(): Int = groups.size
    override fun getItem(position: Int): Any? = groups[position]
    override fun getItemId(position: Int): Long = 0
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View =
            ((convertView as? GroupView) ?: GroupView(context)).apply { setGroup(groups[position]) }
}