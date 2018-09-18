package com.tokyoc.line_client

import android.util.Log
import io.realm.RealmList
import org.junit.Test

import org.junit.Assert.*
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
        return

        val endpoint = "http://localhost:9000/"
        val client1 = Client.build("TEST_TOKEN1", endpoint)
        val client2 = Client.build("TEST_TOKEN2", endpoint)
        val client3 = Client.build("TEST_TOKEN3", endpoint)
        val client4 = Client.build("TEST_TOKEN4", endpoint)
        val client5 = Client.build("TEST_TOKEN5", endpoint)

        val pinStream = client1.getPIN().flatMap {
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
        }.filter { it.type != "noop" }.toBlocking()

        val pin = pinStream.first { it.type == "pin" }.pin

        client2.sendPIN(pin).toBlocking().last()
        client3.sendPIN(pin).toBlocking().last()

        val s = pinStream.first { it.type == "request" }.person

        client1.makeFriends(s).toBlocking().last()
        client1.makeFriends(pinStream.first { it.type == "request" }.person).toBlocking().last()

        val friends1 = client1.getFriends().toBlocking().single()
        val friends2 = client2.getFriends().toBlocking().single()
        val friends3 = client3.getFriends().toBlocking().single()

        assertEquals(2, friends1.size)
        assertEquals(1, friends2.size)
        assertEquals(1, friends3.size)

        assert(friends1.contains("TEST_USER2"))
        assert(friends1.contains("TEST_USER3"))

        assert(friends2.contains("TEST_USER1"))
        assert(friends3.contains("TEST_USER1"))

        var group1 = client1.makeGroup(Group(name = "Jesus Christ", members = RealmList("TEST_USER2"))).toBlocking().last()

        assertEquals("Jesus Christ", group1.name)
        assertEquals(2, group1.members)
        assert(group1.members.contains("TEST_USER1"))
        assert(group1.members.contains("TEST_USER2"))

        client2.invitePerson(group1.id, "TEST_USER3").toBlocking().last()
        client3.inviteMultiplePerson(group1.id, listOf("TEST_USER4", "TEST_USER5")).toBlocking().last()

        group1 = client1.getGroup(group1.id).toBlocking().last()
        assertEquals(5, group1.members)
        assert(group1.members.contains("TEST_USER3"))
        assert(group1.members.contains("TEST_USER4"))
        assert(group1.members.contains("TEST_USER5"))

        client1.leaveGroup(group1.id, "TEST_USER1").toBlocking().last()
        client2.leaveGroup(group1.id, "TEST_USER2").toBlocking().last()
        client3.leaveGroup(group1.id, "TEST_USER4").toBlocking().last()
        client4.leaveGroup(group1.id, "TEST_USER3").toBlocking().last()
        client5.leaveGroup(group1.id, "TEST_USER5").toBlocking().last()

        group1 = client1.getGroup(group1.id).toBlocking().last()
        assertEquals(1, group1.members)
        assert(group1.members.contains("TEST_USER3"))

        client3.leaveGroup(group1.id, "TEST_USER3").toBlocking().last()

        group1 = client1.getGroup(group1.id).toBlocking().last()
    }
}
