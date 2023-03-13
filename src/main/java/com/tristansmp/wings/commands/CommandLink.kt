package com.tristansmp.wings.commands

import com.tristansmp.wings.Wings
import com.tristansmp.wings.lib.sendError
import com.tristansmp.wings.lib.sendInfo
import com.tristansmp.wings.lib.sendSuccess
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@Serializable
data class LinkPayload(val uuid: String, val code: String)

class CommandLink : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (sender !is Player || Wings.instance.config.config.token == null || Wings.instance.config.config.wingsApiEndpoint == null) {
            return false
        }

        if (!Wings.instance.commandRatelimiter.canRunCommand(sender)) {
            sender.sendError("You are sending commands too fast! Please wait a few seconds before trying again.")
            return true
        }

        if (args == null || args.isEmpty()) {
            return false
        }

        val uuid = sender.uniqueId.toString()
        val code = args[0]
        val token = Wings.instance.config.config.token ?: return false
        val endpoint = Wings.instance.config.config.wingsApiEndpoint ?: return false

        sender.sendInfo("Linking account...")

        val scheduler = Wings.instance.server.scheduler

        scheduler.runTaskAsynchronously(Wings.instance, Runnable {
            runBlocking {
                try {
                    val response = Wings.instance.http.post("$endpoint/linkAccount") {
                        header("Authorization", token)
                        contentType(ContentType.Application.Json)
                        setBody(LinkPayload(uuid, code))
                    }

                    if (response.status.value == 200) {
                        sender.sendSuccess("Successfully linked account!")
                    } else {
                        sender.sendError("Failed to link account!")
                    }
                } catch (e: Exception) {
                    sender.sendError("Failed to link account!")
                }
            }
        })

        return true
    }
}