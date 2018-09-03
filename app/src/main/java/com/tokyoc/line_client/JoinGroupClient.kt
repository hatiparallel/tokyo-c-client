package com.tokyoc.line_client

import retrofit2.http.*
import rx.Observable

interface JoinGroupClient {
    //join the group
    @POST("/channels/{id}") //serverの構造依存
    fun joinGroup(@Path("id") id: Int): Observable<Group>
}