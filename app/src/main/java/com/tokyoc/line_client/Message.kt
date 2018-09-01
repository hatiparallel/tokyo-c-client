package com.tokyoc.line_client

import android.os.Parcelable
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.util.*

// MessageデータのFormat
@Parcelize
open class Message: Parcelable, RealmObject() {
    //データを一意に指定するためにidにPrimaryKeyアノテーションを付加
    @PrimaryKey
    var id: Int = 0
    var channel: Int = 0
    var author: String = ""
    var isEvent: Int = 0
    var postedAt: Date = Date()
    var content: String = ""
}
