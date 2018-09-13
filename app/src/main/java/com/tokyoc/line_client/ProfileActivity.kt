package com.tokyoc.line_client

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
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
        val toolbar = supportActionBar!!
        toolbar.setDisplayHomeAsUpEnabled(true)

        realm = Realm.getDefaultInstance()
        val self = realm.where<Member>().equalTo("isFriend", 0.toInt()).findFirst()
        findViewById<TextView>(R.id.name_view).text = self?.name ?: "取得失敗"

        if (self != null && self.image.size > 0) {
            findViewById<ImageView>(R.id.photo_view)
                    .setImageBitmap(BitmapFactory.decodeByteArray(self.image, 0, self.image.size))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_profile, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val token = intent.getStringExtra("token")
        when (item?.itemId) {
            android.R.id.home -> {
                val intent = Intent(this, SettingActivity::class.java)
                intent.putExtra("token", token)
                startActivity(intent)
            }
            R.id.change_image -> {
                val intent = Intent(this, ChangeImageActivity::class.java)
                intent.putExtra("token", token)
                startActivity(intent)
            }
            R.id.change_name -> {
                val intent = Intent(this, ChangeNameActivity::class.java)
                intent.putExtra("token", token)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}