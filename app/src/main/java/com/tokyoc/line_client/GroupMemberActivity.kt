package com.tokyoc.line_client

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.ListView
import io.realm.Realm
import io.realm.kotlin.where
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import io.realm.RealmQuery

class GroupMemberActivity : AppCompatActivity() {
    private lateinit var realm: Realm

    companion object {
        const val EXTRA_MEMBER = "member"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_member_group)

        var groupMemberId = intent.getStringArrayListExtra("members")
        var groupMemberIdArray: Array<String>?
        groupMemberIdArray = groupMemberId.toTypedArray()

        val token = intent.getStringExtra("token")
        val groupId: Int = intent.getIntExtra("groupId", 0)

        //Realmを利用するために必要なもの
        realm = Realm.getDefaultInstance()
        val groupMembers = realm.where<Member>().`in`("id", groupMemberIdArray).findAll()

        val listView: ListView = findViewById<ListView>(R.id.group_member_list_view)
        val listAdapter = MemberListAdapter(groupMembers)
        listView.adapter = listAdapter

        findViewById<Button>(R.id.return_button).setOnClickListener {
            val intent = Intent(this, MessageActivity::class.java)
            intent.putExtra("token", token)
            intent.putExtra("groupId", groupId)
            startActivity(intent)
        }
    }
}


