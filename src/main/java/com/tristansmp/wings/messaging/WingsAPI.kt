package com.tristansmp.wings.messaging

import com.tristansmp.wings.Wings
import com.tristansmp.wings.commands.OTTPayloadReq
import com.tristansmp.wings.commands.OTTPayloadRes
import com.tristansmp.wings.lib.sendError
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.bukkit.entity.Player
import org.bukkit.plugin.messaging.PluginMessageListener
import java.nio.charset.StandardCharsets

class WingsAPI : PluginMessageListener {
    override fun onPluginMessageReceived(channel: String, player: Player, message: ByteArray?) {
        if (message == null) return

        if (!Wings.instance.commandRatelimiter.canRunCommand(player)) {
            return
        }

        val text = String(message, StandardCharsets.UTF_8)

        if (text == "OTT") {
            this.getOTT(player)
        }
    }

    private fun getOTT(player: Player) {
        val uuid = player.uniqueId.toString()
        val token = Wings.instance.config.config.token ?: return
        val endpoint = Wings.instance.config.config.wingsApiEndpoint ?: return

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
                        player.sendPluginMessage(
                            Wings.instance,
                            Wings.WINGS_API_CHANNEL,
                            "OTT:${res.token}".toByteArray()
                        )
                    } else {
                        Wings.instance.logger.warning("Failed to get OTT for ${player.name}!")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })
    }
}