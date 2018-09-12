package com.tokyoc.line_client

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GetTokenResult
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where

class SigninActivity : AppCompatActivity() {
    private lateinit var realm: Realm
    val firebase: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(saveInstanceState: Bundle?) {
        super.onCreate(saveInstanceState)
        setContentView(R.layout.activity_signin)

        realm = Realm.getDefaultInstance()

        val emailEditText: EditText = findViewById<EditText>(R.id.email_edit_text)
        val passwordEditText: EditText = findViewById<EditText>(R.id.password_edit_text)

        findViewById<Button>(R.id.signin_button).setOnClickListener {
            signIn(emailEditText.text.toString(), passwordEditText.text.toString())
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
                    val user = firebase.currentUser
                    if (user != null) {
                        val self = realm.where<Member>().equalTo("isFriend", 0.toInt()).findFirst()
                        if (self?.id != user.uid) {
                            realm.executeTransaction {
                                realm.deleteAll()
                                val self: Member = realm.createObject<Member>(user.uid)
                                self.name = user?.displayName ?: "default"
                                self.isFriend = Relation.SELF
                                self.updateImage()
                            }
                        }
                        val intent = Intent(this, GroupActivity::class.java)
                        intent.putExtra("token", token)
                        startActivity(intent)

                        val service = Intent(this, PollingService::class.java)
                        service.putExtra("token", token)
                        startService(service)
                    } else {
                        Log.d("COMM", "could not get current user")
                    }
                }
            }
        }
    }
}