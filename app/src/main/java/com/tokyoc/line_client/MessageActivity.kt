package com.tokyoc.line_client

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import com.trello.rxlifecycle.components.support.RxAppCompatActivity
import com.trello.rxlifecycle.kotlin.bindToLifecycle
import io.realm.Realm
import io.realm.kotlin.where
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.IOException

class MessageActivity : RxAppCompatActivity() {
    private lateinit var realm: Realm
    lateinit var toolbar: ActionBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        val token = intent.getStringExtra("token")
        val groupId: Int = intent.getIntExtra("groupId", 0)

        //Realmを利用するために必要なもの
        realm = Realm.getDefaultInstance()
        val messages = realm.where<Message>().equalTo("channel", groupId).findAll()
        val listView: ListView = findViewById<ListView>(R.id.message_list_view)
        val group = realm.where<Group>().equalTo("id", groupId).findFirst()

        val sendButton = findViewById<ImageButton>(R.id.send_button)
        val messageEditText = findViewById<EditText>(R.id.message_edit_text)
        val listAdapter = MessageListAdapter(messages)

        listView.adapter = listAdapter
        listView.setSelection(listAdapter.messages0.size)

        toolbar = supportActionBar!!
        toolbar.title = "${group?.name}"
        toolbar.setDisplayHomeAsUpEnabled(true)

        //通信に使うものたちの定義
        val client = Client.build(token)
        var sinceId = realm.where<Message>().max("id") ?: 0

        Log.d("COMM", "token: $token")
        Log.d("COMM", "listening /streams/${group?.id}")
        Log.d("COMM", "members of this group: ${group?.members}")



        client.getMessages(groupId, sinceId.toInt())
                .flatMap {
                    val source = it.source()

                    rx.Observable.create(rx.Observable.OnSubscribe<Message> {
                        try {
                            while (!source.exhausted()) {
                                it.onNext(Client.gson.fromJson<Message>(source.readUtf8Line(), Message::class.java))
                            }

                            it.onCompleted()
                        } catch (e: IOException) {
                            it.onError(e)
                        }
                    })
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .bindToLifecycle(this)
                .subscribe(
                        {
                            val message = it
                            val author = it.author
                            Log.d("COMM", "message ID: ${it.id}")
                            realm.executeTransaction {
                                realm.insert(message)
                                Log.d("COMM", "registered: ${message.id}")
                            }
                            listView.setSelection(listAdapter.messages0.size)
                            Log.d("COMM", "received")

                            if (message.isEvent == 1) {
                                if (message.content == "join") {
                                    Member.lookup(message.author, client)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe({
                                                val realm = Realm.getDefaultInstance()
                                                val memberCome = it
                                                val group = realm.where<Group>().equalTo("id", groupId).findFirst()
                                                realm.executeTransaction {
                                                    group?.members?.add(author)
                                                    if (memberCome != null) {
                                                        memberCome.groupJoin += 1
                                                        realm.insertOrUpdate(memberCome)
                                                    }
                                                }
                                                Log.d("COMM", "now ${group?.members?.size} members")
                                            }, {
                                                Log.d("COMM", "Message Acitivity get person failed: ${it}")
                                            })
                                } else if (message.content == "leave") {
                                    val memberLeft: Member? = realm.where<Member>().equalTo("id", message.author)?.findFirst()
                                    if (memberLeft != null) {
                                        realm.executeTransaction {
                                            memberLeft.groupJoin -= 1
                                        }
                                        memberLeft.deregister()
                                    }
                                    realm.executeTransaction {
                                        group?.members?.remove(message.author)
                                    }
                                    Log.d("COMM", "now ${group?.members?.size} members")
                                }
                            }

                        },
                        {
                            Log.d("COMM", "receive failed: $it")
                        })

        // 送信ボタンをタップした時の処理
        sendButton.setOnClickListener {
            if (messageEditText.text.isEmpty()) {
                return@setOnClickListener
            }

            //通信部分の準備
            val message: Message = Message()
            message.channel = groupId
            message.content = messageEditText.text.toString()
            messageEditText.setText("", TextView.BufferType.NORMAL)

            //ここから通信部分！
            Log.d("COMM", Client.gson.toJson(message))

            client.sendMessage(message)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        Log.d("COMM", "post done: ${it}")
                    }, {
                        Log.d("COMM", "post failed: ${it}")
                    })
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_message, menu)
        return true
    }

    // メニューをタップした時の処理
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        val token = intent.getStringExtra("token")
        val groupId: Int = intent.getIntExtra("groupId", 0)

        realm = Realm.getDefaultInstance()
        val group = realm.where<Group>().equalTo("id", groupId).findFirst()

        when (item?.itemId) {
            android.R.id.home -> {
                val intent = Intent(this, GroupActivity::class.java)
                intent.putExtra("token", token)
                startActivity(intent)
            }
            R.id.member_list -> {
                var memberList = arrayListOf<String>()
                if (group != null) {
                    for (memberId in group.members) {
                        memberList.add(memberId)
                    }
                }
                val intent = Intent(this, GroupMemberActivity::class.java)
                intent.putExtra("token", token)
                intent.putExtra("groupId", groupId)
                intent.putExtra("members", memberList)
                startActivity(intent)
            }
            R.id.member_invite -> {
                val intent = Intent(this, InviteActivity::class.java)
                intent.putExtra("token", token)
                intent.putExtra("groupId", groupId)
                startActivity(intent)
            }
            R.id.group_profile -> {
                val intent = Intent(this, GroupProfileActivity::class.java)
                intent.putExtra("token", token)
                intent.putExtra("groupId", groupId)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //Realmインスタンスを破棄
    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }
}
