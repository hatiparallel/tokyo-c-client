package com.tokyoc.line_client

import io.realm.RealmList
import org.junit.Test

import org.junit.Assert.*
import retrofit2.adapter.rxjava.HttpException
import rx.schedulers.Schedulers
import java.io.IOException

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ServerTest {
    @Test
    fun run() {
        val endpoint = "http://localhost:9000/"
        val client1 = Client.build("TEST_TOKEN1", endpoint)
        val client2 = Client.build("TEST_TOKEN2", endpoint)
        val client3 = Client.build("TEST_TOKEN3", endpoint)
        val client4 = Client.build("TEST_TOKEN4", endpoint)
        val client5 = Client.build("TEST_TOKEN5", endpoint)


        // TEST_USER1でPINを発行
        val pinStream = client1.getPIN()
                .subscribeOn(Schedulers.io())
                .flatMap {
                    val source = it.source()

                    rx.Observable.create(rx.Observable.OnSubscribe<PinEvent> {
                        try {
                            while (!source.exhausted()) {
                                it.onNext(Client.gson.fromJson<PinEvent>(source.readUtf8Line(), PinEvent::class.java))
                            }

                            it.onCompleted()
                        } catch (e: IOException) {
                            it.onError(e)
                        }
                    })
                }.filter { it.type != "noop" }.toBlocking().iterator

        val pinMessage = pinStream.next()

        assertEquals("pin", pinMessage.type)

        // TEST_USER1のPINを用いて、TEST_USER2とTEST_USER3が友達リクエストを送信
        client2.sendPIN(pinMessage.pin).toBlocking().last()
        client3.sendPIN(pinMessage.pin).toBlocking().last()

        // TEST_USER1が(TEST_USER2|TEST_USER3)のリクエストを受信
        var requestMessage = pinStream.next()

        assertEquals("request", requestMessage.type)

        val requestedBy1 = requestMessage.person

        // TEST_USER1が(TEST_USER2|TEST_USER3)のリクエストを受信
        requestMessage = pinStream.next()

        assertEquals("request", requestMessage.type)

        val requestedBy2 = requestMessage.person

        // TEST_USER2とTEST_USER3のいずれからもリクエストが来ていることを確認
        assertTrue(setOf("TEST_USER2", "TEST_USER3") == setOf(requestedBy1, requestedBy2))


        // TEST_USER2とTEST_USER3からのリクエストを承認
        client1.makeFriends(requestedBy1).toBlocking().last()
        client1.makeFriends(requestedBy2).toBlocking().last()

        // {TEST_USER1,TEST_USER2,TEST_USER3}の友達リストを取得
        var friends1 = client1.getFriends().toBlocking().single().toSet()
        var friends2 = client2.getFriends().toBlocking().single().toSet()
        var friends3 = client3.getFriends().toBlocking().single().toSet()

        // {TEST_USER1,TEST_USER2,TEST_USER3}の友達リストが期待されるものであることを確認
        assert(friends1 == setOf("TEST_USER2", "TEST_USER3"))
        assert(friends2 == setOf("TEST_USER1"))
        assert(friends3 == setOf("TEST_USER1"))

        // TEST_USER1がTEST_USER2を初期メンバーとしてグループ"Jesus Christ"を作成
        var group1 = client1.makeGroup(Group(name = "Jesus Christ", members = RealmList("TEST_USER2"))).toBlocking().last()

        // グループ"Jesus Christ"が正しく作成されたことを確認
        assertEquals("Jesus Christ", group1.name)
        assert(group1.members.toSet() == setOf("TEST_USER1", "TEST_USER2"))

        // TEST_USER2がTEST_USER3をグループ"Jesus Christ"に招待
        client2.invitePerson(group1.id, "TEST_USER3").toBlocking().last()

        // TEST_USER3が{TEST_USER4,TEST_USER5}をグループ"Jesus Christ"に一括招待
        client3.inviteMultiplePerson(group1.id, listOf("TEST_USER4", "TEST_USER5")).toBlocking().last()

        // グループ"Jesus Christ"の情報を再取得
        group1 = client1.getGroup(group1.id).toBlocking().last()

        // グループ"Jesus Christ"のメンバーを確認
        assert(group1.members == setOf(
                "TEST_USER1",
                "TEST_USER2",
                "TEST_USER3",
                "TEST_USER4",
                "TEST_USER5"))

        // TEST_USER4がグループ"JHVH"の情報を再取得
        var group2 = client4.makeGroup(Group(name = "JHVH", members = RealmList())).toBlocking().last()

        // TEST_USER4がグループ"JHVH"にTEST_USER5を招待
        client4.invitePerson(group2.id, "TEST_USER5").toBlocking().last()


        // 各ユーザーのグループ参加状況を確認
        assert(client1.getMemberships().toBlocking().last().map { it.id }.toSet() == setOf(group1.id))
        assert(client2.getMemberships().toBlocking().last().map { it.id }.toSet() == setOf(group1.id))
        assert(client3.getMemberships().toBlocking().last().map { it.id }.toSet() == setOf(group1.id))
        assert(client4.getMemberships().toBlocking().last().map { it.id }.toSet() == setOf(group1.id, group2.id))
        assert(client5.getMemberships().toBlocking().last().map { it.id }.toSet() == setOf(group1.id, group2.id))

        // TEST_USER4がTEST_USER5をグループ"JHVH"から退出させる
        client4.leaveGroup(group2.id, "TEST_USER5").toBlocking().last()


        // TEST_USER4が自身をグループ"JHVH"から退出させ、グループは消滅する
        mustCatch(410) {
            client4.leaveGroup(group2.id, "TEST_USER4").toBlocking().last()
        }

        // 各ユーザーのグループ参加状況を確認
        assert(client1.getMemberships().toBlocking().last().map { it.id }.toSet() == setOf(group1.id))
        assert(client2.getMemberships().toBlocking().last().map { it.id }.toSet() == setOf(group1.id))
        assert(client3.getMemberships().toBlocking().last().map { it.id }.toSet() == setOf(group1.id))
        assert(client4.getMemberships().toBlocking().last().map { it.id }.toSet() == setOf(group1.id))
        assert(client5.getMemberships().toBlocking().last().map { it.id }.toSet() == setOf(group1.id))

        // TEST_USER1が自身をグループ"Jesus Christ"から退出させる
        client1.leaveGroup(group1.id, "TEST_USER1").toBlocking().last()

        // TEST_USER2が自身をグループ"Jesus Christ"から退出させる
        client2.leaveGroup(group1.id, "TEST_USER2").toBlocking().last()

        // TEST_USER3がTEST_USER4をグループ"Jesus Christ"から退出させる
        client3.leaveGroup(group1.id, "TEST_USER4").toBlocking().last()

        // TEST_USER4がTEST_USER3をグループ"Jesus Christ"から退出させようとするが、
        // TEST_USER4はすでにメンバーでなく権限を持たない

        mustCatch(403) {
            client4.leaveGroup(group1.id, "TEST_USER3").toBlocking().last()
        }

        // TEST_USER5が自身をグループ"Jesus Christ"から退出させる
        client5.leaveGroup(group1.id, "TEST_USER5").toBlocking().last()

        // グループ"Jesus Christ"の情報を再取得
        group1 = client3.getGroup(group1.id).toBlocking().last()
        assert(group1.members.toSet() == setOf("TEST_USER3"))


        // TEST_USER3が自身をグループ"Jesus Christ"から退出させ、グループは消滅する
        mustCatch(410) {
            client3.leaveGroup(group1.id, "TEST_USER3").toBlocking().last()
        }

        // TEST_USER2がTEST_USER1を友達から削除
        client2.deleteFriend("TEST_USER1").toBlocking().last()

        // {TEST_USER1,TEST_USER2}の友達リストを取得
        friends1 = client1.getFriends().toBlocking().single().toSet()
        friends2 = client2.getFriends().toBlocking().single().toSet()


        // {TEST_USER1,TEST_USER2}の友達リストを確認
        assert(friends1 == setOf("TEST_USER3"))
        assert(friends2.isEmpty())

        // TEST_USER3がTEST_USER1を友達から削除
        client3.deleteFriend("TEST_USER1").toBlocking().last()

        // {TEST_USER1,TEST_USER2,TEST_USER3}の友達リストを取得
        friends1 = client1.getFriends().toBlocking().single().toSet()
        friends2 = client2.getFriends().toBlocking().single().toSet()
        friends3 = client3.getFriends().toBlocking().single().toSet()

        // {TEST_USER1,TEST_USER2}の友達リストを確認
        assert(friends1.isEmpty())
        assert(friends2.isEmpty())
        assert(friends3.isEmpty())
    }

    fun mustCatch(code: Int, f: () -> Unit) {
        try {
            f()
            throw Exception("can't happen")
        } catch (e: RuntimeException) {
            val cause = e.cause

            if (cause is HttpException && cause.code() == code) {
                return
            }

            throw cause ?: return
        }
    }
}
