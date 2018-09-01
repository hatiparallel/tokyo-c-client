package com.tokyoc.line_client

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ListView
import android.widget.Button
import android.widget.Toast
import android.content.Intent
import com.google.gson.GsonBuilder

import com.google.firebase.auth.FirebaseAuth
import io.realm.Realm
import io.realm.kotlin.where

class MemberActivity : AppCompatActivity() {
    private lateinit var realm: Realm

    companion object {
        const val EXTRA_MEMBER = "member"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_member)

        //Realmを利用するために必要なもの
        realm = Realm.getDefaultInstance()
        val members = realm.where<Member>().findAll()
        val listView: ListView = findViewById(R.id.member_list_view)
        val listAdapter = MemberListAdapter(members)
        listView.adapter = listAdapter

        listView.setOnItemClickListener { adapterView, view, position, id ->
            val member = members[position]
            val intent = Intent(this, MessageActivity::class.java)
            intent.putExtra(EXTRA_MEMBER, member)
            intent.putExtra("token", getIntent().getStringExtra("token"))
            startActivity(intent)
        }

        findViewById<Button>(R.id.add_friend_button).setOnClickListener {
            startActivity(Intent(this, AddFriendActivity::class.java))
        }

        findViewById<Button>(R.id.signout_button).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, SigninActivity::class.java))
        }

        findViewById<Button>(R.id.group_button).setOnClickListener {
            val intent = Intent(this, GroupActivity::class.java)
            intent.putExtra("token", getIntent().getStringExtra("token"))
            startActivity(intent)
        }
    }
}
