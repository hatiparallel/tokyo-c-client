package com.tokyoc.line_client

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.KeyEvent
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import io.realm.Realm
import io.realm.kotlin.where
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

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
        val members = realm.where<Member>().equalTo("isFriend", 1.toInt()).findAll()
        val listView: ListView = findViewById(R.id.member_list_view)
        val listAdapter = MemberListAdapter(members)
        listView.adapter = listAdapter

        val token = intent.getStringExtra("token")

        // 通信の準備
        val client = Client.build(token)

        //Memberを長押しした時に友達削除
        listView.setOnItemLongClickListener { adapterView, view, position, id ->
            val memberDelete = adapterView.getItemAtPosition(position) as Member
            Log.d("COMM", "Id is ${memberDelete.id}")
            Log.d("COMM", "id is ${memberDelete.id}")
            client.deleteFriend(memberDelete.id)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        AlertDialog.Builder(this).apply {
                            setTitle("Delete Friend")
                            setMessage("Really Delete ${memberDelete.name}?")
                            setPositiveButton("OK", DialogInterface.OnClickListener { _, _ ->
                                realm.executeTransaction {
                                    realm.where<Member>().equalTo("id", memberDelete.id)?.findFirst()?.deleteFromRealm()
                                }
                            })
                            setNegativeButton("Cancel", null)
                            show()
                        }
                    },{
                        Log.d("COMM", "post failed: ${it}")
                    })
            return@setOnItemLongClickListener true
        }

        //友達追加ボタンを押した時の処理
        findViewById<Button>(R.id.add_friend_button).setOnClickListener {
            val intent = Intent(this, SendPinActivity::class.java)
            intent.putExtra("token", token)
            startActivity(intent)
        }

        //groupボタンを押した時にGroupActivityに遷移
        findViewById<Button>(R.id.group_button).setOnClickListener {
            val intent = Intent(this, GroupActivity::class.java)
            intent.putExtra("token", token)
            startActivity(intent)
        }

        //設定ボタンを押した時の処理
        findViewById<Button>(R.id.setting_button).setOnClickListener {
            val intent = Intent(this, SettingActivity::class.java)
            intent.putExtra("token", token)
            startActivity(intent)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                Toast.makeText(applicationContext, "this key is invalid", Toast.LENGTH_LONG)
                        .show()
                return false
            }
            else -> return super.onKeyDown(keyCode, event)
        }
    }
}
