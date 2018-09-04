package com.tokyoc.line_client

import android.content.DialogInterface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ListView
import android.widget.Button
import android.widget.Toast
import android.content.Intent
import android.support.v7.app.AlertDialog
import android.view.KeyEvent
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

        //Memberを長押しした時の処理
        listView.setOnItemLongClickListener { adapterView, view, position, id ->
            AlertDialog.Builder(this).apply {
                setTitle("Delete Friend")
                setMessage("Really Delete Friend?")
                setPositiveButton("OK", DialogInterface.OnClickListener { _, _ ->
                    val deleteMember = adapterView.getItemAtPosition(position) as Member
                    realm.executeTransaction {
                        realm.where<Member>().equalTo("id", deleteMember.id)?.findFirst()?.deleteFromRealm()
                    }
                })
                setNegativeButton("Cancel", null)
                show()
            }
            return@setOnItemLongClickListener true
        }

        //友達追加ボタンを押した時の処理
        findViewById<Button>(R.id.add_friend_button).setOnClickListener {
            val intent = Intent(this, AddFriendActivity::class.java)
            intent.putExtra("token", getIntent().getStringExtra("token"))
            startActivity(intent)
        }

        //サインアウトボタンを押した時の処理
        findViewById<Button>(R.id.signout_button).setOnClickListener {
            val intent = Intent(this, SigninActivity::class.java)
            AlertDialog.Builder(this).apply {
                setTitle("Sign Out")
                setMessage("Really Sign Out?")
                setPositiveButton("OK", DialogInterface.OnClickListener { _, _ ->
                    FirebaseAuth.getInstance().signOut()
                    startActivity(intent)
                })
                setNegativeButton("Cancel", null)
                show()
            }
        }

        //groupボタンを押した時の処理
        findViewById<Button>(R.id.group_button).setOnClickListener {
            val intent = Intent(this, GroupActivity::class.java)
            intent.putExtra("token", getIntent().getStringExtra("token"))
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
