package com.tokyoc.line_client

import android.app.IntentService
import android.content.Intent

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
