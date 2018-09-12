package com.tokyoc.line_client

//StatusデータFormat
open class Status(
        open var friendshipCount: Int = 0,
        open var latests: List<Summary>)