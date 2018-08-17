package com.tokyoc.line_client

import retrofit2.http.*
import rx.Observable

interface TestClient {
    //send a message
    @Headers("Content-Type:application/json")
    @POST("/stream/123") //serverの構造依存
    fun postTest(@Body mes:TestMessage): Observable<String>
}