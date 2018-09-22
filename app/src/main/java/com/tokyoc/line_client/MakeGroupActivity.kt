package com.tokyoc.line_client

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import com.google.firebase.storage.FirebaseStorage
import com.trello.rxlifecycle.components.support.RxAppCompatActivity
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.ByteArrayOutputStream
import java.io.IOException


class MakeGroupActivity: RxAppCompatActivity() {
    val image_request_code = 2900

    var uri: Uri? = null
    var ba: ByteArray? = null


    private lateinit var realm: Realm

    override fun onCreate(saveInstanceState: Bundle?) {
        super.onCreate(saveInstanceState)
        setContentView(R.layout.activity_group_make)
        realm = Realm.getDefaultInstance()

        val token = intent.getStringExtra("token")

        val toolbar = supportActionBar!!
        toolbar.setDisplayHomeAsUpEnabled(true)

        findViewById<TextView>(R.id.get_image).setOnClickListener() {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.setType("image/*")
            startActivityForResult(intent, image_request_code)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_group_make, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val token = intent.getStringExtra("token")
        val client = Client.build(token)
        realm = Realm.getDefaultInstance()

        when (item?.itemId) {
            android.R.id.home -> {
                val intent = Intent(this, GroupActivity::class.java)
                intent.putExtra("token", token)
                startActivity(intent)
            }
            R.id.make_group -> {
                val groupNameEditText = findViewById<EditText>(R.id.group_name)
                if (groupNameEditText.text.isEmpty()) {
                    return false
                }

                val groupName = groupNameEditText.text.toString()
                val new_group = Group()
                new_group.name = groupName

                client.makeGroup(new_group)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            Log.d("COMM", "post done: name is ${it.name}, id is ${it.id}")
                            val group = it
                            if (ba != null) {
                                if (ba!!.size <= 0 || ba!!.size > 20000) {
                                    Toast.makeText(applicationContext, "画像サイズが適用範囲外のため登録されませんでした", Toast.LENGTH_LONG)
                                            .show()
                                    group.updateImage()
                                    realm.executeTransaction {
                                        realm.insertOrUpdate(group)
                                    }
                                    val intent = Intent(this, GroupActivity::class.java)
                                    intent.putExtra("token", token)
                                    startActivity(intent)
                                } else {
                                    val storageRef = FirebaseStorage.getInstance().reference
                                    val imageRef = storageRef.child("images/groups/${group.id}.jpg")
                                    imageRef.putBytes(ba!!)
                                            .addOnSuccessListener {
                                                Log.d("COMM", "upload success")
                                                realm = Realm.getDefaultInstance()
                                                group.updateImage()
                                                realm.executeTransaction {
                                                    realm.insertOrUpdate(group)
                                                }
                                                val intent = Intent(this, GroupActivity::class.java)
                                                intent.putExtra("token", token)
                                                startActivity(intent)
                                            }
                                            .addOnFailureListener {
                                                realm = Realm.getDefaultInstance()
                                                Log.d("COMM", "upload failure: ${it.message}")
                                                Toast.makeText(applicationContext, "画像登録時にエラーが発生しました", Toast.LENGTH_LONG)
                                                        .show()
                                                group.updateImage()
                                                realm.executeTransaction {
                                                    realm.insertOrUpdate(group)
                                                }
                                                val intent = Intent(this, GroupActivity::class.java)
                                                intent.putExtra("token", token)
                                                startActivity(intent)
                                            }
                                }
                            } else {
                                group.updateImage()
                                realm.executeTransaction {
                                    realm.insertOrUpdate(group)
                                }
                                val intent = Intent(this, GroupActivity::class.java)
                                intent.putExtra("token", token)
                                startActivity(intent)
                            }
                        }, {
                            Log.d("COMM", "post failed: ${it}")
                        })
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //Realmインスタンスの放棄
    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //super.onActivityResult(requestCode, resultCode, data)
        if (requestCode === image_request_code && resultCode === Activity.RESULT_OK) {
            if (data != null) {
                uri = data.getData()
                if (uri == null) {
                    Log.d("COMM", "uri is null")
                    return
                }
                Log.d("COMM", "uri: ${uri.toString()}")
                try {
                    val bmp = getBitmapFromUri(uri as Uri)
                    val imageView = findViewById<ImageView>(R.id.image_view)
                    imageView.setImageBitmap(bmp)
                    val baos: ByteArrayOutputStream = ByteArrayOutputStream()
                    bmp.compress(Bitmap.CompressFormat.JPEG, 50, baos)
                    ba = baos.toByteArray()
                } catch (e: IOException) {
                    e.printStackTrace()
                    Log.d("COMM", "get bitmap error")
                }

            }
        }
    }

    @Throws(IOException::class)
    private fun getBitmapFromUri(uri: Uri): Bitmap {
        val parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")
        val fileDescriptor = parcelFileDescriptor!!.fileDescriptor
        val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor.close()
        return image
    }
}
