package com.tokyoc.line_client

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.google.firebase.storage.FirebaseStorage
import io.realm.Realm
import io.realm.kotlin.where
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers


class ChangeGroupNameActivity : AppCompatActivity() {
    val firebaseUser = FirebaseAuth.getInstance().currentUser
    private lateinit var realm: Realm
    private var groupId: Int = 0

    override fun onCreate(saveInstanceState: Bundle?) {
        super.onCreate(saveInstanceState)
        setContentView(R.layout.activity_name_change)

        val toolbar = supportActionBar!!
        toolbar.setDisplayHomeAsUpEnabled(true)

        groupId = intent.getIntExtra("groupId", 0)
        realm = Realm.getDefaultInstance()
        val group = realm.where<Group>().equalTo("id", groupId).findFirst()
        findViewById<TextView>(R.id.name_view).text = group?.name ?: "取得失敗"
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_name_change, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val token = intent.getStringExtra("token")
        val nameEditText = findViewById<EditText>(R.id.new_name_edit_text)

        val group = realm.where<Group>().equalTo("id", groupId).findFirst()
        findViewById<TextView>(R.id.name_view).text = group?.name ?: "取得失敗"

        when (item?.itemId) {
            android.R.id.home -> {
                val intent = Intent(this, GroupProfileActivity::class.java)
                intent.putExtra("token", token)
                intent.putExtra("groupId", groupId)
                startActivity(intent)
            }
            R.id.change_name -> {
                val newName = nameEditText.text.toString()
                if (newName.isEmpty()) {
                    return false
                }
                val client = Client.build(token)
                client.renameGroup(groupId, newName)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            Log.d("COMM", "change group name success: ${it}")
                            realm = Realm.getDefaultInstance()
                            val group = realm.where<Group>().equalTo("id", groupId).findFirst()
                            realm.executeTransaction {
                                group?.name = newName
                            }
                            val intent = Intent(this, GroupProfileActivity::class.java)
                            intent.putExtra("token", token)
                            intent.putExtra("groupId", groupId)
                            startActivity(intent)
                        }, {
                            Log.d("COMM", "change group name failure: ${it}")
                        })
            }
        }
        return super.onOptionsItemSelected(item)
    }
}