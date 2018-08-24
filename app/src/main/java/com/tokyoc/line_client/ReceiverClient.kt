package com.tokyoc.line_client

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import rx.Observable

interface ReceiverClient {
    @GET("/streams/{channel}") //serverの構造依存
    fun getMessages(@Path("channel") channel: Long, @Query("sinceId") sinceId: Long): Observable<List<Message>>
}