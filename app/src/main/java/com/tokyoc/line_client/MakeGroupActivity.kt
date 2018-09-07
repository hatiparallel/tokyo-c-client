package com.tokyoc.line_client

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.EditText
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.trello.rxlifecycle.components.support.RxAppCompatActivity
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_friend_add.*
import kotlinx.android.synthetic.main.activity_group_make.*
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.concurrent.TimeUnit


class MakeGroupActivity: RxAppCompatActivity() {

    private lateinit var realm: Realm

    override fun onCreate(saveInstanceState: Bundle?) {
        super.onCreate(saveInstanceState)
        setContentView(R.layout.activity_group_make)
        realm = Realm.getDefaultInstance()

        val token = intent.getStringExtra("token")

        val groupNameEditText = findViewById<EditText>(R.id.group_name)

        val client = Client.build(token)

        //グループ作成ボタンを押した時の処理
        findViewById<Button>(R.id.make_group).setOnClickListener {
            if (groupNameEditText.text.isEmpty()) {
                return@setOnClickListener
            }

            val groupName = groupNameEditText.text.toString()
            val new_group = Group()

            client.makeGroup(new_group)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        Log.d("COMM", "post done: ${it}")
                        val groupId = it.groupId
                        realm.executeTransaction {
                            val maxId = realm.where<Group>().max("id")
                            val nextId = (maxId?.toLong() ?: 0L) + 1
                            val group = realm.createObject<Group>(nextId)
                            group.name = group_name.text.toString()
                            group.groupId = groupId
                        }
                        val intent = Intent(this, GroupActivity::class.java)
                        intent.putExtra("token", token)
                        startActivity(intent)
                    }, {
                        Log.d("COMM", "post failed: ${it}")
                    })
        }

        //戻るボタンを押した時の処理
        findViewById<Button>(R.id.to_friendship_button).setOnClickListener {
            val intent = Intent(this, GroupActivity::class.java)
            intent.putExtra("token", token)
            startActivity(intent)
        }
    }

    //Realmインスタンスの放棄
    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }
}
