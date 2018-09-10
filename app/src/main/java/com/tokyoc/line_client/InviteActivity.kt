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

class InviteActivity : AppCompatActivity() {
    private lateinit var realm: Realm

    companion object {
        const val EXTRA_MEMBER = "member"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invite)

        val token = intent.getStringExtra("token")
        val groupId: Int = intent.getIntExtra("groupId", 0)

        //Realmを利用するために必要なもの
        realm = Realm.getDefaultInstance()
        val group = realm.where<Group>().equalTo("id", groupId).findFirst()
        val members = realm.where<Member>().findAll()
        val listView: ListView = findViewById(R.id.member_list_view)
        val listAdapter = MemberListAdapter(members)
        listView.adapter = listAdapter

        val client = Client.build(token)

        var multiple_flag: Boolean = false
        var multiple_invite: Array<Boolean> = Array(members.size, { i -> false })

        //Memberを押した時の処理
        listView.setOnItemClickListener { adapterView, view, position, id ->
            if (multiple_flag == false) {
                val memberInvite = adapterView.getItemAtPosition(position) as Member
                val intent = Intent(this, MessageActivity::class.java)
                AlertDialog.Builder(this).apply {
                    setTitle("Invite Friend")
                    setMessage("Really Invite ${memberInvite.name}?")
                    setPositiveButton("OK", DialogInterface.OnClickListener { _, _ ->
                        Log.d("COMM", "will invite ${memberInvite.name}")
                        Log.d("COMM", "${groupId}, ${memberInvite.id}")
                        client.invitePerson(groupId, memberInvite.id)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe({
                                    Log.d("COMM", "post done: ${it}")
                                    realm.executeTransaction {
                                        group?.members?.add(memberInvite.id)
                                    }
                                    intent.putExtra("token", token)
                                    intent.putExtra("group", groupId)
                                    startActivity(intent)
                                }, {
                                    Log.d("COMM", "post failed: ${it}")
                                })
                    })
                    setNegativeButton("Cancel", null)
                    show()
                }
            } else {
                val memberInvite = adapterView.getItemAtPosition(position) as Member
                if (multiple_invite[position] == false) {
                    multiple_invite[position] = true
                    view.setBackgroundColor(Color.MAGENTA)
                } else {
                    multiple_invite[position] = false
                    view.setBackgroundColor(Color.WHITE)
                }
            }
        }

        listView.setOnItemLongClickListener { adapterView, view, position, id ->
            val memberInvite = adapterView.getItemAtPosition(position) as Member
            multiple_flag = true
            if (multiple_invite[position] == false) {
                multiple_invite[position] = true
                view.setBackgroundColor(Color.MAGENTA)
            } else {
                multiple_invite[position] = false
                view.setBackgroundColor(Color.WHITE)
            }
            return@setOnItemLongClickListener true

        }

        findViewById<Button>(R.id.return_button).setOnClickListener {
            val intent = Intent(this, MessageActivity::class.java)
            intent.putExtra("token", token)
            intent.putExtra("group", groupId)
            startActivity(intent)
        }

        findViewById<Button>(R.id.decide_button).setOnClickListener {
            val intent = Intent(this, MessageActivity::class.java)
            intent.putExtra("token", token)
            intent.putExtra("groupId", groupId)
            startActivity(intent)
        }
    }
}
