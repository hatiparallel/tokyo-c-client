package com.tokyoc.line_client

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_friend_add.*

class AddFriendActivity: AppCompatActivity() {

    private lateinit var realm: Realm
    override fun onCreate(saveInstanceState: Bundle?) {
        super.onCreate(saveInstanceState)
        setContentView(R.layout.activity_friend_add)
        realm = Realm.getDefaultInstance()

        val token = intent.getStringExtra("token")

        findViewById<Button>(R.id.add_friend_button).setOnClickListener {
            realm.executeTransaction {
                val maxId = realm.where<Member>().max("id")
                val nextId = (maxId?.toLong() ?: 0L) + 1
                val member = realm.createObject<Member>(nextId)
                member.name = user_id.text.toString()
            }
            val intent = Intent(this, MemberActivity::class.java)
            intent.putExtra("token", token)
            startActivity(intent)
        }

        findViewById<Button>(R.id.to_member_button).setOnClickListener {
            val intent = Intent(this, MemberActivity::class.java)
            intent.putExtra("token", token)
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }
}