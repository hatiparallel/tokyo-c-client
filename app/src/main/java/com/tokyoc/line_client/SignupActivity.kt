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
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GetTokenResult
import com.google.firebase.auth.UserProfileChangeRequest



class SignupActivity : AppCompatActivity() {
    val firebase: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(saveInstanceState: Bundle?) {
        super.onCreate(saveInstanceState)
        setContentView(R.layout.activity_signup)

        val nameEditText: EditText = findViewById<EditText>(R.id.name_edit_text)
        val emailEditText: EditText = findViewById<EditText>(R.id.email_edit_text)
        val passwordEditText: EditText = findViewById<EditText>(R.id.password_edit_text)

        findViewById<TextView>(R.id.to_signin).setOnClickListener() {
            val intent = Intent(this, SigninActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.signup_button).setOnClickListener() {
            val name: String = nameEditText.text.toString()
            val email: String = emailEditText.text.toString()
            val password: String = passwordEditText.text.toString()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(applicationContext, "any field cannot be empty", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            signUp(name, email, password)
        }
    }

    fun signUp(name: String, email: String, password: String) {
        firebase.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task: Task<AuthResult> ->
            if (!task.isSuccessful) {
                Toast.makeText(applicationContext, "sign up error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                return@addOnCompleteListener
            }

            val user: FirebaseUser? = firebase.currentUser
            val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
            user?.updateProfile(profileUpdates)?.addOnCompleteListener { profileTask ->
                if (!profileTask.isSuccessful) {
                    Toast.makeText(applicationContext, "sign up profile error: ${profileTask.exception?.message}", Toast.LENGTH_LONG).show()
                    return@addOnCompleteListener
                }

                user?.getIdToken(true)?.addOnCompleteListener { tokenTask: Task<GetTokenResult> ->
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
}