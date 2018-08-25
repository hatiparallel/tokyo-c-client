package com.tokyoc.line_client

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.content.Intent
import android.text.Editable
import android.util.Log
import android.widget.ListView
import android.widget.TextView
import android.widget.EditText

import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder

import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

import com.trello.rxlifecycle.components.support.RxAppCompatActivity
import com.trello.rxlifecycle.kotlin.bindToLifecycle
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

import java.util.*
import okhttp3.Interceptor
import okhttp3.OkHttpClient

class MessageActivity : RxAppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        val token = intent.getStringExtra("token")
        val returnButton = findViewById<Button>(R.id.return_button)
        val sendButton = findViewById<Button>(R.id.send_button)
        val messageEditText = findViewById<EditText>(R.id.message_edit_text)

        val groupName = findViewById<TextView>(R.id.send_user_name_text_view)
        val group: Member = getIntent().getParcelableExtra(MemberActivity.EXTRA_TEXTDATA)
        groupName.text = group.name

        //通信に使うものたちの定義
        val gson = GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .setLenient()
                .create()
        val authenticatedClient = OkHttpClient().newBuilder()
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
        val receiverClient = retrofit.create(ReceiverClient::class.java)
        val senderClient = retrofit.create(SenderClient::class.java)
        val testClient = retrofit.create(TestClient::class.java)
        val getClient = retrofit.create(GetClient::class.java)

        // ボタンをクリックしたらMember画面に遷移
        returnButton.setOnClickListener {
            val intent = Intent(this, MemberActivity::class.java)
            intent.putExtra("token", token)
            startActivity(intent)
        }

        var listAdapter = MessageListAdapter(applicationContext)
        var listView = findViewById<ListView>(R.id.message_list_view)
        listView.adapter = listAdapter
        Log.d("TEST", token)
        // 送信ボタン押したらmessagesリストにMessageオブジェクト追加し、ListViewを更新
        sendButton.setOnClickListener {
            if (messageEditText.text.isEmpty()) {
                return@setOnClickListener
            }

            val sendMessage: Message = Message(textmessage = messageEditText.text.toString(), sender = 0, date = Date())

            listAdapter.messages.add(sendMessage)
            listView.adapter = listAdapter
            messageEditText.setText("", TextView.BufferType.NORMAL)

            //ここから通信部分！

            /*
            val sendergson = Gson()
            val senderjson: String = sendergson.toJson(sendMessage)
            senderClient.sendMessage(senderjson)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        //正常
                    }, {
                        //error
                        Toast.makeText(applicationContext, "dead", Toast.LENGTH_LONG)
                    })
                    */

            val testMessage: TestMessage = TestMessage(Text = "good night")

            testClient.postTest(group.groupId, testMessage)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        Log.d("COMM", "post done: $it")
                    }, {
                        Log.d("COMM", "post failed: $it")
                    })

            getClient.getMessages()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .bindToLifecycle(this)
                    .subscribe({
                        Log.d("COMM", "get done: $it")
                    }, {
                        Log.d("COMM", "get failed: $it")
                    })


            //ここまで通信部分！
        }
    }

    private fun dummyMessage(textmessage: String): Message = Message(textmessage = textmessage, sender = 0, date = Date())
}
