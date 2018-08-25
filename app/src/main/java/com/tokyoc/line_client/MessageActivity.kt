package com.tokyoc.line_client

import android.os.Bundle
import android.widget.Button
import android.content.Intent
import android.icu.text.DateFormat
import android.util.Log
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
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.adapter.rxjava.HttpException
import java.io.IOException
import java.util.concurrent.TimeUnit

class MessageActivity : RxAppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        val token = intent.getStringExtra("token")
        val returnButton = findViewById<Button>(R.id.return_button)
        val sendButton = findViewById<Button>(R.id.send_button)
        val messageEditText = findViewById<EditText>(R.id.message_edit_text)
        val listAdapter = MessageListAdapter(applicationContext)
        val listView = findViewById<ListView>(R.id.message_list_view)
        val groupName = findViewById<TextView>(R.id.send_user_name_text_view)
        val group: Member = intent.getParcelableExtra(MemberActivity.EXTRA_TEXTDATA)

        listView.adapter = listAdapter
        groupName.text = group.name

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
        val senderClient = retrofit.create(SenderClient::class.java)

        Log.d("COMM", "token: $token")
        Log.d("COMM", "listening /streams/${group.groupId}")

        retrofit.create(ReceiverClient::class.java).getMessages(group.groupId)
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
                            listAdapter.messages.add(it)
                            listView.adapter = listAdapter
                            messageEditText.setText("", TextView.BufferType.NORMAL)
                        },
                        {
                            Log.d("COMM", "receive failed: $it")
                        })

        // ボタンをクリックしたらMember画面に遷移
        returnButton.setOnClickListener {
            val intent = Intent(this, MemberActivity::class.java)
            intent.putExtra("token", token)
            startActivity(intent)
        }

        sendButton.setOnClickListener {
            if (messageEditText.text.isEmpty()) {
                return@setOnClickListener
            }

            val message: Message = Message(content = messageEditText.text.toString())
            //ここから通信部分！

            Log.d("COMM", gson.toJson(message))

            senderClient.sendMessage(group.groupId, message) //channel番号はgetExtraから本来は読み込む
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        Log.d("COMM", "post done: ${it}")
                    }, {
                        Log.d("COMM", "post failed: ${it}")
                    })

            //ここまで通信部分！
        }
    }
}
