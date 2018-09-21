package com.tokyoc.line_client

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import io.realm.OrderedRealmCollection
import io.realm.RealmBaseAdapter

class MemberListAdapter(data: OrderedRealmCollection<Member>) : RealmBaseAdapter<Member>(data) {

    var members0: MutableList<Member> = data

    inner class ViewHolder(cell: View) {
        val username = cell.findViewById<TextView>(R.id.user_name_view)
        val userImage = cell.findViewById<ImageView>(R.id.profile_image_view)
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
                viewHolder = view.tag as MemberListAdapter.ViewHolder
            }
        }

        adapterData?.run {
            val member = get(position)
            viewHolder.username.text = member.name
            if (member.image.size > 0) {
                viewHolder.userImage
                        .setImageBitmap(BitmapFactory.decodeByteArray(member.image, 0, member.image.size))
            }
        }
        return view
    }
}