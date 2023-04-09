package com.tristansmp.wings.events

import com.tristansmp.wings.Wings
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class ResourcesListener : Listener {
    @EventHandler()
    fun onPlayerJoin(event: PlayerJoinEvent) {

        val scheduler = Wings.instance.server.scheduler

        scheduler.runTaskAsynchronously(Wings.instance, Runnable {
            runBlocking {
                val res = Wings.instance.http.get("https://re.tristansmp.com/resources.zip.sha1")
                val sha1 = res.body<String>()

                event.player.setResourcePack(
                    "https://re.tristansmp.com/resources.zip", sha1, true,
                )
            }
        })
    }
}
