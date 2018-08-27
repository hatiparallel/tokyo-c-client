package com.tokyoc.line_client

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.content.Intent
import android.widget.TextView
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.GetTokenResult

class SigninActivity : AppCompatActivity() {
    val firebase: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(saveInstanceState: Bundle?) {
        super.onCreate(saveInstanceState)
        setContentView(R.layout.activity_signin)

        val emailEditText: EditText = findViewById<EditText>(R.id.email_edit_text)
        val passwordEditText: EditText = findViewById<EditText>(R.id.password_edit_text)

        findViewById<Button>(R.id.signin_button).setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            //空白でなければ一般のサインイン処理
            if (email.isEmpty() && password.isEmpty()) {
                val intent = Intent(this, MemberActivity::class.java)
                startActivity(intent)
                return@setOnClickListener
            }

            signIn(email, password)
        }

        findViewById<TextView>(R.id.to_signup).setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
    }

    fun signIn(email: String, password: String) {
        firebase.signInWithEmailAndPassword(email, password).addOnCompleteListener { task: Task<AuthResult> ->
            if (!task.isSuccessful) {
                Toast.makeText(applicationContext, "sign in error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                return@addOnCompleteListener
            }

            firebase.currentUser?.getIdToken(true)?.addOnCompleteListener { tokenTask: Task<GetTokenResult> ->
                firebase.currentUser?.displayName
                if (!tokenTask.isSuccessful) {
                    Toast.makeText(applicationContext, "sign in token error: ${tokenTask.exception?.message}", Toast.LENGTH_LONG).show()
                    return@addOnCompleteListener
                }

                val token: String? = tokenTask.result.token

                if (token == null) {
                    Toast.makeText(applicationContext, "sign in: token is null", Toast.LENGTH_LONG).show()
                } else {
                    val intent = Intent(this, MemberActivity::class.java)
                    intent.putExtra("token", token)
                    startActivity(intent)
                }
            }
        }
    }
}