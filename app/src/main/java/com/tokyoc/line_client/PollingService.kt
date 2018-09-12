package com.tokyoc.line_client

import android.app.IntentService
import android.content.Intent
import android.util.Log
import com.trello.rxlifecycle.kotlin.bindToLifecycle
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.delete
import io.realm.kotlin.where
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

class PollingService : IntentService("polling_service") {
    override fun onHandleIntent(intent: Intent) {
        val realm = Realm.getDefaultInstance()
        val token = intent.getStringExtra("token")
        val client = Client.build(token)

        while (true) {
            val status = client.getStatus().toBlocking().first()

            realm.executeTransaction {
                for (summary in status.latests) {
                    val group = realm.where<Group>().equalTo("id", summary.channelId).findFirst()
                            ?: (realm.createObject<Group>(summary.channelId) ?: continue)

                    group.name = summary.channelName

                    if (summary.messageId > group.latest) {
                        // client.getMessage(summary.messageId).toBlocking().first()
                    }
                }

                val deleted = realm.where<Group>().findAll().map { it.id } - status.latests.map { it.channelId }

                realm.where<Group>().`in`("id", deleted.toTypedArray()).findAll().deleteAllFromRealm()
            }

            Thread.sleep(3000)
        }
    }
}
