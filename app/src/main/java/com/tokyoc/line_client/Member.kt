package com.tokyoc.line_client

import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

//MemberデータFormat
open class Member : RealmObject() {
    @PrimaryKey
    @SerializedName("UID")
    open var id: String = "A"

    @SerializedName("DisplayName")
    open var name: String = "Aさん"

    @SerializedName("PhotoURL")
    open var photo: String = ""

    open var cached: Date = Date(0)
}
