package com.tokyoc.line_client

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
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

        val toolbar = supportActionBar!!
        toolbar.setDisplayHomeAsUpEnabled(true)

        //Realmを利用するために必要なもの
        realm = Realm.getDefaultInstance()
        val group = realm.where<Group>().equalTo("id", groupId).findFirst()
        val members = realm.where<Member>().findAll()
        val listView: ListView = findViewById(R.id.member_list_view)
        val listAdapter = MemberListAdapter(members)
        listView.adapter = listAdapter

        val client = Client.build(token)

        var multiple_flag: Boolean = false
        var multiple_invite: MutableList<String> = mutableListOf()

        //Memberを押した時の処理
        listView.setOnItemClickListener { adapterView, view, position, id ->
            val memberInvite = adapterView.getItemAtPosition(position) as Member
            if (multiple_flag == false) {
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
            } else if (multiple_invite.remove(memberInvite.id)) {
                view.setBackgroundColor(Color.WHITE)
            } else {
                multiple_invite.add(memberInvite.id)
                view.setBackgroundColor(Color.MAGENTA)
            }
        }

        listView.setOnItemLongClickListener { adapterView, view, position, id ->
            val memberInvite = adapterView.getItemAtPosition(position) as Member
            multiple_flag = true
            if (multiple_invite.remove(memberInvite.id)) {
                view.setBackgroundColor(Color.WHITE)
            } else {
                multiple_invite.add(memberInvite.id)
                view.setBackgroundColor(Color.MAGENTA)
            }
            return@setOnItemLongClickListener true

        }

        findViewById<Button>(R.id.decide_button).setOnClickListener {
            if (multiple_flag == false) {
                return@setOnClickListener
            }

            client.inviteMultiplePerson(groupId, multiple_invite)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        Log.d("COMM", "post done: ${it}")
                        realm.executeTransaction {
                            for (i in multiple_invite) {
                                group?.members?.add(i)
                            }
                        }
                        Log.d("COMM", "register done")
                        val intent = Intent(this, MessageActivity::class.java)
                        intent.putExtra("token", token)
                        intent.putExtra("groupId", groupId)
                        startActivity(intent)
                    }, {
                        Log.d("COMM", "post failed: ${it.message}")
                    })
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val token = intent.getStringExtra("token")
        val groupId: Int = intent.getIntExtra("groupId", 0)
        when (item?.itemId) {
            android.R.id.home -> {
                val intent = Intent(this, MessageActivity::class.java)
                intent.putExtra("token", token)
                intent.putExtra("groupId", groupId)
                startActivity(intent)
            }
        }
         return super.onOptionsItemSelected(item)
    }
}
