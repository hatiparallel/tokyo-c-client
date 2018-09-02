package com.tokyoc.line_client

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ListView
import android.widget.Button
import android.widget.Toast
import android.content.Intent
import com.google.gson.GsonBuilder

import com.google.firebase.auth.FirebaseAuth
import io.realm.RealmList
import kotlinx.android.parcel.RawValue

class GroupActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_GROUP = "group"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group)

        val listView: ListView = findViewById(R.id.group_list_view)
        val listAdapter = GroupListAdapter(applicationContext)

        listView.adapter = listAdapter
        listAdapter.groups = listOf(dummyGroup("い"), dummyGroup("ろ"), dummyGroup("は"), dummyGroup("に"), dummyGroup("ほ"))

        listView.setOnItemClickListener { adapterView, view, position, id ->
            val group = listAdapter.groups[position]
            val intent = Intent(this, GroupMessageActivity::class.java)
            intent.putExtra(EXTRA_GROUP, group)
            intent.putExtra("token", getIntent().getStringExtra("token"))
            startActivity(intent)
        }

        findViewById<Button>(R.id.signout_button).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, SigninActivity::class.java))
        }

        findViewById<Button>(R.id.member_button).setOnClickListener {
            val intent = Intent(this, MemberActivity::class.java)
            intent.putExtra("token", getIntent().getStringExtra("token"))
            startActivity(intent)
        }

        findViewById<Button>(R.id.make_group_button).setOnClickListener {
            val intent = Intent(this, MakeGroupActivity::class.java)
            intent.putExtra("token", getIntent().getStringExtra("token"))
            startActivity(intent)
        }
    }

    private fun dummyMember(name: String): Member = Member(name = name, id = 123, groupId = 123)
    private fun dummyGroup(name: String): Group = Group(name = name, groupId = 123)
            //members = RealmList(dummyMember("Aさん"), dummyMember("Bさん"), dummyMember("Cさん")))
}
