package com.tokyoc.line_client

import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.annotations.SerializedName
import io.realm.Realm
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.*

//MemberデータFormat
open class Member : RealmObject() {
    companion object {
        fun lookup(uid: String, client: Client, realm: Realm): rx.Observable<Member> {
            return rx.Observable.create<Member> {
                val subscriber = it

                realm.executeTransaction {
                    var cache = realm.where<Member>().equalTo("id", uid).findFirst()
                            ?: realm.createObject<Member>(uid)

                    if (Date().getTime() - cache.cached.getTime() <= 5 * 60 * 1000) {
                        subscriber.onNext(cache)
                    } else {
                        client.getPerson(uid).subscribe{
                            cache = it
                            cache.cached = Date()
                            cache.updateImage()

                            realm.insertOrUpdate(cache)
                            subscriber.onNext(cache)
                        }
                    }
                }
            }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
        }
    }

    fun deregister(realm: Realm) {
        if (this.isFriend == Relation.OTHER && this.groupJoin <= 0) {
            realm.executeTransaction {
                this.deleteFromRealm()
            }
        }
    }

    fun updateImage() {
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("images/${this.id}.jpg")
        imageRef.getBytes(20000)
                .addOnSuccessListener {
                    Log.d("COMM", "get unique ByteArray Success")
                    val ba = it
                    realm.executeTransaction {
                        this.image = ba
                    }
                }
                .addOnFailureListener {
                    Log.d("COMM", "get unique ByteArray Failure")
                    val defaultImageRef = storageRef.child("images/yoda.jpg")
                    defaultImageRef.getBytes(20000)
                            .addOnSuccessListener {
                                Log.d("COMM", "get yoda ByteArray Success")
                                val ba = it
                                realm.executeTransaction {
                                    this.image = ba
                                }
                            }
                            .addOnFailureListener {
                                Log.d("COMM", "get yoda ByteArray Failure")
                            }
                }
    }

    @PrimaryKey
    @SerializedName("UID")
    open var id: String = "A"

    @SerializedName("DisplayName")
    open var name: String = "Aさん"

    @SerializedName("PhotoURL")
    open var photo: String = ""

    open var cached: Date = Date(0)

    open var isFriend: Int = Relation.OTHER
    // self: 0, friend: 1, others: 2

    open var groupJoin: Int = 0
    // if isFriend == 2 and groupCommon == 0 then he should be deloted from realm

    open var image: ByteArray = byteArrayOf()
}
