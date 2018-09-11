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
import retrofit2.adapter.rxjava.HttpException
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

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

        val token = intent.getStringExtra("token")
        val client = Client.build(token)

        //Groupをクリックした時の処理
        listView.setOnItemClickListener { adapterView, view, position, id ->
            val group = groups[position]
            val intent = Intent(this, MessageActivity::class.java)
            intent.putExtra("groupId", group?.id)
            intent.putExtra("token", getIntent().getStringExtra("token"))
            startActivity(intent)
        }

        listView.setOnItemLongClickListener { adapterView, view, position, id ->
            val groupLeave = adapterView.getItemAtPosition(position) as Group
            AlertDialog.Builder(this).apply {
                setTitle("Leave Group")
                setMessage("Really Leave ${groupLeave.name}?")
                setPositiveButton("OK", DialogInterface.OnClickListener { _, _ ->
                    val myUid: String? = FirebaseAuth.getInstance().currentUser?.uid
                    if (myUid == null) {
                        Toast.makeText(applicationContext, "sorry, could not get UID", Toast.LENGTH_LONG).show()
                        Log.d("COMM", "could not get UID")
                    } else {
                        Log.d("COMM", "succeeded to get UID: $myUid")
                        client.leaveGroup(groupLeave.id, myUid)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe({
                                    realm.executeTransaction {
                                        Log.d("COMM", "leaving ${groupLeave.id}")
                                        realm.where<Group>().equalTo("id", groupLeave.id)?.findFirst()?.deleteFromRealm()
                                    }
                                }, {
                                    val httpException = it as HttpException
                                    val httpCode = httpException.code()
                                    if (httpCode.toInt() == 410) {
                                        Log.d("COMM", "You were the last participant. See you.")
                                        realm.executeTransaction {
                                            Log.d("COMM", "leaving ${groupLeave.id}")
                                            realm.where<Group>().equalTo("id", groupLeave.id)?.findFirst()?.deleteFromRealm()
                                        }
                                    } else {
                                        Log.d("COMM", "delete failed: ${it.message}")
                                    }
                                })
                    }
                })
                setNegativeButton("Cancel", null)
                show()
            }
            return@setOnItemLongClickListener true
        }

        //memberボタンを押した時の処理
        findViewById<Button>(R.id.member_button).setOnClickListener {
            val intent = Intent(this, MemberActivity::class.java)
            intent.putExtra("token", token)
            startActivity(intent)
        }

        //グループ作成ボタンを押した時の処理
        findViewById<Button>(R.id.make_group_button).setOnClickListener {
            val intent = Intent(this, MakeGroupActivity::class.java)
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
                Log.d("COMM", "back")
                Toast.makeText(applicationContext, "this key is invalid", Toast.LENGTH_LONG)
                        .show()
                return false
            }
            else -> return super.onKeyDown(keyCode, event)
        }
    }
}
