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
import io.realm.Case
import io.realm.Realm
import io.realm.kotlin.where
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.IOException

class SearchMessageActivity : RxAppCompatActivity() {
    private lateinit var realm: Realm
    lateinit var toolbar: ActionBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_search)

        val token = intent.getStringExtra("token")

        //Realmを利用するために必要なもの
        realm = Realm.getDefaultInstance()
        var messages = realm.where<Message>().findAll()
        Log.d("SEARCH", "${messages.size}")
        val listView: ListView = findViewById<ListView>(R.id.message_list_view)

        val searchButton = findViewById<Button>(R.id.search_button)
        val searchEditText = findViewById<EditText>(R.id.search_edit_text)
        val listAdapter = MessageListAdapter(messages)

        listView.adapter = listAdapter
        listView.setSelection(listAdapter.messages0.size)

        toolbar = supportActionBar!!
        toolbar.setDisplayHomeAsUpEnabled(true)

        searchButton.setOnClickListener {
            if (searchEditText.text.isEmpty()) {
                return@setOnClickListener
            }
            val keyWord = searchEditText.text.toString()
            messages = realm.where<Message>().contains("content", keyWord, Case.INSENSITIVE).findAll()
            listView.adapter = MessageListAdapter(messages)
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
                val intent = Intent(this, GroupActivity::class.java)
                intent.putExtra("token", token)
                startActivity(intent)
                finishAndRemoveTask()
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
