package com.tokyoc.line_client

import android.os.Parcelable
import io.realm.RealmObject
import kotlinx.android.parcel.Parcelize

// MemberデータFormat
@Parcelize
data class Member(var name: String,
                  var id: Int,
                  var groupId: Int): Parcelable