package com.tokyoc.line_client

import retrofit2.http.*
import rx.Observable

interface MakeGroupClient {
    //make a group
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("/channels/") //serverの構造依存
    fun makeGroup(@Body group: Group): Observable<String>
}