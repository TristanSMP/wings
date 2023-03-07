package com.tristansmp.wings.commands

import com.tristansmp.wings.Wings
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.launch
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

        if (args == null || args.isEmpty()) {
            return false
        }

        val uuid = sender.uniqueId.toString()
        val code = args[0]
        val token = Wings.instance.config.config.token ?: return false
        val endpoint = Wings.instance.config.config.wingsApiEndpoint ?: return false

        sender.sendMessage("§d[Wings] §aLinking account...")

        runBlocking {
            launch {
                try {
                    val response = Wings.instance.http.post("$endpoint/linkAccount") {
                        header("Authorization", token)
                        contentType(ContentType.Application.Json)
                        setBody(LinkPayload(uuid, code))
                    }

                    if (response.status.value == 200) {
                        sender.sendMessage("§d[Wings] §aSuccessfully linked account!")
                    } else {
                        sender.sendMessage("§d[Wings] §cFailed to link account!")
                    }
                } catch (e: Exception) {
                    sender.sendMessage("§d[Wings] §cFailed to link account!")
                }
            }
        }

        return true
    }
}