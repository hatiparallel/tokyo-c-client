package com.tokyoc.line_client

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.ActionBar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.KeyEvent
import android.widget.Button
import android.widget.ImageButton
import android.widget.ListView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import io.realm.Realm

class SettingActivity : AppCompatActivity() {
    private lateinit var realm: Realm
    lateinit var toolbar: ActionBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        val token = intent.getStringExtra("token")

        toolbar = supportActionBar!!
        val bottomNavigation: BottomNavigationView = findViewById(R.id.navigation)
        val item = bottomNavigation.getMenu().getItem(2)
        item.setChecked(true)

//        // BottomNavigationBarのItemを押した時の処理（Fragment変更Ver.）
//        bottomNavigation.setOnNavigationItemSelectedListener {
//            when (it.itemId) {
//                R.id.navigation_friend -> {
//                    toolbar.title = "友達"
//                    val memberFragment = MemberFragment.newInstance()
//                    openFragment(memberFragment)
//                    Log.d("COMM", "${it.title}")
//                }
//                R.id.navigation_group -> {
//                    toolbar.title = "グループ"
//                    val groupFragment = GroupFragment.newInstance()
//                    openFragment(groupFragment)
//                    Log.d("COMM", "${it.title}")
//                }
//                R.id.navigation_setting -> {
//                    toolbar.title = "設定"
//                    val settingFragment = SettingFragment.newInstance()
//                    openFragment(settingFragment)
//                    Log.d("COMM", "${it.title}")
//                }
//            }
//            true
//        }

        // BottomNavigationButtonのItemを押した時の処理（Activity変更Ver.）
        bottomNavigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_friend -> {
                    val intent = Intent(this, MemberActivity::class.java)
                    intent.putExtra("token", token)
                    startActivity(intent)
                    Log.d("COMM", "${it.title}")
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.navigation_group -> {
                    val intent = Intent(this, GroupActivity::class.java)
                    intent.putExtra("token", getIntent().getStringExtra("token"))
                    startActivity(intent)
                    Log.d("COMM", "${it.title}")
                    return@setOnNavigationItemSelectedListener true
                }
            }
            false
        }

        //サインアウトボタンを押した時の処理
        findViewById<ImageButton>(R.id.signout_button).setOnClickListener {
            val intent = Intent(this, SigninActivity::class.java)
            AlertDialog.Builder(this).apply {
                setTitle("Sign Out")
                setMessage("Really Sign Out?")
                setPositiveButton("OK", DialogInterface.OnClickListener { _, _ ->
                    FirebaseAuth.getInstance().signOut()
                    startActivity(intent)
                })
                setNegativeButton("Cancel", null)
                show()
            }
        }

        // 個人情報変更ボタンを押した時の処理
        findViewById<ImageButton>(R.id.profile_button).setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("token", token)
            startActivity(intent)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                Toast.makeText(applicationContext, "this key is invalid", Toast.LENGTH_LONG)
                        .show()
                return false
            }
            else -> return super.onKeyDown(keyCode, event)
        }
    }

    // フラグメントを変更するための関数
    private fun openFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}
