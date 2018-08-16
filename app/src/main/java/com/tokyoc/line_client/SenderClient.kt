package com.tokyoc.line_client

import retrofit2.http.GET
import retrofit2.http.Query
import rx.Observable

interface SenderClient {
    //send a message
    @GET("/groups") //serverの構造依存
    fun sendMessage(@Query("message") query: String): Observable<Boolean>
}