package com.tristansmp.wings.commands

import com.tristansmp.wings.Wings
import com.tristansmp.wings.lib.ChatRes
import com.tristansmp.wings.lib.SerializeUtils.Companion.itemStackFromBase64
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

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

        val player = sender
        val uuid = player.uniqueId.toString()
        val token = Wings.instance.config.config.token ?: return false
        val endpoint = Wings.instance.config.config.wingsApiEndpoint ?: return false

        if (!sender.hasPermission("wings.deliver")) {
            sender.sendMessage(ChatRes.error("You don't have permission to use this command!"))
            return true
        }


        runBlocking {
            try {

                Wings.instance.logger.info("$endpoint/marketDeposit")

                val response = Wings.instance.http.post("$endpoint/marketDelivery") {
                    header("Authorization", token)
                    contentType(ContentType.Application.Json)
                    setBody(DeliveryPayload(uuid))
                }

                if (response.status.value == 200) {
                    val body = response.body<DeliveryResponse>()

                    if (body.items.isEmpty()) {
                        player.sendMessage(ChatRes.error("You have no items to deliver!"))
                        return@runBlocking
                    }

                    for (item in body.items) {
                        if (player.inventory.firstEmpty() == -1) {
                            player.sendMessage(
                                ChatRes.error("You don't have enough space in your inventory! So I'm dropping it!"));
                            player.world.dropItem(player.location, itemStackFromBase64(item));
                        } else {
                            player.inventory.addItem(itemStackFromBase64(item));
                        }
                    }

                    player.sendMessage(ChatRes.success("Delivered ${body.items.size} items!"))

                } else {
                    val nonce = (0..100000).random()
                    Wings.instance.logger.warning("Failed to deliver items for ${player.name} (${player.uniqueId})! (Nonce: $nonce)")
                    player.sendMessage(ChatRes.error("Failed to deliver! Screenshot this error, create a ticket and send it! (Nonce: $nonce)"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                player.sendMessage(ChatRes.error("Failed to deliver!"))
            }
        }

        return true
    }
}