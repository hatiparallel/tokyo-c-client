package com.tokyoc.line_client

import android.os.Parcelable
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import kotlinx.android.parcel.Parcelize

//MemberデータFormat
@Parcelize
open class Member: RealmObject(), Parcelable {
    @PrimaryKey
    var id: Int = 0
    var name: String = "Aさん"
    var userId: Int = 0
    var groupId: Int = 0
}