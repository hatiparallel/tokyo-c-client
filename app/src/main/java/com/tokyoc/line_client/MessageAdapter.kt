package com.tokyoc.line_client

import android.content.Context
import android.view.LayoutInflater
import android.widget.BaseAdapter
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import io.realm.OrderedRealmCollection
import io.realm.RealmBaseAdapter

class MessageListAdapter(data: OrderedRealmCollection<Message>) : RealmBaseAdapter<Message>(data) {

    // var messages: MutableList<Message> = mutableListOf()

    inner class ViewHolder(cell: View) {
//        val imageview = cell.findViewById<ImageView>(R.id.message_profile_image_view)
//        val bubbleview = cell.findViewById<com.daasuu.bl.BubbleLayout>(R.id.bubble_view)
        val textmessage = cell.findViewById<TextView>(android.R.id.text1)
    }

    //override fun getCount(): Int = messages.size
    //override fun getItem(position: Int): Any? = messages[position]
    //override fun getItemId(position: Int): Long = 0
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val viewHolder: ViewHolder

        when (convertView) {
            null -> {
                val inflater = LayoutInflater.from(parent?.context)
                view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false)
                viewHolder = ViewHolder(view)
                view.tag = viewHolder
            }
            else -> {
                view = convertView
                viewHolder = view.tag as ViewHolder
            }
        }

        adapterData?.run {
            val message = get(position)
            viewHolder.textmessage.text = message.textmessage
        }
        return view
    }
}

