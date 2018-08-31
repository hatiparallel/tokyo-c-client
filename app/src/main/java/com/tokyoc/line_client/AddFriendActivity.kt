package com.tokyoc.line_client

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button

class AddFriendActivity: AppCompatActivity() {

    override fun onCreate(saveInstanceState: Bundle?) {
        super.onCreate(saveInstanceState)
        setContentView(R.layout.activity_friend_add)

        findViewById<Button>(R.id.to_member).setOnClickListener {
            startActivity(Intent(this, MemberActivity::class.java))
        }
    }
}