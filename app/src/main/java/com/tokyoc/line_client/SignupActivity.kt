package com.tokyoc.line_client

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Button
import android.content.Intent


class SignupActivity: AppCompatActivity() {
    override fun onCreate(saveInstanceState: Bundle?) {
        super.onCreate(saveInstanceState)
        setContentView(R.layout.activity_signup)

        val toSignin = findViewById<TextView>(R.id.to_signin)
        toSignin.setOnClickListener() {
            val intent = Intent(this, SigninActivity::class.java)
            startActivity(intent)
        }

        val signupButton = findViewById<Button>(R.id.signup_button)
        signupButton.setOnClickListener() {
            val intent = Intent(this, MemberActivity::class.java)
            startActivity(intent)
        }
    }
}