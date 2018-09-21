package com.tokyoc.line_client

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.google.firebase.storage.FirebaseStorage
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.widget.*
import kotlinx.android.synthetic.main.activity_image_change.*
import java.io.IOException
import android.graphics.BitmapFactory
import android.os.ParcelFileDescriptor
import com.google.firebase.storage.StorageReference
import retrofit2.adapter.rxjava.HttpException
import java.io.ByteArrayOutputStream
import android.graphics.drawable.BitmapDrawable
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.storage.StorageMetadata
import io.realm.Realm
import io.realm.kotlin.where


class ChangeImageActivity : AppCompatActivity() {
    val firebaseUser:FirebaseUser? = FirebaseAuth.getInstance().currentUser

    val image_request_code = 2700

    var uri: Uri? = null
    var ba: ByteArray? = null

    private lateinit var realm: Realm

    override fun onCreate(saveInstanceState: Bundle?) {
        super.onCreate(saveInstanceState)
        setContentView(R.layout.activity_image_change)

        val token = intent.getStringExtra("token")

        val toolbar = supportActionBar!!
        toolbar.setDisplayHomeAsUpEnabled(true)

        realm = Realm.getDefaultInstance()

        findViewById<TextView>(R.id.get_image).setOnClickListener() {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.setType("image/*")
            startActivityForResult(intent, image_request_code)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_image_change, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val token = intent.getStringExtra("token")
        realm = Realm.getDefaultInstance()
        when (item?.itemId) {
            android.R.id.home -> {
                val intent = Intent(this, ProfileActivity::class.java)
                intent.putExtra("token", token)
                startActivity(intent)
            }
            R.id.change_image -> {
                val storageRef: StorageReference = FirebaseStorage.getInstance().reference
                if (ba == null) {
                    Toast.makeText(applicationContext, "画像が読み込めていません", Toast.LENGTH_LONG).show()
                    return false
                } else if (ba!!.size > 20000) {
                    Toast.makeText(applicationContext, "画像サイズが大きすぎます", Toast.LENGTH_LONG).show()
                    Log.d("COMM", "the image size is too big")
                    return false
                }

                val self: Member? = realm.where<Member>().equalTo("isFriend", Relation.SELF).findFirst()
                if (self == null) {
                    Toast.makeText(applicationContext, "ユーザーが認証できていません", Toast.LENGTH_LONG).show()
                    return false
                }

                val imageRef = storageRef.child("images/${self.id}.jpg")
                imageRef.putBytes(ba!!)
                        .addOnSuccessListener {
                            Log.d("COMM", "upload success")
                            self.updateImage()
                            val intent = Intent(this, ProfileActivity::class.java)
                            intent.putExtra("token", token)
                            startActivity(intent)
                        }
                        .addOnFailureListener {
                            Log.d("COMM", "upload failure: ${it.message}")
                        }
            }
        }
        return super.onOptionsItemSelected(item)
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
                    val self = realm.where<Member>().equalTo("isFriend", Relation.SELF).findFirst()
                    realm.executeTransaction {
                        self?.image = ba!!
                    }
                    findViewById<TextView>(R.id.get_image).setText(R.string.got_image)
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