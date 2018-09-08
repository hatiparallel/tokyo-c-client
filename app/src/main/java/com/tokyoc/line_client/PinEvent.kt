package com.tokyoc.line_client

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.util.*

// PinEventデータのFormat
open class PinEvent {
    val type: String = ""
    @SerializedName("PIN")
    val pin: Int = 0
    val person: String = ""
}