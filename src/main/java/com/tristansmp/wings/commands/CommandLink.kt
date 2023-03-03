package com.tristansmp.wings.commands

import com.tristansmp.wings.Wings
import khttp.post
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CommandLink : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (sender !is Player || Wings.instance.config.config.token == null || Wings.instance.config.config.wingsApiEndpoint == null) {
            return false
        }

        // make sure we have a code
        if (args == null || args.isEmpty()) {
            return false
        }

        val player = sender
        val uuid = player.uniqueId.toString()
        val code = args[0]
        val token = Wings.instance.config.config.token ?: return false
        val endpoint = Wings.instance.config.config.wingsApiEndpoint ?: return false

        player.sendMessage("§d[Wings] §aLinking account...")

        try {
            val response = post(
                url = "$endpoint/linkAccount",
                data = mapOf(
                    "uuid" to uuid,
                    "code" to code
                ),
                headers = mapOf(
                    "Authorization" to token
                )
            )

            // check the response code
            if (response.statusCode == 200) {
                player.sendMessage("§d[Wings] §aSuccessfully linked account!")
            } else {
                player.sendMessage("§d[Wings] §cFailed to link account!")
            }
        } catch (e: Exception) {
            player.sendMessage("§d[Wings] §cFailed to link account!")
        }

        return true
    }
}