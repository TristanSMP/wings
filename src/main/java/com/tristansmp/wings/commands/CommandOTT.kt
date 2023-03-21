package com.tristansmp.wings.commands

import com.tristansmp.wings.Wings
import com.tristansmp.wings.lib.HandleGatewayError
import com.tristansmp.wings.lib.sendError
import com.tristansmp.wings.lib.sendInfo
import com.tristansmp.wings.lib.sendSuccess
import io.ktor.client.call.*
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.*
import jdk.incubator.foreign.ResourceScope.Handle
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@Serializable
data class OTTPayloadReq(val uuid: String)

@Serializable
data class OTTPayloadRes(val token: String)

class CommandOTT : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (sender !is Player || Wings.instance.config.config.token == null || Wings.instance.config.config.wingsApiEndpoint == null) {
            return false
        }

        if (!Wings.instance.commandRatelimiter.canRunCommand(sender)) {
            sender.sendError("You are sending commands too fast! Please wait a few seconds before trying again.")
            return true
        }

        val uuid = sender.uniqueId.toString()
        val token = Wings.instance.config.config.token ?: return false
        val endpoint = Wings.instance.config.config.wingsApiEndpoint ?: return false

        sender.sendInfo("Fetching OTT...")

        val scheduler = Wings.instance.server.scheduler

        scheduler.runTaskAsynchronously(Wings.instance, Runnable {
            runBlocking {
                try {
                    val response = Wings.instance.http.post("$endpoint/registerOTT") {
                        header("Authorization", token)
                        contentType(ContentType.Application.Json)
                        setBody(OTTPayloadReq(uuid))
                    }

                    if (response.status.value == 200) {
                        val res = response.body<OTTPayloadRes>()
                        sender.sendMessage("%BEGIN TSMPOTT% ${res.token} %END TSMPOTT%")
                    } else {
                        HandleGatewayError(response, sender)
                    }
                } catch (e: Exception) {
                    sender.sendError("Failed to talk to the TSMP gateway.")
                }
            }
        })

        return true
    }
}