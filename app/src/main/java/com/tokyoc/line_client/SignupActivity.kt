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


class SignupActivity: AppCompatActivity() {
    val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(saveInstanceState: Bundle?) {
        super.onCreate(saveInstanceState)
        setContentView(R.layout.activity_signup)

        val userEditText: EditText = findViewById<EditText>(R.id.user_edit_text)
        val addressEditText: EditText = findViewById<EditText>(R.id.address_edit_text)
        val passwordEditText: EditText = findViewById<EditText>(R.id.password_edit_text)

        val toSignin = findViewById<TextView>(R.id.to_signin)
        toSignin.setOnClickListener() {
            val intent = Intent(this, SigninActivity::class.java)
            startActivity(intent)
        }

        val signupButton = findViewById<Button>(R.id.signup_button)
        signupButton.setOnClickListener() {
            val user: String = userEditText.text.toString()
            val address: String = addressEditText.text.toString()
            val password: String = passwordEditText.text.toString()

            if (user.isNotEmpty() && address.isNotEmpty() && password.isNotEmpty()) { //とりあえず空白だけ拒否
                signUp(address, password)
            }
        }
    }


    fun signUp(email: String, password: String) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task: Task<AuthResult> ->
                    if (task.isSuccessful) {
                        val mUser: FirebaseUser? = mAuth.getCurrentUser()
                        mUser?.getIdToken(true)
                                ?.addOnCompleteListener { tokenTask: Task<GetTokenResult> ->
                                    if (tokenTask.isSuccessful) {
                                        val mToken: String? = tokenTask.getResult().token
                                        if (mToken != null) {
                                            Toast.makeText(applicationContext, "signUp succeeded", Toast.LENGTH_LONG).show()
                                            val intent = Intent(this, MemberActivity::class.java)
                                            intent.putExtra("token", mToken)
                                            startActivity(intent)
                                        } else { //token is null
                                            Toast.makeText(applicationContext, "token is null", Toast.LENGTH_LONG).show()
                                        }
                                    } else { //could register, but could not get token
                                        Toast.makeText(applicationContext, "sign up token error: ${tokenTask.exception?.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                    } else { //could not register with email and password
                        Toast.makeText(applicationContext, "sign up error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
    }
}