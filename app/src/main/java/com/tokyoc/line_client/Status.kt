package com.tokyoc.line_client

import android.os.Parcelable
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

//StatusデータFormat
open class Status(
    open var id: Int = 0,
    open var friendshipCount: Int = 0,
    open var latests: HashMap<Int, Int>)