package com.tokyoc.line_client

import android.os.Bundle
import android.widget.Button
import android.content.Intent
import android.icu.text.DateFormat
import android.util.Log
import android.view.View
import android.widget.ListView
import android.widget.TextView
import android.widget.EditText

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder

import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

import com.trello.rxlifecycle.components.support.RxAppCompatActivity
import com.trello.rxlifecycle.kotlin.bindToLifecycle
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.adapter.rxjava.HttpException
import java.io.IOException
import java.util.concurrent.TimeUnit

class MessageActivity : RxAppCompatActivity() {
    private lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_message)

        //Realmを利用するために必要なもの
        realm = Realm.getDefaultInstance()
        val messages = realm.where<Message>().findAll()
        val listView: ListView = findViewById<ListView>(R.id.message_list_view)

        val token = intent.getStringExtra("token")
        val group: Group = intent.getParcelableExtra(GroupActivity.EXTRA_GROUP)

        val returnButton = findViewById<Button>(R.id.return_button)
        val sendButton = findViewById<Button>(R.id.send_button)
        val messageEditText = findViewById<EditText>(R.id.message_edit_text)
        val listAdapter = MessageListAdapter(messages)
        val groupName = findViewById<TextView>(R.id.send_user_name_text_view)

        listView.adapter = listAdapter
        groupName.text = group.name
        listView.setSelection(listAdapter.messages0.size)

        //通信に使うものたちの定義
        val gson = GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                .setLenient()
                .create()
        val authenticatedClient = OkHttpClient().newBuilder()
                .readTimeout(0, TimeUnit.SECONDS)
                .addInterceptor(Interceptor { chain ->
                    chain.proceed(
                            chain.request()
                                    .newBuilder()
                                    .header("Authorization", "Bearer $token")
                                    .build())
                })
                .build()
        val retrofit = Retrofit.Builder()
                .client(authenticatedClient)
                .baseUrl(BuildConfig.BACKEND_BASEURL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build()
        val client = retrofit.create(Client::class.java)
        var since_id = realm.where<Message>().max("id")

        Log.d("COMM", "token: $token")
        Log.d("COMM", "listening /streams/${group.groupId}")

        if (since_id != null) {
            client.getMessages(group.groupId, since_id.toInt())
                    .flatMap {
                        val source = it.source()

                        rx.Observable.create(rx.Observable.OnSubscribe<Message> {
                            try {
                                while (!source.exhausted()) {
                                    it.onNext(gson.fromJson<Message>(source.readUtf8Line(), Message::class.java))
                                }

                                it.onCompleted()
                            } catch (e: IOException) {
                                it.onError(e)
                            }
                        })
                    }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .bindToLifecycle(this)
                    .subscribe(
                            {
                                val message0 = it
                                realm.executeTransaction {
                                    val message = realm.createObject<Message>(message0.id)
                                    message.content = message0.content
                                    message.author = message0.author
                                    message.channel = message0.channel
                                    message.isEvent = message0.isEvent
                                    message.postedAt = message0.postedAt
                                    Log.d("COMM", "registered: ${message0.id}")
                                }
                                listView.setSelection(listAdapter.messages0.size)
                                Log.d("COMM", "received")
                            },
                            {
                                Log.d("COMM", "receive failed: $it")
                            })
        }

        // ボタンをクリックしたらGroup画面に遷移
        returnButton.setOnClickListener {
            val intent = Intent(this, GroupActivity::class.java)
            intent.putExtra("token", token)
            startActivity(intent)
        }

        sendButton.setOnClickListener {
            if (messageEditText.text.isEmpty()) {
                return@setOnClickListener
            }

            //通信部分の準備
            val message: Message = Message()
            message.content = messageEditText.text.toString()
            messageEditText.setText("", TextView.BufferType.NORMAL)

            //ここから通信部分！
            Log.d("COMM", gson.toJson(message))

            client.sendMessage(group.groupId, message) //channel番号はgetExtraから本来は読み込む
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        Log.d("COMM", "post done: ${it}")
                    }, {
                        Log.d("COMM", "post failed: ${it}")
                    })
        }
    }
    //Realmインスタンスを破棄
    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }
}
