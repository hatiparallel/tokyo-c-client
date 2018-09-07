package com.tokyoc.line_client

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import kotlinx.android.parcel.Parcelize

//MemberデータFormat
@Parcelize
open class Member(
    @PrimaryKey
    open var id: Int = 0,
    @SerializedName("DisplayName")
    open var name: String = "Aさん",
    @SerializedName("PhotoURL")
    open var photo: String = "",
    @SerializedName("UID")
    open var userId: String = ""): Parcelable, RealmObject()

