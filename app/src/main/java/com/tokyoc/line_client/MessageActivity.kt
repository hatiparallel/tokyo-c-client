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
import io.realm.RealmResults
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import org.jetbrains.anko.Android
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.IOException
import java.util.concurrent.TimeUnit

class MessageActivity : RxAppCompatActivity() {
    private lateinit var realm: Realm
    lateinit var toolbar: ActionBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        val token = intent.getStringExtra("token")
        val groupId: Int = intent.getIntExtra("groupId", 0)
        val target: Int = intent.getIntExtra("target", -1)

        //Realmを利用するために必要なもの
        realm = Realm.getDefaultInstance()
        val messages = realm.where<Message>().equalTo("channel", groupId).findAll()
        val listView: ListView = findViewById<ListView>(R.id.message_list_view)
        val group = realm.where<Group>().equalTo("id", groupId).findFirst()

        val sendButton = findViewById<ImageButton>(R.id.send_button)
        val messageEditText = findViewById<EditText>(R.id.message_edit_text)
        val listAdapter = MessageListAdapter(messages)

        var isImportant: Boolean = false

        listView.adapter = listAdapter
        if (target < 0) {
            listView.setSelection(messages.size)
        } else {
            for ((index, value) in messages.withIndex()) {
                if (value.id == target) {
                    listView.setSelection(index)
                    break
                }
            }
        }

        toolbar = supportActionBar!!
        toolbar.title = "${group?.name}"
        toolbar.setDisplayHomeAsUpEnabled(true)

        //通信に使うものたちの定義
        val client = Client.build(token)
        var sinceId = realm.where<Message>().max("id") ?: -1

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
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .bindToLifecycle(this)
                .subscribe({
                    val realm = Realm.getDefaultInstance()
                    val message = it

                    Log.d("COMM", "message ID: ${it.id}")

                    realm.executeTransaction {
                        realm.insert(message)
                        Log.d("COMM", "registered: ${message.id}")
                    }

                    Log.d("COMM", "received ${message.isEvent}")

                    runOnUiThread {
                        listView.setSelection(listAdapter.messages0.size)
                    }

                    if (message.isEvent == 1) {
                        if (message.content == "join") {
                            val memberCome = Member.lookup(message.author, client).toBlocking().single()
                                    ?: return@subscribe
                            val group = realm.where<Group>().equalTo("id", groupId).findFirst()

                            realm.executeTransaction {
                                group?.members?.add(message.author)
                                if (memberCome != null) {
                                    memberCome.groupJoin += 1
                                    realm.insertOrUpdate(memberCome)
                                }
                            }
                            Log.d("COMM", "now ${group?.members?.size} members")
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
                }, {
                    Log.d("COMM", "receive failed: $it")
                })


        sendButton.setOnLongClickListener {
            if (isImportant) {
                isImportant = false
                messageEditText.setBackgroundColor(Color.WHITE)
            } else {
                isImportant = true
                messageEditText.setBackgroundColor(Color.MAGENTA)
            }
            return@setOnLongClickListener true
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
            Log.d("COMM", "Posted at ${message.postedAt}")
            if (isImportant) {
                message.isEvent = 2
            }
            messageEditText.setText("", TextView.BufferType.NORMAL)
            isImportant = false
            messageEditText.setBackgroundColor(Color.WHITE)

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
                val flag = intent.getIntExtra("flag", 0)
                if (flag == 0) {
                    val intent = Intent(this, GroupActivity::class.java)
                    intent.putExtra("token", token)
                    startActivity(intent)
                } else {
                    val intent = Intent(this, SearchMessageActivity::class.java)
                    intent.putExtra("token", token)
                    startActivity(intent)
                }
                finishAndRemoveTask()
            }
            R.id.member_list -> {
                if (group == null) {
                    return false
                } else {
                    val memberList = arrayListOf<String>()
                    for (memberId in group.members) {
                        memberList.add(memberId)
                    }
                    val client = Client.build(token)
                    client.getGroup(groupId)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({
                                if (memberList.size < it.members.size) {
                                    Toast.makeText(applicationContext, "情報取得中です。お待ちください。", Toast.LENGTH_LONG).show()
                                    for (member in it.members) {
                                        if (memberList.all { m -> m != member }) {
                                            memberList.add(member)
                                            val newMember = Member.lookup(member, client).toBlocking().single()
                                            realm.executeTransaction {
                                                if (newMember != null) {
                                                    newMember.groupJoin += 1
                                                    realm.insertOrUpdate(newMember)
                                                }
                                            }
                                        }
                                    }
                                }
                                val intent = Intent(this, GroupMemberActivity::class.java)
                                intent.putExtra("token", token)
                                intent.putExtra("groupId", groupId)
                                intent.putExtra("members", memberList)
                                startActivity(intent)
                                Log.d("COMM", "group information get done: ${it}")
                            }, {
                                Log.d("COMM", "group information get failed: ${it}")
                            })
                }
            }
            R.id.member_invite -> {
                if (group == null) {
                    return false
                } else if (group.name.isEmpty()) {
                    AlertDialog.Builder(this).apply {
                        setTitle("Alert")
                        setMessage("Not a group.\n You'd set the group's name if you wanna invite.")
                        setPositiveButton("OK", null)
                        show()
                    }
                } else {
                    val invitableList = arrayListOf<String>()
                    val friends = realm.where<Member>().equalTo("isFriend", Relation.FRIEND).findAll()
                    if (group != null) {
                        for (friend in friends) {
                            if (group.members.all { m -> m != friend.id }) {
                                invitableList.add(friend.id)
                            }
                        }
                    }
                    val intent = Intent(this, InviteActivity::class.java)
                    intent.putExtra("token", token)
                    intent.putExtra("groupId", groupId)
                    intent.putExtra("invitable", invitableList)
                    startActivity(intent)
                }
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