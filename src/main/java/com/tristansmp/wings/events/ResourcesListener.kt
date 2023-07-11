package com.tristansmp.wings.events

import com.tristansmp.wings.Wings
import com.tristansmp.wings.lib.ChatRes
import com.tristansmp.wings.lib.get12HourTime
import com.tristansmp.wings.lib.sendInfoIndicator
import com.tristansmp.wings.lib.sendSuccessIndicator
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerResourcePackStatusEvent

class ResourcesListener : Listener {
    @EventHandler()
    fun onPlayerJoin(event: PlayerJoinEvent) {
//        event.player.sendInfoIndicator("Attempting to apply TSMP resources...")
//
//        val scheduler = Wings.instance.server.scheduler
//
//        scheduler.runTaskAsynchronously(Wings.instance, Runnable {
//            runBlocking {
//                val res = Wings.instance.http.get("https://storage.googleapis.com/re.tristansmp.com/resources.zip.sha1")
//                val sha1 = res.body<String>()
//
//                event.player.sendInfoIndicator("Asking you to accept the resource pack...")
//
//                event.player.setResourcePack(
//                    "https://storage.googleapis.com/re.tristansmp.com/resources.zip?s=${sha1}", sha1, true,
//                    LegacyComponentSerializer.legacyAmpersand().deserialize(
//                        arrayOf(
//                            "&7&m------------------------------------",
//                            "&6Hey &d${event.player.name}&r&f!",
//                            "&6Our resource pack is required to play TSMP!",
//                            "",
//                            "&fWe promise to not change any vanilla textures, rather it",
//                            "&fpowers our custom items, blocks, and more!",
//                            "",
//                            "&7&m------------------------------------"
//                        ).joinToString("&r\n")
//                    )
//                )
//            }
//        })
    }

    @EventHandler()
    fun onPlayerResourcePackStatusEvent(event: PlayerResourcePackStatusEvent) {
        if (event.status == PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED) {
            val scheduler = Wings.instance.server.scheduler

            event.player.sendSuccessIndicator("Thanks for accepting our resource pack :)")

            scheduler.runTaskLater(Wings.instance, Runnable {
                event.player.sendInfoIndicator("Enjoy your stay on TSMP!")
            }, 20 * 3)

            scheduler.runTaskLater(Wings.instance, Runnable {
                event.player.sendInfoIndicator("It's day ${event.player.world.fullTime / 24000} and it's ${event.player.world.get12HourTime()}")
            }, 20 * 7)
        }

        if (event.status == PlayerResourcePackStatusEvent.Status.DECLINED) {
            event.player.kick(ChatRes.error("You must accept the resource pack to play on TSMP!"))
        }

        if (event.status == PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD) {
            event.player.kick(ChatRes.error("Failed to download the resource pack!"))
        }
    }
}
