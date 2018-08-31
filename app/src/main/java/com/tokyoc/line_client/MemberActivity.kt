package com.tokyoc.line_client

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ListView
import android.widget.Button
import android.widget.Toast
import android.content.Intent
import com.google.gson.GsonBuilder

import com.google.firebase.auth.FirebaseAuth

class MemberActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_MEMBER = "member"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_member)

        val listView: ListView = findViewById(R.id.list_view)
        val listAdapter = MemberListAdapter(applicationContext)

        listView.adapter = listAdapter
        listAdapter.members = listOf(dummyMember("Aさん"), dummyMember("Bさん"), dummyMember("Cさん"), dummyMember("Dさん"), dummyMember("Eさん"))

        listView.setOnItemClickListener { adapterView, view, position, id ->
            val member = listAdapter.members[position]
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
    }

    private fun dummyMember(name: String): Member = Member(name = name, id = 123, groupId = 123)
}
