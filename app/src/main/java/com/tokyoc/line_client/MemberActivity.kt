package com.tokyoc.line_client

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.ActionBar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import android.widget.Toast
import io.realm.Realm
import io.realm.kotlin.where
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

class MemberActivity : AppCompatActivity() {
    private lateinit var realm: Realm
    lateinit var toolbar: ActionBar

    companion object {
        const val EXTRA_MEMBER = "member"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_member)

        toolbar = supportActionBar!!
        val bottomNavigation: BottomNavigationView = findViewById(R.id.navigation)
        val item = bottomNavigation.getMenu().getItem(0)
        item.setChecked(true)

        //Realmを利用するために必要なもの
        realm = Realm.getDefaultInstance()
        val members = realm.where<Member>().equalTo("isFriend", Relation.FRIEND).findAll()
        val membersAll = realm.where<Member>().findAll()
        Log.d("COMM", "start")
        for (i in membersAll) {
            Log.d("COMM", "${i.isFriend}, ${i.name}")
        }
        Log.d("COMM", "goal")
        val listView: ListView = findViewById(R.id.member_list_view)
        val listAdapter = MemberListAdapter(members)
        listView.adapter = listAdapter

        val token = intent.getStringExtra("token")

        // 通信の準備
        val client = Client.build(token)

        listView.setOnItemClickListener { adapterView, view, position, id ->
            val memberChosen = adapterView.getItemAtPosition(position) as Member
            intent = Intent(this, MemberProfileActivity::class.java)
            intent.putExtra("token", token)
            intent.putExtra("memberId", memberChosen.id)
            startActivity(intent)
        }

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

        bottomNavigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_group -> {
                    val intent = Intent(this, GroupActivity::class.java)
                    intent.putExtra("token", token)
                    startActivity(intent)
                    Log.d("COMM", "${it.title}")
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.navigation_setting -> {
                    val intent = Intent(this, SettingActivity::class.java)
                    intent.putExtra("token", getIntent().getStringExtra("token"))
                    startActivity(intent)
                    Log.d("COMM", "${it.title}")
                    return@setOnNavigationItemSelectedListener true
                }
            }
            false
        }

        //友達追加ボタンを押した時の処理
        findViewById<FloatingActionButton>(R.id.add_friend_button).setOnClickListener {
            val intent = Intent(this, SendPinActivity::class.java)
            intent.putExtra("token", token)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_member, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val token = intent.getStringExtra("token")
        when (item?.itemId) {
            R.id.search -> {
                val intent = Intent(this, SearchMemberActivity::class.java)
                intent.putExtra("token", token)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
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
