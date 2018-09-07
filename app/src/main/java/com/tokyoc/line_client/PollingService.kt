package com.tokyoc.line_client

import android.app.IntentService
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class PollingService : IntentService("polling_service") {
    override fun onHandleIntent(intent: Intent) {
        val token = intent.getStringExtra("token")
        val client = Client.build(token)

        while (true) {
            client.getStatus()

            Thread.sleep(1000)
        }
    }
}
