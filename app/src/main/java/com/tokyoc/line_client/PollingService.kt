package com.tokyoc.line_client

import android.app.IntentService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.util.Log
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import rx.schedulers.Schedulers

class PollingService : IntentService("polling_service") {
    companion object {
        const val CHANNEL_ID = "YODA_POLLING_SERVICE"
    }

    override fun onHandleIntent(intent: Intent) {
        val realm = Realm.getDefaultInstance()
        val token = intent.getStringExtra("token")
        val client = Client.build(token)
        val notification_manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notification_manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "YODA", NotificationManager.IMPORTANCE_DEFAULT))

        while (true) {
            Thread.sleep(3000)

            val status = client.getStatus().toBlocking().first() ?: continue

            realm.executeTransaction {
                for (summary in status.latests) {
                    val group = realm.where<Group>().equalTo("id", summary.channelId).findFirst()
                            ?: realm.createObject<Group>(summary.channelId)

                    group.name = summary.channelName

                    if (summary.messageId > group.latest) {
                        client.getMessage(summary.messageId)
                                .observeOn(Schedulers.io())
                                .subscribe({
                                    val author = Member.lookup(it.author, client, realm).toBlocking().single()

                                    var text = ""

                                    if (it.isEvent == 0) {
                                        text = it.content
                                    } else if (it.content == "join") {
                                        text = "${author.name}が${group.name}に参加しました"
                                    } else if (it.content == "leave") {
                                        text = "${author.name}が${group.name}から退出しました"
                                    }

                                    Log.d("COMM/POLL", "notify ${it.author}")

                                    notification_manager.notify(summary.channelId,
                                            NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                                                    .setSmallIcon(R.drawable.img001)
                                                    .setContentTitle(author?.name)
                                                    .setContentText(text)
                                                    .setSubText(summary.channelName)
                                                    .setWhen(it.postedAt.time)
                                                    .setAutoCancel(true)
                                                    .build())
                                }, {
                                    Log.d("COMM/POLL", "notify failed $it")
                                })
                    }

                    group.latest = summary.messageId
                }

                val deleted = realm.where<Group>().findAll().map { it.id } - status.latests.map { it.channelId }

                realm.where<Group>().`in`("id", deleted.toTypedArray()).findAll().deleteAllFromRealm()
            }
        }
    }
}
