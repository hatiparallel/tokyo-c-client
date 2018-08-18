package com.tokyoc.line_client

import retrofit2.http.*
import rx.Observable

interface GetClient {
    @GET("/stream/{id}") //serverの構造依存
    @Streaming
    fun getMessages(): Observable<TestMessage>
}