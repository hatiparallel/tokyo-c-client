package com.tokyoc.line_client

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.realm.OrderedRealmCollection
import io.realm.RealmBaseAdapter

class MemberListAdapter2(data: OrderedRealmCollection<Member2>) : RealmBaseAdapter<Member2>(data) {

    var members0: MutableList<Member2> = mutableListOf()

    inner class ViewHolder(cell: View) {
        val username = cell.findViewById<TextView>(R.id.user_name_view)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val viewHolder: ViewHolder

        when (convertView) {
            null -> {
                val inflater = LayoutInflater.from(parent?.context)
                view = inflater.inflate(R.layout.view_member, parent, false)
                viewHolder = ViewHolder(view)
                view.tag = viewHolder
            }
            else -> {
                view = convertView
                viewHolder = view.tag as MemberListAdapter2.ViewHolder
            }
        }

        adapterData?.run {
            val member = get(position)
            viewHolder.username.text = member.name
        }
        return view
    }
}

