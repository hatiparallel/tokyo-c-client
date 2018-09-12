package com.tokyoc.line_client

import android.util.Log
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
        fun lookup(uid: String, client: Client): rx.Observable<Member> {
            return rx.Observable.create<Member> {
                val realm = Realm.getDefaultInstance()
                val subscriber = it
                var cache: Member? = null

                realm.executeTransaction {
                    cache = realm.where<Member>().equalTo("id", uid).findFirst()
                            ?: realm.createObject<Member>(uid)

                    if (Date().getTime() - cache!!.cached.getTime() <= 5 * 60 * 1000) {
                        return@executeTransaction
                    }

                    try {
                        cache = client.getPerson(uid).toBlocking().single()
                        cache!!.cached = Date()
                        realm.insertOrUpdate(cache)
                    } catch (e: Exception) {
                    }
                }

                subscriber.onNext(cache)
                subscriber.onCompleted()
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

    open var image: ByteArray = byteArrayOf()
}
