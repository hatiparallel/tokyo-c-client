package com.tokyoc.line_client

import android.os.Parcelable
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

//GroupデータFormat
open class Group(
    @PrimaryKey
    open var id: Int = 0,
    open var name: String = "GroupA",
    open var members: RealmList<String> = RealmList(),
    open var groupId: Int = 0): RealmObject()