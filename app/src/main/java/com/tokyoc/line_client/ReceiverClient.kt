package com.tokyoc.line_client

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming
import rx.Observable

interface ReceiverClient {
    @GET("/streams/{channel}")
    @Streaming
    fun getMessages(@Path("channel") channel: Int): Observable<ResponseBody>
}