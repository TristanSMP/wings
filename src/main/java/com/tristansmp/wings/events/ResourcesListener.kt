package com.tristansmp.wings.events

import com.tristansmp.wings.Wings
import com.tristansmp.wings.lib.ChatRes
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerResourcePackStatusEvent

class ResourcesListener : Listener {
    @EventHandler()
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val scheduler = Wings.instance.server.scheduler

        scheduler.runTaskAsynchronously(Wings.instance, Runnable {
            runBlocking {
                val res = Wings.instance.http.get("https://storage.googleapis.com/re.tristansmp.com/resources.zip.sha1")
                val sha1 = res.body<String>()

                Wings.instance.logger.info("Resource pack SHA1: $sha1")

                event.player.setResourcePack(
                    "https://storage.googleapis.com/re.tristansmp.com/resources.zip?s=${sha1}", sha1, true,
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

    @EventHandler()
    fun onPlayerResourcePackStatusEvent(event: PlayerResourcePackStatusEvent) {
        if (event.status == PlayerResourcePackStatusEvent.Status.DECLINED) {
            event.player.kick(ChatRes.info("You must accept the resource pack to play on TSMP!"))
        } else if (event.status == PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD) {
            event.player.kick(ChatRes.info("Failed to download the resource pack! Try rejoining."))
        }
    }
}
