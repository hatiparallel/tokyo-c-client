package com.tokyoc.line_client

//StatusデータFormat
//StatusデータFormat
open class Status(
    open var id: Int = 0,
    open var friendshipCount: Int = 0,
    open var latests: HashMap<Int, Int>)