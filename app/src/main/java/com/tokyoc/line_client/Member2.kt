package com.tokyoc.line_client

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

//MemberデータFormat
open class Member2: RealmObject() {
    @PrimaryKey
    var id: Int = 0
    var name: String = ""
    var userId: Int = 0
    var groupId: Int = 0
}
