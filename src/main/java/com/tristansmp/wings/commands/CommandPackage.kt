package com.tristansmp.wings.commands

import com.tristansmp.wings.Wings
import com.tristansmp.wings.lib.ChatRes
import com.tristansmp.wings.lib.SerializeUtils
import com.tristansmp.wings.lib.toJsonObject
import com.tristansmp.wings.routes.toJson
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@Serializable
data class PackagePayload(val uuid: String, val price: Int, val item: JsonObject)

class CommandPackage : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (sender !is Player) {
            sender.sendMessage("You must be a player to use this command!")
            return true
        }

        val player = sender
        val uuid = player.uniqueId.toString()
        val token = Wings.instance.config.config.token ?: return false
        val endpoint = Wings.instance.config.config.wingsApiEndpoint ?: return false

        if (!sender.hasPermission("wings.package")) {
            sender.sendMessage(ChatRes.error("You don't have permission to use this command!"))
            return true
        }

        if (args == null) {
            sender.sendMessage(ChatRes.error("You must specify a price!"))
            return true
        }

        if (args.size != 1) {
            sender.sendMessage(ChatRes.error("You must specify a price!"))
            return true
        }

        val price = args[0].toIntOrNull()

        if (price == null) {
            sender.sendMessage(ChatRes.error("You must specify a valid price!"))
            return true
        }

        if (price < 1) {
            sender.sendMessage(ChatRes.error("You must specify a valid price!"))
            return true
        }

        val item: ItemStack = player.inventory.itemInMainHand

        if (item == null) {
            sender.sendMessage(ChatRes.error("You must be holding an item to deposit!"))
            return true
        }

        if (item.type == Material.AIR) {
            sender.sendMessage(ChatRes.error("You must be holding an item to deposit!"))
            return true
        }

        player.inventory.setItemInMainHand(null)

        val scheduler = Wings.instance.server.scheduler

        scheduler.runTaskAsynchronously(Wings.instance, Runnable {
            runBlocking {
                try {

                    Wings.instance.logger.info("$endpoint/marketPackage")

                    val response = Wings.instance.http.post("$endpoint/marketPackage") {
                        header("Authorization", token)
                        contentType(ContentType.Application.Json)
                        setBody(PackagePayload(uuid, price, JsonObject(item.toJson().toJsonObject())))
                    }

                    if (response.status.value == 200) {
                        player.sendMessage(ChatRes.success("Successfully listed item for $price diamonds!"))
                    } else {
                        val nonce = (0..100000).random()
                        Wings.instance.logger.warning("Failed to package item for ${player.name} (${player.uniqueId})! (Nonce: $nonce)")
                        Wings.instance.logger.warning("Nonce: $nonce b64: ${SerializeUtils.itemStackToBase64(item)}")
                        player.sendMessage(ChatRes.error("Failed to package! Screenshot this error, create a ticket and send it! (Nonce: $nonce)"))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    player.sendMessage(ChatRes.error("Failed to package!"))
                }
            }
        })

        return true
    }
}