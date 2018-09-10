package com.tokyoc.line_client

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.google.firebase.storage.FirebaseStorage


class ProfileActivity : AppCompatActivity() {
    val storage: FirebaseStorage = FirebaseStorage.getInstance()

    override fun onCreate(saveInstanceState: Bundle?) {
        super.onCreate(saveInstanceState)
        setContentView(R.layout.activity_profile)

        val token = intent.getStringExtra("token")

        val nameEditText: EditText = findViewById<EditText>(R.id.name_edit_text)
        val emailEditText: EditText = findViewById<EditText>(R.id.email_edit_text)
        val passwordEditText: EditText = findViewById<EditText>(R.id.password_edit_text)
        val againPasswordEditText: EditText = findViewById<EditText>(R.id.again_password_edit_text)

        findViewById<Button>(R.id.change_image_button).setOnClickListener() {
            val intent = Intent(this, ChangeImageActivity::class.java)
            intent.putExtra("token", token)
            startActivity(intent)
        }

        findViewById<Button>(R.id.decide_button).setOnClickListener() {
            val name: String = nameEditText.text.toString()
            val email: String = emailEditText.text.toString()
            val password: String = passwordEditText.text.toString()
            val againPassword: String = passwordEditText.text.toString()

            if (password.isNotEmpty()) {

            }

            if (name.isNotEmpty()) {

            }

            if (email.isNotEmpty()) {

            }


        }

        findViewById<TextView>(R.id.return_button).setOnClickListener() {
            val intent = Intent(this, MemberActivity::class.java)
            intent.putExtra("token", token)
            startActivity(intent)
        }
    }
}