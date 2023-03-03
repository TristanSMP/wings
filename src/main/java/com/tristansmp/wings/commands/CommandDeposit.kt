package com.tristansmp.wings.commands

import com.tristansmp.wings.Wings
import com.tristansmp.wings.lib.ChatRes
import khttp.post
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

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

        if (!sender.hasPermission("tsmpmarkets.deposit")) {
            sender.sendMessage(ChatRes.error("You don't have permission to use this command!"))
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

        if (item.type != Material.DIAMOND) {
            sender.sendMessage(ChatRes.error("You must be holding a diamond/diamonds to deposit!"))
            return true
        }

        player.inventory.setItemInMainHand(null)

        try {
            val response = post(
                url = "$endpoint/marketDeposit",
                data = mapOf(
                    "uuid" to uuid,
                    "amount" to item.amount
                ),
                headers = mapOf(
                    "Authorization" to token
                )
            )

            return if (response.statusCode == 200) {
                player.sendMessage(ChatRes.success("Successfully deposited ${item.amount} diamonds!"))
                true
            } else {
                val nonce = (0..100000).random()
                Wings.instance.logger.warning("Failed to deposit ${item.amount} diamonds for ${player.name} (${player.uniqueId})! (Nonce: $nonce)")
                player.sendMessage(ChatRes.error("Failed to deposit! Screenshot this error, create a ticket and send it! (Nonce: $nonce)"))
                true
            }
        } catch (e: Exception) {
            player.sendMessage(ChatRes.error("Failed to deposit!"))
            return true
        }
    }
}