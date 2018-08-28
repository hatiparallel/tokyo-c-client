package com.tokyoc.line_client

import retrofit2.http.*
import rx.Observable

interface SenderClient {
    //send a message
    @Headers("Content-Type: application/json")
    @POST("/streams/{channel}") //serverの構造依存
    fun sendMessage(@Path("channel") channel: Int, @Body message: Message): Observable<String>
}