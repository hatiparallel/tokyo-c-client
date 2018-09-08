package com.tokyoc.line_client

import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import rx.Observable
import java.util.concurrent.TimeUnit

interface Client {
    companion object {
        public val gson : Gson =GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                .setLenient()
                .create()

        fun build(token: String): Client {
            val authenticatedClient = OkHttpClient().newBuilder()
                    .readTimeout(0, TimeUnit.SECONDS)
                    .addInterceptor(Interceptor { chain ->
                        chain.proceed(
                                chain.request()
                                        .newBuilder()
                                        .header("Authorization", "Bearer $token")
                                        .build())
                    })
                    .build()
            val retrofit = Retrofit.Builder()
                    .client(authenticatedClient)
                    .baseUrl(BuildConfig.BACKEND_BASEURL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .build()

            return retrofit.create(Client::class.java)
        }
    }

    @GET("/messages/{channel}")
    @Streaming
    fun getMessages(@Path("channel") channel: Int, @Query("since_id") since_id: Int = 0): Observable<ResponseBody>

    @Headers("Content-Type: application/json")
    @POST("/messages/{channel}") //serverの構造依存
    fun sendMessage(@Path("channel") channel: Int, @Body message: Message): Observable<Message>


    @Headers("Content-type: application/json")
    @POST("/friendships/")
    fun sendPIN(@Body pin: Int): Observable<String>

    @PUT("/friendships/{person}")
    fun makeFriends(@Path("person") person: String): Observable<List<String>>


    @Headers("Content-Type: application/json")
    @POST("/channels/") //serverの構造依存
    fun makeGroup(@Body group: Group): Observable<Group>

    @Headers("Content-Type: application/json")
    @PUT("/channels/{channel}/{person}")
    fun invitePerson(@Path("channel") channel: Int, @Path("person") person: String): Observable<Group>



    @Headers("Content-Type: application/json")
    @GET("/people/{uid}")
    fun getPerson(@Path("uid") uid: String): Observable<Member>


    @Headers("Content-Type: application/json")
    @GET("/status")
    fun getStatus(): Observable<Status>


    @GET("/pin")
    @Streaming
    fun getPIN(): Observable<ResponseBody>

    @DELETE("/friendships/{person}")
    fun deleteFriend(@Path("person") person: String): Observable<List<String>>
}