package com.tristansmp.wings.events

import com.tristansmp.wings.Wings
import com.tristansmp.wings.lib.ChatRes
import com.tristansmp.wings.lib.completelyDebuff
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerResourcePackStatusEvent
import org.bukkit.potion.PotionEffect
import java.util.*

class ResourcesListener : Listener {

    private val PLAYER_POTION_EFFECTS_MAP = mutableMapOf<UUID, List<PotionEffect>>()

    @EventHandler()
    fun onPlayerJoin(event: PlayerJoinEvent) {
        PLAYER_POTION_EFFECTS_MAP[event.player.uniqueId] = event.player.activePotionEffects.toList()
        event.player.activePotionEffects.forEach { event.player.removePotionEffect(it.type) }

        event.player.isInvulnerable = true
        event.player.completelyDebuff()

        val scheduler = Wings.instance.server.scheduler

        scheduler.runTaskAsynchronously(Wings.instance, Runnable {
            runBlocking {
                val res = Wings.instance.http.get("https://storage.googleapis.com/re.tristansmp.com/resources.zip.sha1")
                val sha1 = res.body<String>()

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
        if (event.status == PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED) {
            event.player.isInvulnerable = false
            event.player.activePotionEffects.forEach { event.player.removePotionEffect(it.type) }
            PLAYER_POTION_EFFECTS_MAP[event.player.uniqueId]?.forEach { event.player.addPotionEffect(it) }
            PLAYER_POTION_EFFECTS_MAP.remove(event.player.uniqueId)
        }

        if (event.status == PlayerResourcePackStatusEvent.Status.DECLINED) {
            event.player.kick(ChatRes.error("You must accept the resource pack to play on TSMP!"))
        }

        if (event.status == PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD) {
            event.player.kick(ChatRes.error("Failed to download the resource pack!"))
        }
    }

    @EventHandler()
    fun onPlayerLeave(event: PlayerQuitEvent) {
        PLAYER_POTION_EFFECTS_MAP.remove(event.player.uniqueId)
    }
}
