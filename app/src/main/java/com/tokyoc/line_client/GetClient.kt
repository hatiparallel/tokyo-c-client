package com.tokyoc.line_client

import retrofit2.http.*
import rx.Observable

interface GetClient {
    @GET("/stream/123") //serverの構造依存
    fun getMessages(): Observable<TestMessage>
}