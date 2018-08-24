package com.tokyoc.line_client

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.content.Intent
import android.text.Editable
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
import android.widget.Toast

//書き換える
const val baseurl: String = "http://192.168.56.1:80"

class MessageActivity : RxAppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

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
        val retrofit = Retrofit.Builder()
                .baseUrl(baseurl)
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
            startActivity(intent)
        }

        var listAdapter = MessageListAdapter(applicationContext)
        var listView = findViewById<ListView>(R.id.message_list_view)
        listView.adapter = listAdapter

        // 送信ボタン押したらmessagesリストにMessageオブジェクト追加し、ListViewを更新
        sendButton.setOnClickListener {
            if (messageEditText.text.isNotEmpty()) {
                val sendMessage: Message = Message(content=messageEditText.text.toString())
                listAdapter.messages.add(sendMessage)
                listView.adapter = listAdapter
                messageEditText.setText("", TextView.BufferType.NORMAL)

                //ここから通信部分！

                senderClient.sendMessage(channel=1, message=sendMessage) //channel番号はgetExtraから本来は読み込む
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            //正常
                        }, {
                            //error
                            Toast.makeText(applicationContext, "dead", Toast.LENGTH_LONG)
                        })

                val testMessage: TestMessage = TestMessage(Text="good night")



                testClient.postTest(group.groupId, testMessage)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            //Toast.makeText(applicationContext, "done, $it", Toast.LENGTH_LONG).show()
                        }, {
                            Toast.makeText(applicationContext, "sorry, dead. $it", Toast.LENGTH_LONG).show()
                        })

                getClient.getMessages()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .bindToLifecycle(this)
                        .subscribe({
                            Toast.makeText(applicationContext, "done, ${it.Text}", Toast.LENGTH_LONG).show()
                        }, {
                            Toast.makeText(applicationContext, "sorry, $it", Toast.LENGTH_LONG).show()
                        })

                
                //ここまで通信部分！



            } else {
                /*
                //サーバからのMessage受け取り。実際は常時スレッドを立てる。
                //現状ではテキスト空欄で送信ボタンを押したときだけ動作。テスト用。
                receiverClient.getMessages(1) //実際は相手の識別番号
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            val messagesReceived: List<Message> = it
                            listAdapter.messages.plus(messagesReceived)
                        }, {
                            //error処理
                            Toast.makeText(applicationContext, "dead", Toast.LENGTH_LONG)
                        })
                        */
            }
        }
    }
}
