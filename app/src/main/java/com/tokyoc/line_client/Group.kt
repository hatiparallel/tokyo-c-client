package com.tokyoc.line_client

import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

//GroupデータFormat
open class Group() : RealmObject() {
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
                    val defaultImageRef = storageRef.child("images/yoda.jpg")
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

    @PrimaryKey
    open var id: Int = 0
    open var name: String = ""
    open var members: RealmList<String> = RealmList()
    open var latest: Int = 0
    open var image: ByteArray = byteArrayOf()
}
