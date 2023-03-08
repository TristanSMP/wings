package com.tristansmp.wings.commands

import com.tristansmp.wings.Wings
import com.tristansmp.wings.lib.ChatRes
import kotlinx.coroutines.runBlocking
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CommandCreateSignShop : CommandExecutor {
    val errorMsg =
        "You must specify a discovered item's b64 key! You can find this in the URL of an item's page. For example, if the URL is https://tristansmp.com/market/example, the b64 key is example."

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (sender !is Player) {
            sender.sendMessage("You must be a player to use this command!")
            return true
        }

        val player = sender

        if (!sender.hasPermission("wings.create-sign-shop")) {
            sender.sendMessage(ChatRes.error("You don't have permission to use this command!"))
            return true
        }

        if (args == null) {
            sender.sendMessage(ChatRes.error(errorMsg))
            return true
        }

        if (args.size != 1) {
            sender.sendMessage(ChatRes.error(errorMsg))
            return true
        }

        val b64key = args[0]

        val lookingAt = player.getTargetBlock(null, 5)

        if (!lookingAt.type.name.contains("SIGN")) {
            sender.sendMessage(ChatRes.error("You must be looking at a sign to create a sign shop!"))
            return true
        }

        val scheduler = Wings.instance.server.scheduler

        scheduler.runTaskAsynchronously(Wings.instance, Runnable {
            runBlocking {
                try {
                    val existingSignShop = Wings.instance.inPersonPOSManager.GetSignShop(lookingAt.location)

                    if (existingSignShop != null) {
                        player.sendMessage(ChatRes.error("There is already a sign shop at this location!"))
                        return@runBlocking
                    }

                    val signShop = Wings.instance.inPersonPOSManager.CreateSignShop(lookingAt.location, player, b64key)

                    if (signShop == null) {
                        player.sendMessage(ChatRes.error("Failed to create sign shop!"))
                        return@runBlocking
                    }

                    player.sendMessage(ChatRes.success("Successfully created sign shop!"))
                } catch (e: Exception) {
                    e.printStackTrace()
                    player.sendMessage(ChatRes.error("Failed to create sign shop!"))
                }
            }
        })
        return true
    }
}