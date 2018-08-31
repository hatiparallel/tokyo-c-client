package com.tokyoc.line_client

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

//MessageデータのFormat
open class Message: RealmObject() {
    //データを一意に指定するためにidにPrimaryKeyアノテーションを付加
    @PrimaryKey
    var id: Int = 0
    var channel: Int = 0
    var author: String = ""
    var isEvent: Int = 0
    var postedAt: Date = Date()
    var content: String = ""
}
