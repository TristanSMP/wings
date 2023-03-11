package com.tristansmp.wings.commands

import com.tristansmp.wings.Wings
import com.tristansmp.wings.lib.ChatRes
import com.tristansmp.wings.lib.sendError
import com.tristansmp.wings.lib.sendSuccess
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@Serializable
data class DepositPayload(val uuid: String, val amount: Int)

class CommandDeposit : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (sender !is Player) {
            sender.sendMessage("You must be a player to use this command!")
            return true
        }

        val player = sender
        val uuid = player.uniqueId.toString()
        val token = Wings.instance.config.config.token ?: return false
        val endpoint = Wings.instance.config.config.wingsApiEndpoint ?: return false

        if (!sender.hasPermission("wings.deposit")) {
            sender.sendError("You don't have permission to use this command!")
            return true
        }

        val item: ItemStack = player.inventory.itemInMainHand

        if (item == null) {
            sender.sendError("You must be holding an item to deposit!")
            return true
        }

        if (item.type == Material.AIR) {
            sender.sendError("You must be holding an item to deposit!")
            return true
        }

        if (item.type != Material.DIAMOND) {
            sender.sendError("You must be holding a diamond/diamonds to deposit!")
            return true
        }

        player.inventory.setItemInMainHand(null)

        val scheduler = Wings.instance.server.scheduler

        scheduler.runTaskAsynchronously(Wings.instance, Runnable {
            runBlocking {
                try {

                    Wings.instance.logger.info("$endpoint/marketDeposit")

                    val response = Wings.instance.http.post("$endpoint/marketDeposit") {
                        header("Authorization", token)
                        contentType(ContentType.Application.Json)
                        setBody(DepositPayload(uuid, item.amount))
                    }

                    if (response.status.value == 200) {
                        player.sendSuccess("Successfully deposited ${item.amount} diamonds!")
                    } else {
                        val nonce = (0..100000).random()
                        Wings.instance.logger.warning("Failed to deposit ${item.amount} diamonds for ${player.name} (${player.uniqueId})! (Nonce: $nonce)")
                        player.sendError("Failed to deposit! Screenshot this error, create a ticket and send it! (Nonce: $nonce)")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    player.sendError("Failed to deposit!")
                }
            }
        })

        return true
    }
}