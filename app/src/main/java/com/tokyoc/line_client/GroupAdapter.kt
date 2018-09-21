package com.tokyoc.line_client

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import io.realm.OrderedRealmCollection
import io.realm.RealmBaseAdapter

class GroupListAdapter(data: OrderedRealmCollection<Group>) : RealmBaseAdapter<Group>(data) {

    inner class ViewHolder(cell: View) {
        val groupName = cell.findViewById<TextView>(R.id.group_name_view)
        val groupImage = cell.findViewById<ImageView>(R.id.group_image_view)
        val groupLatest = cell.findViewById<TextView>(R.id.group_latest_view)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val viewHolder: ViewHolder

        when (convertView) {
            null -> {
                val inflater = LayoutInflater.from(parent?.context)
                view = inflater.inflate(R.layout.view_group, parent, false)
                viewHolder = ViewHolder(view)
                view.tag = viewHolder
            }
            else -> {
                view = convertView
                viewHolder = view.tag as GroupListAdapter.ViewHolder
            }
        }

        adapterData?.run {
            val member = get(position)
            member.display(viewHolder.groupName, viewHolder.groupImage, viewHolder.groupLatest)
        }
        return view
    }
}