package com.tokyoc.line_client

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.content.Intent
import android.widget.ListView
import android.widget.TextView
import android.widget.EditText

class MessageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        val returnButton = findViewById<Button>(R.id.return_button)
        val sendButton = findViewById<Button>(R.id.send_button)
        val messageEditText = findViewById<EditText>(R.id.message_edit_text)

        // ボタンをクリックしたらMember画面に遷移
        returnButton.setOnClickListener {
            val intent = Intent(this, MemberActivity::class.java)
            startActivity(intent)
        }

        var listAdapter = MessageListAdapter(applicationContext)
        var listView = findViewById<ListView>(R.id.message_list_view)

        // 送信ボタン押したらmessagesリストにMessageオブジェクト追加し、ListViewを更新
        sendButton.setOnClickListener {
            if (messageEditText.text != null) {
                var sendMessage: Message = Message(textmessage=messageEditText.text.toString())
                listAdapter.messages.add(sendMessage)
                listView.adapter = listAdapter
                messageEditText.text = null
            }
        }

        var sendUser = findViewById<TextView>(R.id.send_user_name_text_view)
        sendUser.text = intent.getStringExtra(MemberActivity.EXTRA_TEXTDATA)
    }
    private fun dummyMessage(textmessage: String): Message = Message(textmessage=textmessage)
}
