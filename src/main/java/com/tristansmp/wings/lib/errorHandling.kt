package com.tristansmp.wings.lib

import com.tristansmp.wings.Wings
import io.ktor.client.call.*
import io.ktor.client.statement.*
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player

@Serializable
data class PossibleErrorPayload(
    val userLandError: String,
)

suspend fun HandleGatewayError(res: HttpResponse, player: Player) {
    val scheduler = Wings.instance.server.scheduler

    try {
        val body = res.body<PossibleErrorPayload>()

        scheduler.runTask(Wings.instance, Runnable {
            player.sendMessage(ChatRes.error(body.userLandError))
        })
    } catch (e: Exception) {
        scheduler.runTask(Wings.instance, Runnable {
            player.sendMessage(ChatRes.error("An unknown error occurred."))
        })
    }
}