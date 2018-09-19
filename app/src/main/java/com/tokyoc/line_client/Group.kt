package com.tokyoc.line_client

import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.storage.FirebaseStorage
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.kotlin.where

//GroupデータFormat
open class Group() : RealmObject() {
    constructor(name: String, members: RealmList<String>) : this() {
        this.name = name
        this.members = members
    }

    fun updateImage() {
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("images/groups/${this.id}.jpg")
        imageRef.getBytes(20000)
                .addOnSuccessListener {
                    val realm = Realm.getDefaultInstance()
                    Log.d("COMM", "${this.name}: get unique ByteArray Success")
                    val ba = it
                    realm.executeTransaction {
                        this.image = ba
                        realm.insertOrUpdate(this)
                    }
                }
                .addOnFailureListener {
                    Log.d("COMM", "${this.name}: get unique ByteArray Failure")
                    val defaultImageRef = storageRef.child("images/groups/yoda_group.jpg")
                    defaultImageRef.getBytes(20000)
                            .addOnSuccessListener {
                                val realm = Realm.getDefaultInstance()
                                Log.d("COMM", "${this.name}: get yoda ByteArray Success")
                                val ba = it
                                realm.executeTransaction {
                                    this.image = ba
                                    realm.insertOrUpdate(this)
                                }
                            }
                            .addOnFailureListener {
                                Log.d("COMM", "${this.name}: get yoda ByteArray Failure")
                            }
                }
    }

    fun display(nameView: TextView, imageView: ImageView) {
        if (this.name.isNotEmpty()) {
            nameView.text = this.name
            imageView.setImageBitmap(BitmapFactory.decodeByteArray(this.image, 0, this.image.size))
        } else {
            nameView.setTextColor(Color.DKGRAY)
            val realm = Realm.getDefaultInstance()
            val member = realm.where<Member>().equalTo("id", this.members[0]).findFirst()
            if (member != null) {
                nameView.text = member.name
                imageView.setImageBitmap(BitmapFactory.decodeByteArray(member.image, 0, member.image.size))
            } else {
                nameView.text = "取得失敗"
                imageView.setImageResource(R.drawable.img001)
            }
        }
    }

    @PrimaryKey
    open var id: Int = 0
    open var name: String = ""
    open var members: RealmList<String> = RealmList()
    open var latest: Int = 0
    open var image: ByteArray = byteArrayOf()
}
