package com.tokyoc.line_client

import retrofit2.http.GET
import retrofit2.http.Query
import rx.Observable

interface ReceiverClient {
    @GET("/groups") //serverの構造依存
    fun getMessages(@Query("group") query: Int): Observable<List<Message>>
    //queryはグループIDと人を渡すのでのちのちjsonにしないといけないことに気が付いた
}