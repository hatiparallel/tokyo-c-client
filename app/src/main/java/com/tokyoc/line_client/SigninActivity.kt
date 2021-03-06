package com.tokyoc.line_client

import android.app.ProgressDialog
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
    private val firebase: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(saveInstanceState: Bundle?) {
        super.onCreate(saveInstanceState)
        setContentView(R.layout.activity_signin)

        realm = Realm.getDefaultInstance()

        findViewById<Button>(R.id.signin_button).setOnClickListener {
            findViewById<EditText>(R.id.email_edit_text).setEnabled(false)
            findViewById<EditText>(R.id.password_edit_text).setEnabled(false)
            findViewById<Button>(R.id.signin_button).setEnabled(false)
            findViewById<TextView>(R.id.to_signup).setEnabled(false)

            signIn(
                    findViewById<EditText>(R.id.email_edit_text).text.toString(),
                    findViewById<EditText>(R.id.password_edit_text).text.toString())
        }

        findViewById<TextView>(R.id.to_signup).setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
    }

    fun signIn(email: String, password: String) {
        firebase.signInWithEmailAndPassword(email, password).addOnCompleteListener { task: Task<AuthResult> ->
            findViewById<EditText>(R.id.email_edit_text).setEnabled(true)
            findViewById<EditText>(R.id.password_edit_text).setEnabled(true)
            findViewById<Button>(R.id.signin_button).setEnabled(true)
            findViewById<TextView>(R.id.to_signup).setEnabled(true)

            if (!task.isSuccessful) {
                Toast.makeText(applicationContext, "sign in error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                return@addOnCompleteListener
            }

            firebase.currentUser?.getIdToken(true)?.addOnCompleteListener { tokenTask: Task<GetTokenResult> ->
                if (!tokenTask.isSuccessful) {
                    Toast.makeText(applicationContext, "sign in token error: ${tokenTask.exception?.message}", Toast.LENGTH_LONG).show()
                    return@addOnCompleteListener
                }

                val token: String? = tokenTask.result.token

                if (token == null) {
                    Toast.makeText(applicationContext, "sign in: token is null", Toast.LENGTH_LONG).show()
                } else {
                    val user = firebase.currentUser
                    Log.d("COMM", "a")
                    if (user != null) {
                        val realm = Realm.getDefaultInstance()
                        val self = realm.where<Member>().equalTo("isFriend", Relation.SELF).findFirst()
                        if (self?.id != user.uid) {
                            realm.executeTransaction {
                                realm.deleteAll()
                                val selfNew: Member = realm.createObject<Member>(user.uid)
                                selfNew.name = user.displayName ?: "default"
                                selfNew.isFriend = Relation.SELF
                                selfNew.updateImage()
                                Log.d("COMM", "new self updated")
                            }
                        }
                        val member1 = realm.where<Member>().findAll()

                        for (i in member1) {
                            Log.d("COMM", "sign in: ${i.isFriend}, ${i.name}")
                        }
                        val intent = Intent(this, GroupActivity::class.java)
                        intent.putExtra("token", token)
                        startActivity(intent)
                    } else {
                        Log.d("COMM", "could not get current user")
                    }
                }
            }
        }
    }

    //Realmインスタンスを破棄
    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }
}