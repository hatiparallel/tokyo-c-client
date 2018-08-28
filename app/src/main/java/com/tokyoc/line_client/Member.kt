package com.tokyoc.line_client

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

// MemberデータFormat
@Parcelize
data class Member(val name: String,
                  val id: Int,
                  val groupId: Int): Parcelable