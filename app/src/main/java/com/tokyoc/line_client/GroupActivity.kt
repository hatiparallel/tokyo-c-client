package com.tokyoc.line_client

import android.content.DialogInterface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ListView
import android.widget.Button
import android.widget.Toast
import android.content.Intent

import com.google.firebase.auth.FirebaseAuth
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.KeyEvent
import io.realm.Realm
import io.realm.kotlin.where

class GroupActivity : AppCompatActivity() {
    private lateinit var realm: Realm

    companion object {
        const val EXTRA_GROUP = "groupId"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group)

        //Realmのために必要なもの
        realm = Realm.getDefaultInstance()
        val groups = realm.where<Group>().findAll()
        val listView: ListView = findViewById(R.id.group_list_view)
        val listAdapter = GroupListAdapter(groups)
        listView.adapter = listAdapter
        //listAdapter.groups = listOf(dummyGroup("い"), dummyGroup("ろ"), dummyGroup("は"), dummyGroup("に"), dummyGroup("ほ"))

        //Groupをクリックした時の処理
        listView.setOnItemClickListener { adapterView, view, position, id ->
            val group = groups[position]
            val intent = Intent(this, MessageActivity::class.java)
            intent.putExtra(EXTRA_GROUP, group?.groupId)
            intent.putExtra("token", getIntent().getStringExtra("token"))
            startActivity(intent)
        }

        listView.setOnItemLongClickListener { adapterView, view, position, id ->
            val groupLeave = adapterView.getItemAtPosition(position) as Group
            AlertDialog.Builder(this).apply {
                setTitle("Leave Group")
                setMessage("Really Leave ${groupLeave.name}?")
                setPositiveButton("OK", DialogInterface.OnClickListener { _, _ ->
                    realm.executeTransaction {
                        realm.where<Group>().equalTo("id", groupLeave.id)?.findFirst()?.deleteFromRealm()
                    }
                })
                setNegativeButton("Cancel", null)
                show()
            }
            return@setOnItemLongClickListener true
        }


        //signoutボタンを押した時の処理
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

        //memberボタンを押した時の処理
        findViewById<Button>(R.id.member_button).setOnClickListener {
            val intent = Intent(this, MemberActivity::class.java)
            intent.putExtra("token", getIntent().getStringExtra("token"))
            startActivity(intent)
        }

        //グループ作成ボタンを押した時の処理
        findViewById<Button>(R.id.make_group_button).setOnClickListener {
            val intent = Intent(this, MakeGroupActivity::class.java)
            intent.putExtra("token", getIntent().getStringExtra("token"))
            startActivity(intent)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                Log.d("COMM", "back")
                Toast.makeText(applicationContext, "this key is invalid", Toast.LENGTH_LONG)
                        .show()
                return false
            }
            else -> return super.onKeyDown(keyCode, event)
        }
    }
}
