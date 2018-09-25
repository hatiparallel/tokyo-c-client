package com.tokyoc.line_client

import android.app.Application
import android.app.IntentService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
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

    private var bound = false
    private var suppressedGroup = -1

    inner class Receiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            suppressedGroup = intent.extras.getInt("suppress")
            Log.d("POLL", "suppress $suppressedGroup")
        }
    }

    override fun onCreate() {
        super.onCreate()

        val intentFilter = IntentFilter()
        intentFilter.addAction("POLLING_CONTROL")
        registerReceiver(Receiver(), intentFilter)
    }

    override fun onBind(intent: Intent?): IBinder? {
        bound = true
        return android.os.Binder()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        bound = false
        return super.onUnbind(intent)
    }

    override fun onHandleIntent(intent: Intent) {
        val realm = Realm.getDefaultInstance()
        val token = intent.getStringExtra("token")
        val client = Client.build(token)
        val notification_manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        var lastStatus: Status? = null

        notification_manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "YODA", NotificationManager.IMPORTANCE_DEFAULT))

        while (bound) {
            val status = client.getStatus().toBlocking().first() ?: continue

            if (lastStatus == null ||
                    lastStatus!!.friendshipCount != status.friendshipCount ||
                    lastStatus!!.friendshipAddedAt != status.friendshipAddedAt) {

                Log.d("POLL", "friends updated")

                val friends = client.getFriends().toBlocking().single().toTypedArray()

                rx.Observable.from(friends)
                        .flatMap { Member.lookup(it, client) }
                        .subscribe {
                            val member = it
                            val realm = Realm.getDefaultInstance()

                            Log.d("DDDDD", "got it ! ${member.name}, ${member.isFriend}")

                            if (member.isFriend != Relation.OTHER) {
                                return@subscribe
                            }

                            Log.d("DDDDD", "it'll be friend ${member.name}, ${member.isFriend}")

                            member.isFriend = Relation.FRIEND

                            realm.executeTransaction {
                                realm.insertOrUpdate(member)
                            }

                            notification_manager.notify(0,
                                    NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                                            .setSmallIcon(R.drawable.img001)
                                            .setContentTitle(getText(R.string.app_name))
                                            .setContentText("${member.name}が友達になりました")
                                            .setAutoCancel(true)
                                            .build())
                        }

                realm.where<Member>()
                        .not().beginGroup().`in`("id", friends).endGroup()
                        .findAll().forEach {
                            val member = it
                            Log.d("DDDD", "it'll be other ${member.name}, ${member.isFriend}")

                            if (member.isFriend == Relation.FRIEND) {
                                Log.d("DDDD", "good bye ${member.name}, ${member.isFriend}")
                                realm.executeTransaction {
                                    member.isFriend = Relation.OTHER
                                }
                                member.deregister()
                            }
                        }
            }

            rx.Observable.from(status.latests)
                    .flatMap {
                        lateinit var group: Group
                        var isModified = false
                        var summary = it

                        realm.executeTransaction {
                            group = realm.where<Group>().equalTo("id", summary.channelId).findFirst()
                                    ?: realm.createObject<Group>(summary.channelId)

                            group.name = summary.channelName
                            isModified = summary.messageId != group.latest
                            group.latest = summary.messageId
                        }

                        if (group.isManaged) {
                            group = realm.copyFromRealm(group)
                        }

                        if (isModified) {
                            return@flatMap rx.Observable.just(group)
                        } else {
                            return@flatMap rx.Observable.empty<Group>()
                        }
                    }
                    .flatMap { client.getMessage(it.latest).subscribeOn(Schedulers.io()) }
                    .subscribe({
                        val message = it
                        val realm = Realm.getDefaultInstance()

                        val group = realm.where<Group>().equalTo("id", it.channel).findFirst()
                                ?: return@subscribe

                        group.updateImage()

                        realm.executeTransaction {
                            group.latestText = message.content
                        }

                        if (suppressedGroup == group.id) {
                            return@subscribe
                        }

                        val author = Member.lookup(it.author, client).toBlocking().single()
                        var text = ""

                        if (it.isEvent != 1) {
                            text = it.content
                        } else if (it.content == "join") {
                            text = "${author.name}が${group.name}に参加しました"
                        } else if (it.content == "leave") {
                            text = "${author.name}が${group.name}から退室しました"
                        }

                        Log.d("POLL", "notify ${it.author}")

                        notification_manager.notify(group.id,
                                NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                                        .setSmallIcon(R.drawable.img001)
                                        .setContentTitle(author?.name)
                                        .setContentText(text)
                                        .setSubText(group.name)
                                        .setWhen(it.postedAt.time)
                                        .setAutoCancel(true)
                                        .build())
                    }, {
                        Log.d("POLL", "notify failed $it")
                    })

            lastStatus = status

            val deleted = realm.where<Group>().findAll().map { it.id } - status.latests.map { it.channelId }

            realm.executeTransaction {
                realm.where<Group>().`in`("id", deleted.toTypedArray()).findAll().deleteAllFromRealm()
            }

            Thread.sleep(3000)
        }
    }

    override fun onDestroy() {
        Log.d("POLL", "PollingService destroyed")
        super.onDestroy()
    }
}
