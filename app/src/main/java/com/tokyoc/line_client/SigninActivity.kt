package com.tokyoc.line_client

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.content.Intent

class SigninActivity: AppCompatActivity() {
    override fun onCreate(saveInstanceState: Bundle?) {
        super.onCreate(saveInstanceState)
        setContentView(R.layout.activity_signin)

        val signinButton = findViewById<Button>(R.id.signin_button)

        signinButton.setOnClickListener() {
            val intent = Intent(this, MemberActivity::class.java)
            startActivity(intent)
        }
    }
}