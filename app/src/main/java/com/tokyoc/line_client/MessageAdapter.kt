package com.tokyoc.line_client

import android.graphics.BitmapFactory
import android.graphics.Color
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.tokyoc.line_client.R.drawable.img001
import io.realm.OrderedRealmCollection
import io.realm.Realm
import io.realm.RealmBaseAdapter
import io.realm.kotlin.where
import java.util.*

class MessageListAdapter(data: OrderedRealmCollection<Message>) : RealmBaseAdapter<Message>(data) {
    private  var realm: Realm = Realm.getDefaultInstance()

    var messages0: MutableList<Message> = data

    inner class ViewHolder(cell: View) {
        val messageContent = cell.findViewById<TextView>(R.id.message_text_view)
        val messageAuthor = cell.findViewById<TextView>(R.id.author_name_text_view)
        val messageImage = cell.findViewById<ImageView>(R.id.author_profile_image_view)
        val messagePostedAt = cell.findViewById<TextView>(R.id.message_send_time)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val viewHolder: ViewHolder

        when (convertView) {
            null -> {
                val inflater = LayoutInflater.from(parent?.context)

//                val rand = Random()
//                val num = rand.nextInt(2)
//                if (num == 0) {
//                    view = inflater.inflate(R.layout.view_message, parent, false)
//                } else {
//                    view = inflater.inflate(R.layout.view_message_self, parent, false)
//                }

                view = inflater.inflate(R.layout.view_message, parent, false)
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
            val author = realm.where<Member>().equalTo("id", message.author).findFirst()
            viewHolder.messageContent.text = message.content
            viewHolder.messageAuthor.text = author?.name ?: "取得失敗"
            if (author != null && author.image.size > 0) {
                viewHolder.messageImage
                        .setImageBitmap(BitmapFactory.decodeByteArray(author.image, 0, author.image.size))
            }
            val messageSendTime = DateFormat.format("yyyy/MM/dd kk:mm:ss", message.postedAt).toString()
            viewHolder.messagePostedAt.text = messageSendTime
            if (message.isEvent == 1) {
                viewHolder.messageContent.setTextColor(Color.BLUE)
            } else if (message.isEvent == 2) {
                viewHolder.messageContent.setTextColor(Color.MAGENTA)
            } else if (message.isEvent > 2) {
                viewHolder.messageContent.setTextColor(Color.RED)
            } else {
                viewHolder.messageContent.setTextColor(Color.BLACK)
            }
        }
        return view
    }
}
