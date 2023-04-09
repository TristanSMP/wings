package com.tristansmp.wings.events

import com.tristansmp.wings.Wings
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class ResourcesListener : Listener {
    @EventHandler()
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val scheduler = Wings.instance.server.scheduler

        scheduler.runTaskAsynchronously(Wings.instance, Runnable {
            runBlocking {
                val res = Wings.instance.http.get("https://storage.googleapis.com/re.tristansmp.com/resources.zip.sha1")
                val sha1 = res.body<String>()

                event.player.setResourcePack(
                    "https://storage.googleapis.com/re.tristansmp.com/resources.zip", sha1, true,
                    LegacyComponentSerializer.legacyAmpersand().deserialize(
                        arrayOf(
                            "&7&m------------------------------------",
                            "&6Hey &d${event.player.name}&r&f!",
                            "&6Our resource pack is required to play TSMP!",
                            "",
                            "&fWe promise to not change any vanilla textures, rather it",
                            "&fpowers our custom items, blocks, and more!",
                            "",
                            "&7&m------------------------------------"
                        ).joinToString("&r\n")
                    )
                )
            }
        })
    }
}
