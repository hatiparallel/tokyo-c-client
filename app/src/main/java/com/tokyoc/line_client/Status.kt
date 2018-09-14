package com.tokyoc.line_client

import java.util.*

//StatusデータFormat
open class Status(
        open var friendshipCount: Int = 0,
        open var friendshipAddedAt: Date,
        open var latests: List<Summary>)