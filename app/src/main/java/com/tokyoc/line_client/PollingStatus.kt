package com.tokyoc.line_client

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class PollingStatus : RealmObject() {
    @PrimaryKey
    var id: Int = 0
    var suppressedGroup: Int? = null
}