package com.tokyoc.line_client

import android.content.DialogInterface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ListView
import android.widget.Button
import android.widget.Toast
import android.content.Intent
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.KeyEvent
import com.google.gson.GsonBuilder

import com.google.firebase.auth.FirebaseAuth
import com.google.gson.FieldNamingPolicy
import io.realm.Realm
import io.realm.kotlin.where
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class InviteActivity : AppCompatActivity() {
    private lateinit var realm: Realm

    companion object {
        const val EXTRA_MEMBER = "member"
    }

    val token = intent.getStringExtra("token")
    val group: Group = intent.getParcelableExtra("group")

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_member)

        //Realmを利用するために必要なもの
        realm = Realm.getDefaultInstance()
        val members = realm.where<Member>().findAll()
        val listView: ListView = findViewById(R.id.member_list_view)
        val listAdapter = MemberListAdapter(members)
        listView.adapter = listAdapter

        //Memberを長押しした時の処理
        listView.setOnItemClickListener { adapterView, view, position, id ->
            val memberInvite = adapterView.getItemAtPosition(position) as Member
            AlertDialog.Builder(this).apply {
                setTitle("Delete Friend")
                setMessage("Really Invite ${memberInvite.name}?")
                setPositiveButton("OK", DialogInterface.OnClickListener { _, _ ->
                    Log.d("COMM", "will invite ${memberInvite.name}")
                    client.invitePerson(group.groupId, memberInvite.userId)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({
                                Log.d("COMM", "post done: ${it}")
                            }, {
                                Log.d("COMM", "post failed: ${it}")
                            })
                })
                setNegativeButton("Cancel", null)
                show()
            }
        }


        //groupボタンを押した時の処理
        findViewById<Button>(R.id.group_button).setOnClickListener {
            val intent = Intent(this, GroupActivity::class.java)
            intent.putExtra("token", getIntent().getStringExtra("token"))
            startActivity(intent)
        }
    }

}
