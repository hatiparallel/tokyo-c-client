package com.tokyoc.line_client

import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.annotations.SerializedName
import io.realm.Realm
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.kotlin.createObject
import io.realm.kotlin.delete
import io.realm.kotlin.where
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.*

//MemberデータFormat
open class Member : RealmObject() {
    companion object {
        fun lookup(uid: String, client: Client): rx.Observable<Member> {
            return rx.Observable.create<Member> {
                val subscriber = it
                var cache: Member? = null
                val realm: Realm = Realm.getDefaultInstance()

                realm.executeTransaction {
                    cache = realm.where<Member>().equalTo("id", uid).findFirst()
                            ?: realm.createObject<Member>(uid)
                    val isFriend = cache?.isFriend

                    if (Date().getTime() - cache!!.cached.getTime() <= 5 * 60 * 1000) {
                        return@executeTransaction
                    }

                    try {
                        val cache = client.getPerson(uid).toBlocking().single()
                        cache!!.cached = Date()
                        cache!!.updateImage()
                        cache!!.isFriend = isFriend ?: Relation.OTHER
                        realm.insertOrUpdate(cache)
                    } catch (e: Exception) {
                        Log.d("COMM", "$e")
                    }
                }
                Log.d("CACHE", "cache: ${cache?.id}, ${cache?.name}, ${cache?.cached}, ${cache?.isValid()}, ${cache?.isManaged()}")

                if (cache == null) {
                    subscriber.onNext(cache)
                } else if (cache!!.isManaged()) {
                    subscriber.onNext(realm.copyFromRealm(cache))
                } else {
                    subscriber.onNext(cache)
                }

                //subscriber.onNext(realm.copyFromRealm(cache))
                subscriber.onCompleted()
            }
        }
    }

    fun deregister() {
        val realm = Realm.getDefaultInstance()
        if (this.isFriend == Relation.OTHER && this.groupJoin <= 0) {
            realm.executeTransaction {
                realm.where<Member>().equalTo("id", this.id).findFirst()?.deleteFromRealm()
            }
        }
    }

    fun updateImage() {
        Log.d("updater", "before: ${this.name}, ${this.isFriend}")
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("images/${this.id}.jpg")
        imageRef.getBytes(20000)
                .addOnSuccessListener {
                    val realm = Realm.getDefaultInstance()
                    Log.d("COMM", "${this.name}: get unique ByteArray Success")
                    val ba = it
                    realm.executeTransaction {
                        this.image = ba
                        realm.insertOrUpdate(this)
                        Log.d("updater", "after: ${this.name}, ${this.isFriend}")
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
                                    Log.d("updater", "after: ${this.name}, ${this.isFriend}")
                                }
                            }
                            .addOnFailureListener {
                                Log.d("COMM", "${this.name}: get yoda ByteArray Failure")
                            }
                }
    }

    @PrimaryKey
    @SerializedName("UID")
    open var id: String = "A"

    @SerializedName("DisplayName")
    open var name: String = "Aさん"

    open var cached: Date = Date(0)

    open var isFriend: Int = Relation.OTHER
    // self: 0, friend: 1, others: 2

    open var groupJoin: Int = 0
    // if isFriend == 2 and groupCommon == 0 then he should be deloted from realm

    open var image: ByteArray = byteArrayOf()
}
