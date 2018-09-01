package com.tokyoc.line_client

import android.os.Parcelable
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import kotlinx.android.parcel.Parcelize

//MemberデータFormat
@Parcelize
open class Member(
    @PrimaryKey
    open var id: Int = 0,
    open var name: String = "Aさん",
    open var userId: Int = 0,
    open var groupId: Int = 0): Parcelable, RealmObject()