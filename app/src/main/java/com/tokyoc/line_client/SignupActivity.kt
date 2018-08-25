package com.tokyoc.line_client

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Button
import android.content.Intent
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GetTokenResult

class SignupActivity : AppCompatActivity() {
    val firebase: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(saveInstanceState: Bundle?) {
        super.onCreate(saveInstanceState)
        setContentView(R.layout.activity_signup)

        val userEditText: EditText = findViewById<EditText>(R.id.user_edit_text)
        val emailEditText: EditText = findViewById<EditText>(R.id.email_edit_text)
        val passwordEditText: EditText = findViewById<EditText>(R.id.password_edit_text)

        findViewById<TextView>(R.id.to_signin).setOnClickListener() {
            val intent = Intent(this, SigninActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.signup_button).setOnClickListener() {
            val user: String = userEditText.text.toString()
            val email: String = emailEditText.text.toString()
            val password: String = passwordEditText.text.toString()

            if (user.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(applicationContext, "any field cannot be empty", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            signUp(email, password)
        }
    }

    fun signUp(email: String, password: String) {
        firebase.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task: Task<AuthResult> ->
            if (!task.isSuccessful) {
                Toast.makeText(applicationContext, "sign up error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                return@addOnCompleteListener
            }

            firebase.currentUser?.getIdToken(true)?.addOnCompleteListener { tokenTask: Task<GetTokenResult> ->
                if (!tokenTask.isSuccessful) {
                    Toast.makeText(applicationContext, "sign up token error: ${tokenTask.exception?.message}", Toast.LENGTH_LONG).show()
                    return@addOnCompleteListener
                }

                val token: String? = tokenTask.getResult().token

                if (token == null) {
                    Toast.makeText(applicationContext, "token is null", Toast.LENGTH_LONG).show()
                } else {
                    val intent = Intent(this, MemberActivity::class.java)
                    intent.putExtra("token", token)
                    startActivity(intent)
                }
            }
        }
    }
}