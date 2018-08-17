package com.tokyoc.line_client

import retrofit2.http.*
import rx.Observable

interface SenderClient {
    //send a message
    @Headers("Content-Type:application/json")
    @POST("/groups") //serverの構造依存
    fun sendMessage(@Body sjson:String): Observable<Boolean>
}