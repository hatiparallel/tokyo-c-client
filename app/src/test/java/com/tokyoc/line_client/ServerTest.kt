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

        client2.sendPIN(pinMessage.pin).toBlocking().last()
        client3.sendPIN(pinMessage.pin).toBlocking().last()

        var requestMessage = pinStream.next()

        assertEquals("request", requestMessage.type)

        val requestedBy1 = requestMessage.person

        requestMessage = pinStream.next()

        assertEquals("request", requestMessage.type)

        val requestedBy2 = requestMessage.person

        assertTrue(setOf("TEST_USER2", "TEST_USER3") == setOf(requestedBy1, requestedBy2))

        client1.makeFriends(requestedBy1).toBlocking().last()
        client1.makeFriends(requestedBy2).toBlocking().last()

        var friends1 = client1.getFriends().toBlocking().single()
        var friends2 = client2.getFriends().toBlocking().single()
        var friends3 = client3.getFriends().toBlocking().single()

        assertEquals(2, friends1.size)
        assertEquals(1, friends2.size)
        assertEquals(1, friends3.size)

        assert(friends1.contains("TEST_USER2"))
        assert(friends1.contains("TEST_USER3"))

        assert(friends2.contains("TEST_USER1"))
        assert(friends3.contains("TEST_USER1"))

        var group1 = client1.makeGroup(Group(name = "Jesus Christ", members = RealmList("TEST_USER2"))).toBlocking().last()

        assertEquals("Jesus Christ", group1.name)
        assertEquals(2, group1.members.size)

        assert(group1.members.contains("TEST_USER1"))
        assert(group1.members.contains("TEST_USER2"))

        client2.invitePerson(group1.id, "TEST_USER3").toBlocking().last()
        client3.inviteMultiplePerson(group1.id, listOf("TEST_USER4", "TEST_USER5")).toBlocking().last()

        group1 = client1.getGroup(group1.id).toBlocking().last()
        assertEquals(5, group1.members.size)

        assert(group1.members.contains("TEST_USER1"))
        assert(group1.members.contains("TEST_USER2"))
        assert(group1.members.contains("TEST_USER3"))
        assert(group1.members.contains("TEST_USER4"))
        assert(group1.members.contains("TEST_USER5"))

        var group2 = client4.makeGroup(Group(name = "JHVH", members = RealmList())).toBlocking().last()
        client4.invitePerson(group2.id, "TEST_USER5").toBlocking().last()

        assert(client1.getMemberships().toBlocking().last().map { it.id }.toSet() == setOf(group1.id))
        assert(client2.getMemberships().toBlocking().last().map { it.id }.toSet() == setOf(group1.id))
        assert(client3.getMemberships().toBlocking().last().map { it.id }.toSet() == setOf(group1.id))
        assert(client4.getMemberships().toBlocking().last().map { it.id }.toSet() == setOf(group1.id, group2.id))
        assert(client5.getMemberships().toBlocking().last().map { it.id }.toSet() == setOf(group1.id, group2.id))

        client4.leaveGroup(group2.id, "TEST_USER5").toBlocking().last()

        expectError(410) {
            client4.leaveGroup(group2.id, "TEST_USER4").toBlocking().last()
        }

        assert(client1.getMemberships().toBlocking().last().map { it.id }.toSet() == setOf(group1.id))
        assert(client2.getMemberships().toBlocking().last().map { it.id }.toSet() == setOf(group1.id))
        assert(client3.getMemberships().toBlocking().last().map { it.id }.toSet() == setOf(group1.id))
        assert(client4.getMemberships().toBlocking().last().map { it.id }.toSet() == setOf(group1.id))
        assert(client5.getMemberships().toBlocking().last().map { it.id }.toSet() == setOf(group1.id))

        client1.leaveGroup(group1.id, "TEST_USER1").toBlocking().last()
        client2.leaveGroup(group1.id, "TEST_USER2").toBlocking().last()
        client3.leaveGroup(group1.id, "TEST_USER4").toBlocking().last()

        expectError(403) {
            client4.leaveGroup(group1.id, "TEST_USER3").toBlocking().last()
        }

        client5.leaveGroup(group1.id, "TEST_USER5").toBlocking().last()

        group1 = client3.getGroup(group1.id).toBlocking().last()
        assertEquals(1, group1.members.size)
        assert(group1.members.contains("TEST_USER3"))

        expectError(410) {
            client3.leaveGroup(group1.id, "TEST_USER3").toBlocking().last()
        }

        client2.deleteFriend("TEST_USER1").toBlocking().last()

        friends1 = client1.getFriends().toBlocking().single()
        friends2 = client2.getFriends().toBlocking().single()

        assert(!friends1.contains("TEST_USER2"))
        assert(friends1.contains("TEST_USER3"))

        assert(!friends2.contains("TEST_USER1"))

        client3.deleteFriend("TEST_USER1").toBlocking().last()

        friends1 = client1.getFriends().toBlocking().single()
        friends2 = client2.getFriends().toBlocking().single()
        friends3 = client3.getFriends().toBlocking().single()

        assert(!friends1.contains("TEST_USER2"))
        assert(!friends1.contains("TEST_USER3"))
        assert(!friends2.contains("TEST_USER1"))
        assert(!friends3.contains("TEST_USER1"))
    }

    fun expectError(code: Int, f: () -> Unit) {
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
