package com.tokyoc.line_client

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

//MessageデータのFormat
//senderは仮置き。実際はMember型。sender=0なら自分、1なら相手。
//idはmessageを連番で管理するためのもの。一意である必要があるため@PrimaryKeyが要る
open class Message: RealmObject() {
    @PrimaryKey
    var id: Long = 0
    var textmessage: String = ""
    var sender: Int = 0
    var date: Date = Date()
}