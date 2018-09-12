package com.tokyoc.line_client

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

//GroupデータFormat
open class Group(
        @PrimaryKey
        open var id: Int = 0,
        open var name: String = "",
        open var members: RealmList<String> = RealmList(),
        open var latest: Int = 0) : RealmObject()
