package com.tokyoc.line_client

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.realm.OrderedRealmCollection
import io.realm.RealmBaseAdapter

class GroupMessageListAdapter(data: OrderedRealmCollection<Message>) : RealmBaseAdapter<Message>(data) {

    var messages0: MutableList<Message> = data

    inner class ViewHolder(cell: View) {
        val messagecontent = cell.findViewById<TextView>(R.id.message_text_view)
        val messageAuthor = cell.findViewById<TextView>(R.id.author_name_text_view)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val viewHolder: ViewHolder

        when (convertView) {
            null -> {
                val inflater = LayoutInflater.from(parent?.context)
                view = inflater.inflate(R.layout.view_group_message, parent, false)
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
            viewHolder.messagecontent.text = message.content
            viewHolder.messageAuthor.text = "誰かさん"
        }
        return view
    }
}
