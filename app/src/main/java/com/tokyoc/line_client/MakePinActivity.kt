package com.tokyoc.line_client

import android.content.DialogInterface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.content.Intent
import android.support.v7.app.AlertDialog
import android.util.Log
import android.widget.TextView
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.GetTokenResult
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.trello.rxlifecycle.components.support.RxAppCompatActivity
import com.trello.rxlifecycle.kotlin.bindToLifecycle
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.IOException
import java.util.concurrent.TimeUnit

class MakePinActivity : RxAppCompatActivity() {
    private lateinit var realm: Realm

    override fun onCreate(saveInstanceState: Bundle?) {
        super.onCreate(saveInstanceState)
        setContentView(R.layout.activity_pin_make)

        realm = Realm.getDefaultInstance()

        val pin_show = findViewById<TextView>(R.id.pin)

        val token = intent.getStringExtra("token")

        val client = Client.build(token)

        client.getPIN()
                .onBackpressureBuffer()
                .flatMap {
                    val source = it.source()

                    rx.Observable.create(rx.Observable.OnSubscribe<PinEvent> {
                        try {
                            while (!source.exhausted()) {
                                it.onNext(Client.gson.fromJson<PinEvent>(source.readUtf8Line(), PinEvent::class.java))
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
                            if (it.type == "pin") {
                                pin_show.text = it.pin.toString()
                            } else if (it.type == "request") {
                                val uid: String = it.person
                                client.getPerson(uid)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe({
                                            val member0: Member = it
                                            Log.d("COMM", "get person done: ${it.name}")
                                            AlertDialog.Builder(this).apply {
                                                setTitle("Friend Request")
                                                setMessage("Make Friends with ${it.name}?")
                                                setPositiveButton("OK", DialogInterface.OnClickListener { _, _ ->
                                                    client.makeFriends(uid)
                                                            .subscribeOn(Schedulers.io())
                                                            .observeOn(AndroidSchedulers.mainThread())
                                                            .subscribe({
                                                                Log.d("COMM", "make friends succeeded: ${it.size}")
                                                                realm.executeTransaction {
                                                                    val maxId = realm.where<Member>().max("id")
                                                                    val nextId = (maxId?.toLong() ?: 0L) + 1
                                                                    val member = realm.createObject<Member>(nextId)
                                                                    member.userId = member0.userId
                                                                    member.name = member0.name
                                                                    member.photo = member0.photo
                                                                    Log.d("COMM", "registered: ${member.name}")
                                                                }
                                                            }, {
                                                                Log.d("COMM", "make friends failed: ${it}")
                                                            })
                                                })
                                                setNegativeButton("Cancel", null)
                                                show()
                                            }
                                        }, {
                                            Log.d("COMM", "get person failed: ${it}")
                                        })
                            }
                        },
                        {
                            Log.d("COMM", "get_pin receive failed: $it")
                            val intent = Intent(this, SendPinActivity::class.java)
                            AlertDialog.Builder(this).apply {
                                setTitle("Error")
                                setMessage("サーバとの通信に失敗しました。\n一旦戻ってから再度PINを発行してください。")
                                setPositiveButton("OK", DialogInterface.OnClickListener { _, _ ->
                                    intent.putExtra("token", token)
                                    startActivity(intent)
                                })
                                setNegativeButton("Cancel", null)
                                show()
                            }
                        })

        findViewById<Button>(R.id.return_button).setOnClickListener {
            val intent = Intent(this, MemberActivity::class.java)
            intent.putExtra("token", token)
            startActivity(intent)
        }
    }
}