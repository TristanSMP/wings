package com.tristansmp.wings.lib

import io.ktor.client.call.*
import io.ktor.client.statement.*
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player

@Serializable
data class PossibleErrorPayload(
    val userLandError: String,
)

suspend fun HandleGatewayError(res: HttpResponse, player: Player) {
    try {
        val body = res.body<PossibleErrorPayload>()
        player.sendMessage(ChatRes.error(body.userLandError))
    } catch (e: Exception) {
        player.sendMessage("An unknown error occurred.")
    }
}