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
        val listView: ListView = findViewById<ListView>(R.id.message_list_view)

        val searchButton = findViewById<Button>(R.id.search_button)
        val searchEditText = findViewById<EditText>(R.id.search_edit_text)

        val radioGroup = findViewById<RadioGroup>(R.id.radio_group)
        radioGroup.check(R.id.radio_all)
        var searchFilter = 0

        radioGroup.setOnCheckedChangeListener { _, checked ->
            val radioButton = findViewById<RadioButton>(checked)
            searchFilter = when(radioButton.id) {
                R.id.radio_all -> 0
                R.id.radio_notEvent -> 1
                R.id.radio_importantOrFavorite -> 2
                R.id.radio_onlyFavorite -> 3
                else -> 0
            }
            if (searchEditText.text.isEmpty()) {
                messages = when(searchFilter) {
                    0 -> realm.where<Message>().findAll()
                    1 -> realm.where<Message>().notEqualTo("isEvent", 1.toInt()).findAll()
                    2 -> realm.where<Message>().greaterThanOrEqualTo("isEvent", 2.toInt()).findAll()
                    3 -> realm.where<Message>().greaterThan("isEvent", 2.toInt()).findAll()
                    else -> realm.where<Message>().findAll()
                }
            } else {
                val keyWord = searchEditText.text.toString()
                messages = when(searchFilter) {
                    0 -> realm.where<Message>().contains("content", keyWord, Case.INSENSITIVE).findAll()
                    1 -> realm.where<Message>().notEqualTo("isEvent", 1.toInt())
                            .contains("content", keyWord, Case.INSENSITIVE).findAll()
                    2 -> realm.where<Message>().greaterThanOrEqualTo("isEvent", 2.toInt())
                            .contains("content", keyWord, Case.INSENSITIVE).findAll()
                    3 -> realm.where<Message>().greaterThan("isEvent", 2.toInt())
                            .contains("content", keyWord, Case.INSENSITIVE).findAll()
                    else -> realm.where<Message>().contains("content", keyWord, Case.INSENSITIVE).findAll()
                }
            }
            listView.adapter = SearchMessageListAdapter(messages)
        }

        val listAdapter = SearchMessageListAdapter(messages)

        listView.adapter = listAdapter
        listView.setSelection(listAdapter.messages0.size)

        toolbar = supportActionBar!!
        toolbar.setDisplayHomeAsUpEnabled(true)

        searchButton.setOnClickListener {
            if (searchEditText.text.isEmpty()) {
                if (searchFilter == 0) {
                    messages = realm.where<Message>().findAll()
                } else if (searchFilter == 1) {
                    messages = realm.where<Message>().notEqualTo("isEvent", 1.toInt()).findAll()
                } else if (searchFilter == 2) {
                    messages = realm.where<Message>().greaterThanOrEqualTo("isEvent", 2.toInt()).findAll()
                } else if (searchFilter == 3) {
                    messages = realm.where<Message>().greaterThan("isEvent", 2.toInt()).findAll()
                }
            } else {
                val keyWord = searchEditText.text.toString()
                if (searchFilter == 0) {
                    messages = realm.where<Message>().contains("content", keyWord, Case.INSENSITIVE).findAll()
                } else if (searchFilter == 1) {
                    messages = realm.where<Message>().notEqualTo("isEvent", 1.toInt())
                            .contains("content", keyWord, Case.INSENSITIVE).findAll()
                } else if (searchFilter == 2) {
                    messages = realm.where<Message>().greaterThanOrEqualTo("isEvent", 2.toInt())
                            .contains("content", keyWord, Case.INSENSITIVE).findAll()
                } else if (searchFilter == 3) {
                    messages = realm.where<Message>().greaterThan("isEvent", 2.toInt())
                            .contains("content", keyWord, Case.INSENSITIVE).findAll()
                }
            }
            listView.adapter = SearchMessageListAdapter(messages)
        }

        listView.setOnItemClickListener { adapterView, view, position, id ->
            val group = realm.where<Group>()
                    .equalTo("id", messages[position]?.channel).findFirst() ?: return@setOnItemClickListener

            val message = Intent()
            message.action = "POLLING_CONTROL"
            message.putExtra("suppress", group.id)
            sendBroadcast(message)

            val intent = Intent(this, MessageActivity::class.java)
            intent.putExtra("groupId", group.id)
            intent.putExtra("token", getIntent().getStringExtra("token"))
            intent.putExtra("target", messages[position]?.id)
            intent.putExtra("flag", 1)
            startActivity(intent)
        }

        listView.setOnItemLongClickListener { adapterView, view, position, id ->
            if (messages[position] != null) {
                if (messages[position]!!.isEvent < 3) {
                    realm.executeTransaction {
                        messages[position]!!.isEvent += 3
                    }
                } else {
                    realm.executeTransaction {
                        messages[position]!!.isEvent -= 3
                    }
                }
            }
            return@setOnItemLongClickListener true
        }

    }

    // メニューをタップした時の処理
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val token = intent.getStringExtra("token")
        val groupId: Int = intent.getIntExtra("groupId", 0)

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

    override fun onResume() {
        super.onResume()

        val message = Intent()
        message.action = "POLLING_CONTROL"
        message.putExtra("suppress", -1)
        sendBroadcast(message)
    }

    //Realmインスタンスを破棄
    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }
}
