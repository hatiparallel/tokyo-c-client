package com.tokyoc.line_client

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.*
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.google.firebase.storage.FirebaseStorage
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.bumptech.glide.Glide
import com.google.firebase.storage.StorageReference


class ProfileActivity : AppCompatActivity() {
    val storageRef = FirebaseStorage.getInstance().reference

    override fun onCreate(saveInstanceState: Bundle?) {
        super.onCreate(saveInstanceState)
        setContentView(R.layout.activity_profile)

        val token = intent.getStringExtra("token")

        findViewById<Button>(R.id.change_name_button).setOnClickListener() {
            val intent = Intent(this, ChangeNameActivity::class.java)
            intent.putExtra("token", token)
            startActivity(intent)
        }

        val photo = findViewById<ImageView>(R.id.photo_view)
        /*
        val imageRef = storageRef.child("images/{Uid}.jpg")

        Glide.with(this /* context */)
                .using<StorageReference>(FirebaseImageLoader())
                .load(imageRef)
                .into(photo)
                */

        findViewById<Button>(R.id.change_image_button).setOnClickListener() {
            val intent = Intent(this, ChangeImageActivity::class.java)
            intent.putExtra("token", token)
            startActivity(intent)
        }

        findViewById<TextView>(R.id.return_button).setOnClickListener() {
            val intent = Intent(this, MemberActivity::class.java)
            intent.putExtra("token", token)
            startActivity(intent)
        }
    }
}