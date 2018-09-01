package com.tokyoc.line_client

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

// MemberデータFormat
@Parcelize
data class Group(var name: String,
                  var members: List<Member>,
                  var groupId: Int): Parcelable