package com.tokyoc.line_client

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.*
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.google.firebase.storage.FirebaseStorage
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.google.firebase.storage.StorageReference
import io.realm.Realm
import io.realm.kotlin.where


class ProfileActivity : AppCompatActivity() {
    private lateinit var realm: Realm
    val storageRef = FirebaseStorage.getInstance().reference
    val firebaseUser = FirebaseAuth.getInstance().currentUser

    override fun onCreate(saveInstanceState: Bundle?) {
        super.onCreate(saveInstanceState)
        setContentView(R.layout.activity_profile)

        val token = intent.getStringExtra("token")

        realm = Realm.getDefaultInstance()
        val self = realm.where<Member>().equalTo("isFriend", 0.toInt()).findFirst()
        findViewById<TextView>(R.id.name_view).text = self?.name ?: "取得失敗"

        findViewById<Button>(R.id.change_name_button).setOnClickListener() {
            val intent = Intent(this, ChangeNameActivity::class.java)
            intent.putExtra("token", token)
            startActivity(intent)
        }

        val photo = findViewById<ImageView>(R.id.photo_view)

        val uri = self?.photo ?: "https://firebasestorage.googleapis.com/v0/b/tokyo-c-client.appspot.com/o/a.jpg?alt=media&token=8534f22a-d164-40fa-8cd1-1d3e6b5a494c"
        Log.d("COMM", "uri: ${uri}")

        Glide.with(this)
                .load(uri)
                .fitCenter()
                .into(photo)


        findViewById<Button>(R.id.change_image_button).setOnClickListener() {
            val intent = Intent(this, ChangeImageActivity::class.java)
            intent.putExtra("token", token)
            startActivity(intent)
        }

        findViewById<TextView>(R.id.return_button).setOnClickListener() {
            val intent = Intent(this, SettingActivity::class.java)
            intent.putExtra("token", token)
            startActivity(intent)
        }
    }
}