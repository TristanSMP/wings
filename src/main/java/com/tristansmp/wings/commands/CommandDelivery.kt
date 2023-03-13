package com.tristansmp.wings.commands

import com.tristansmp.wings.Wings
import com.tristansmp.wings.lib.ChatRes
import com.tristansmp.wings.lib.SerializeUtils.Companion.itemStackFromBase64
import com.tristansmp.wings.lib.sendError
import com.tristansmp.wings.lib.sendSuccess
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@Serializable
data class DeliveryPayload(val uuid: String)

@Serializable
data class DeliveryResponse(val items: List<String>)

class CommandDeliver : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (sender !is Player) {
            sender.sendMessage("You must be a player to use this command!")
            return true
        }

        if (!Wings.instance.commandRatelimiter.canRunCommand(sender)) {
            sender.sendError("You are sending commands too fast! Please wait a few seconds before trying again.")
            return true
        }

        val player = sender
        val uuid = player.uniqueId.toString()
        val token = Wings.instance.config.config.token ?: return false
        val endpoint = Wings.instance.config.config.wingsApiEndpoint ?: return false

        if (!sender.hasPermission("wings.deliver")) {
            sender.sendError("You don't have permission to use this command!")
            return true
        }

        val scheduler = Wings.instance.server.scheduler

        scheduler.runTaskAsynchronously(Wings.instance, Runnable {
            runBlocking {
                val nonce = (0..100000).random()

                try {
                    val response = Wings.instance.http.post("$endpoint/marketDelivery") {
                        header("Authorization", token)
                        contentType(ContentType.Application.Json)
                        setBody(DeliveryPayload(uuid))
                    }

                    if (response.status.isSuccess()) {
                        val body = response.body<DeliveryResponse>()

                        if (body.items.isEmpty()) {
                            player.sendError("You have no items to deliver!")
                            return@runBlocking
                        }

                        for (item in body.items) {
                            scheduler.runTask(Wings.instance, Runnable {
                                if (player.inventory.firstEmpty() == -1) {
                                    player.sendError("You don't have enough space in your inventory! So I'm dropping it!")
                                    player.world.dropItem(player.location, itemStackFromBase64(item))
                                } else {
                                    player.inventory.addItem(itemStackFromBase64(item))
                                }
                            })
                        }

                        player.sendSuccess("Delivered ${body.items.size} items!")

                    } else {
                        Wings.instance.logger.warning("Failed to deliver items for ${player.name} (${player.uniqueId})! (Nonce: $nonce)")
                        player.sendError("Failed to deliver! Screenshot this error, create a ticket and send it! (Nonce: $nonce)")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    player.sendError("Fatal error! Please screenshot this error and create a ticket! (Nonce: ${nonce})")
                }
            }
        })
        return true
    }
}