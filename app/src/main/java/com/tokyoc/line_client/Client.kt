package com.tokyoc.line_client

import okhttp3.ResponseBody
import retrofit2.http.*
import rx.Observable

interface Client {
    @GET("/messages/{channel}")
    @Streaming
    fun getMessages(@Path("channel") channel: Int, @Query("since_id") since_id : Int = 0): Observable<ResponseBody>

    @Headers("Content-Type: application/json")
    @POST("/messages/{channel}") //serverの構造依存
    fun sendMessage(@Path("channel") channel: Int, @Body message: Message): Observable<Message>

    @Headers("Content-Type: application/json")
    @POST("/channels/") //serverの構造依存
    fun makeGroup(@Body group: Group): Observable<Group>

    @Headers("Content-Type: application/json")
    @PUT("/channels/{channel}/{person}")
    fun invitePerson(@Path("channel") channel: Int, @Path("person") person: String): Observable<String>

    @Headers("Content-Type: application/json")
    @GET("/people/{uid}")
    fun getPerson(@Path("uid") uid: String): Observable<Member>

    @Headers("Content-Type: application/json")
    @GET("/status")
    fun getStatus(): Observable<Status>

    @GET("/pin")
    @Streaming
    fun getPIN(): Observable<ResponseBody>
}