package com.tokyoc.line_client

import android.content.Context
import android.widget.BaseAdapter
import android.view.View
import android.view.ViewGroup

class MemberListAdapter(private val context: Context) : BaseAdapter() {

    var members: List<Member> = emptyList()

    override fun getCount(): Int = members.size
    override fun getItem(position: Int): Any? = members[position]
    override fun getItemId(position: Int): Long = 0
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View =
            ((convertView as? MemberView) ?: MemberView(context)).apply { setMember(members[position]) }
}