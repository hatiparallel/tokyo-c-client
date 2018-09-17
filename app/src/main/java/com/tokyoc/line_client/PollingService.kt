package com.tokyoc.line_client

import android.app.IntentService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.util.Log
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import retrofit2.adapter.rxjava.HttpException
import rx.schedulers.Schedulers

class PollingService : IntentService("polling_service") {
    companion object {
        const val CHANNEL_ID = "YODA_POLLING_SERVICE"
    }

    private val binder = Binder()
    private var bound = false
    private val suppressedGroups: Set<Int> = setOf()

    inner class Binder : android.os.Binder() {
        fun getService(): PollingService {
            return this@PollingService
        }
    }

    fun suppressNotification(groupId: Int) {
        suppressedGroups.plus(groupId)
    }

    fun clearSuppressions() {
        suppressedGroups.minus(suppressedGroups)
    }

    override fun onBind(intent: Intent?): IBinder? {
        bound = true
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        clearSuppressions()
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
            Log.d("POLL", "tick")

            val status = client.getStatus().toBlocking().first() ?: continue

            if (lastStatus == null ||
                    lastStatus!!.friendshipCount != status.friendshipCount ||
                    lastStatus!!.friendshipAddedAt != lastStatus.friendshipAddedAt) {

                val friends = client.getFriends().toBlocking().single().toTypedArray()

                rx.Observable.from(friends)
                        .flatMap { return@flatMap Member.lookup(it, client) }
                        .subscribe {
                            val member = it
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
                                realm.executeTransaction { member.isFriend = Relation.OTHER }
                            }
                        }
            }

            for (summary in status.latests) {
                realm.executeTransaction {
                    var group = realm.where<Group>().equalTo("id", summary.channelId).findFirst()
                            ?: realm.createObject<Group>(summary.channelId)

                    val groupName = group.name

                    if (!suppressedGroups.contains(group.id) && summary.messageId > group.latest) {
                        client.getMessage(summary.messageId)
                                .observeOn(Schedulers.io())
                                .subscribe({
                                    val author = Member.lookup(it.author, client).toBlocking().single()
                                    val realm = Realm.getDefaultInstance()

                                    realm.executeTransaction {
                                        realm.insertOrUpdate(author)
                                    }

                                    var text = ""

                                    if (it.isEvent == 0) {
                                        text = it.content
                                    } else if (it.content == "join") {
                                        text = "${author.name}が${groupName}に参加しました"
                                    } else if (it.content == "leave") {
                                        text = "${author.name}が${groupName}から退室しました"
                                    }

                                    Log.d("POLL", "notify ${it.author}")


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
                                    Log.d("POLL", "notify failed $it")
                                })
                    }

                    group.name = summary.channelName
                    group.latest = summary.messageId
                }

                lastStatus = status

                val deleted = realm.where<Group>().findAll().map { it.id } - status.latests.map { it.channelId }

                realm.executeTransaction {
                    realm.where<Group>().`in`("id", deleted.toTypedArray()).findAll().deleteAllFromRealm()
                }
            }

            Thread.sleep(3000)
        }
    }

    override fun onDestroy() {
        Log.d("POLL", "PollingService destroyed")
        super.onDestroy()
    }
}
