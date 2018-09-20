package com.tokyoc.line_client

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.ActionBar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import io.realm.Realm
import io.realm.Sort
import io.realm.kotlin.where
import retrofit2.adapter.rxjava.HttpException
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

class GroupActivity : AppCompatActivity() {
    private lateinit var realm: Realm
    private lateinit var pollingConnection: ServiceConnection
    private lateinit var pollingStatus: PollingStatus

    lateinit var toolbar: ActionBar

    companion object {
        const val EXTRA_GROUP = "groupId"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group)

        toolbar = supportActionBar!!
        val bottomNavigation: BottomNavigationView = findViewById(R.id.navigation)
        val item = bottomNavigation.getMenu().getItem(1)
        item.setChecked(true)

        //Realmのために必要なもの
        realm = Realm.getDefaultInstance()
        val groups = realm.where<Group>().findAll().sort("latest", Sort.DESCENDING)
        val listView: ListView = findViewById(R.id.group_list_view)
        val listAdapter = GroupListAdapter(groups)
        listView.adapter = listAdapter

        val token = intent.getStringExtra("token")
        val client = Client.build(token)

        pollingConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder0: IBinder?) {
                pollingStatus = realm.where<PollingStatus>().findFirst() ?: return
            }

            override fun onServiceDisconnected(p0: ComponentName?) {}
        }

        val intent = Intent(applicationContext, PollingService::class.java)
        intent.putExtra("token", token)
        bindService(intent, pollingConnection, Context.BIND_AUTO_CREATE)
        startService(intent)

        //Groupをクリックした時の処理
        listView.setOnItemClickListener { adapterView, view, position, id ->
            val group = groups[position] ?: return@setOnItemClickListener

            realm.executeTransaction {
                pollingStatus.suppressedGroup = group.id
            }

            val intent = Intent(this, MessageActivity::class.java)
            intent.putExtra("groupId", group.id)
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

        bottomNavigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_friend -> {
                    val intent = Intent(this, MemberActivity::class.java)
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

        //グループ作成ボタンを押した時の処理
        findViewById<FloatingActionButton>(R.id.make_group_button).setOnClickListener {
            val intent = Intent(this, MakeGroupActivity::class.java)
            intent.putExtra("token", token)
            startActivity(intent)
        }

        val member1 = realm.where<Member>().findAll()
        for (i in member1) {
            Log.d("COMM", "group activity: ${i.isFriend}, ${i.name}")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_group, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val token = intent.getStringExtra("token")

        when (item?.itemId) {
            R.id.search -> {
                val intent = Intent(this, SearchMessageActivity::class.java)
                intent.putExtra("token", token)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPause() {
        realm.executeTransaction {
            pollingStatus.suppressedGroup = null
        }

        super.onPause()
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

    override fun onDestroy() {
        unbindService(pollingConnection)
        stopService(Intent(applicationContext, PollingService::class.java))
        super.onDestroy()
    }
}
