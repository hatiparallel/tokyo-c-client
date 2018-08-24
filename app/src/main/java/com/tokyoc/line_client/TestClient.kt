package com.tokyoc.line_client

import retrofit2.http.*
import rx.Observable

interface TestClient {
    //send a message
    @Headers("Content-Type:application/json")
    @POST("/streams/{id}") //serverの構造依存
    fun postTest(@Path("id") id: Int, @Body mes:TestMessage): Observable<String>
}