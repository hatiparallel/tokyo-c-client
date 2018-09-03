package com.tokyoc.line_client

import android.os.Parcelable
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

// MemberデータFormat
@Parcelize
open class Group(
        //@PrimaryKey
        open var name: String, // = "",
        //open var members:@RawValue RealmList<Member> = RealmList(),
        open var groupId: Int): Parcelable //, RealmObject()