package com.tokyoc.line_client

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import com.trello.rxlifecycle.components.support.RxAppCompatActivity
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers


class MakeGroupActivity: RxAppCompatActivity() {
    private lateinit var realm: Realm

    override fun onCreate(saveInstanceState: Bundle?) {
        super.onCreate(saveInstanceState)
        setContentView(R.layout.activity_group_make)
        realm = Realm.getDefaultInstance()

        val token = intent.getStringExtra("token")

        val groupNameEditText = findViewById<EditText>(R.id.group_name)

        val toolbar = supportActionBar!!
        toolbar.setDisplayHomeAsUpEnabled(true)

        val client = Client.build(token)

        //グループ作成ボタンを押した時の処理
        findViewById<Button>(R.id.make_group).setOnClickListener {
            if (groupNameEditText.text.isEmpty()) {
                return@setOnClickListener
            }

            val groupName = groupNameEditText.text.toString()
            val new_group = Group(name = groupName)

            client.makeGroup(new_group)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        Log.d("COMM", "post done: name is ${it.name}, id is ${it.id}")
                        val groupId = it.id
                        val self = realm.where<Member>().equalTo("isFriend", Relation.SELF).findFirst()
                        realm.executeTransaction {
                            val group = realm.createObject<Group>(groupId)
                            group.name = groupName
                            if (self != null) {
                                group.members.add(self.id)
                            }
                        }
                        val intent = Intent(this, GroupActivity::class.java)
                        intent.putExtra("token", token)
                        startActivity(intent)
                    }, {
                        Log.d("COMM", "post failed: ${it}")
                    })
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val token = intent.getStringExtra("token")
        when (item?.itemId) {
            android.R.id.home -> {
                val intent = Intent(this, GroupActivity::class.java)
                intent.putExtra("token", token)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //Realmインスタンスの放棄
    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }
}
