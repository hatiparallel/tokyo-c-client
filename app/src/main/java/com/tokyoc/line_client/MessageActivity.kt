package com.tokyoc.line_client

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import com.trello.rxlifecycle.components.support.RxAppCompatActivity
import com.trello.rxlifecycle.kotlin.bindToLifecycle
import io.realm.Realm
import io.realm.kotlin.where
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.IOException

class MessageActivity : RxAppCompatActivity() {
    private lateinit var realm: Realm

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

        val returnButton = findViewById<Button>(R.id.return_button)
        val sendButton = findViewById<Button>(R.id.send_button)
        val inviteButton = findViewById<Button>(R.id.invite_button)
        val messageEditText = findViewById<EditText>(R.id.message_edit_text)
        val listAdapter = MessageListAdapter(messages)
        val groupName = findViewById<TextView>(R.id.send_user_name_text_view)
        val memberListButton = findViewById<Button>(R.id.member_list_button)

        listView.adapter = listAdapter
        groupName.text = group?.name
        listView.setSelection(listAdapter.messages0.size)

        //通信に使うものたちの定義
        val client = Client.build(token)
        var sinceId = realm.where<Message>().max("id") ?: 0

        Log.d("COMM", "token: $token")
        Log.d("COMM", "listening /streams/${group?.id}")
        Log.d("COMM", "members of this group: ${group?.members}")

        var memberList = arrayListOf<String>()

        if (group != null) {
            for (memberId in group.members) {
                val member = realm.where<Member>().equalTo("id", memberId).findFirst()
                Log.d("COMM", "${member?.id}")
                if (member != null) {
                    memberList.add(member.id)
                }
            }
        }

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
                            Log.d("COMM", "message ID: ${it.id}")
                            realm.executeTransaction {
                                realm.insert(message)
                                Log.d("COMM", "registered: ${message.id}")
                            }
                            listView.setSelection(listAdapter.messages0.size)
                            Log.d("COMM", "received")
                            if (message.isEvent == 1) {
                                if (message.content == "join") {
                                    realm.executeTransaction {
                                        group?.members?.remove(message.author)
                                    }
                                } else if (message.content == "leave") {
                                    realm.executeTransaction {
                                        group?.members?.add(message.author)
                                    }
                                }
                            }
                        },
                        {
                            Log.d("COMM", "receive failed: $it")
                        })

        // ボタンをクリックしたらGroup画面に遷移
        returnButton.setOnClickListener {
            val intent = Intent(this, GroupActivity::class.java)
            intent.putExtra("token", token)
            startActivity(intent)
        }

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

        // 招待ボタンを押した時の処理
        inviteButton.setOnClickListener {
            val intent = Intent(this, InviteActivity::class.java)
            intent.putExtra("token", token)
            intent.putExtra("groupId", groupId)
            startActivity(intent)
        }

        // メンバー一覧ボタンを押した時の処理
        memberListButton.setOnClickListener {
            val intent = Intent(this, GroupMemberActivity::class.java)
            intent.putExtra("token", token)
            intent.putExtra("groupId", groupId)
            intent.putExtra("members", memberList)
            startActivity(intent)
        }

    }

    //Realmインスタンスを破棄
    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }
}
