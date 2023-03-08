package com.tristansmp.wings.events

import com.tristansmp.wings.Wings
import com.tristansmp.wings.lib.ChatRes
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.scheduler.BukkitScheduler

class InPersonPOS : Listener {

    val rateLimits = mutableMapOf<String, Long>()

    @EventHandler()
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) {
            return
        }

        if (event.clickedBlock == null) {
            return
        }

        val block = event.clickedBlock!!

        if (block.type.name.contains("SIGN")) {
            if (rateLimits.containsKey(block.location.toString())) {
                if (System.currentTimeMillis() - rateLimits[block.location.toString()]!! < 1000) {
                    event.player.sendMessage(ChatRes.error("Please wait a second before interacting with this sign shop again!"))
                    return
                }
            }

            rateLimits[block.location.toString()] = System.currentTimeMillis()

            val scheduler = Wings.instance.server.scheduler

            scheduler.runTaskAsynchronously(Wings.instance, Runnable {
                runBlocking {
                    val signShop = Wings.instance.inPersonPOSManager.GetSignShop(block.location) ?: return@runBlocking

                    scheduler.runTask(Wings.instance, Runnable {
                        Wings.instance.logger.info("player uuid: ${event.player.uniqueId}")

                        if (signShop.owner == event.player.uniqueId) {
                            event.player.sendMessage(ChatRes.error("Refreshed sign shop!"))
                            return@Runnable
                        } else {
                            scheduler.runTaskAsynchronously(Wings.instance, Runnable {
                                runBlocking {
                                    Wings.instance.inPersonPOSManager.BuyItem(signShop, event.player)
                                }
                            })
                        }
                    })
                }
            })

        }

    }

    @EventHandler()
    fun onBlockBreak(event: BlockBreakEvent) {
        if (event.block.type.name.contains("SIGN")) {
            val scheduler = Wings.instance.server.scheduler

            scheduler.runTaskAsynchronously(Wings.instance, Runnable {
                runBlocking {
                    val signShop =
                        Wings.instance.inPersonPOSManager.GetSignShop(event.block.location) ?: return@runBlocking

                    if (signShop.owner != event.player.uniqueId) {
                        event.player.sendMessage(ChatRes.error("you shouldn't of done that..."))
                    } else {
                        event.player.sendMessage(ChatRes.success("You have broken a sign shop!"))
                    }

                    Wings.instance.inPersonPOSManager.DeleteSignShop(event.block.location, event.player)
                }
            })
        }
    }
}