package com.tristansmp.wings.InPersonPOS

import com.tristansmp.wings.Wings
import com.tristansmp.wings.lib.ChatRes
import com.tristansmp.wings.lib.HandleGatewayError
import com.tristansmp.wings.lib.strSerialize
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Location
import org.bukkit.block.Sign
import org.bukkit.entity.Player

@Serializable
data class GETSignShopPayload(val location: String)

@Serializable
data class CreateSignShopPayload(val uuid: String, val b64key: String, val location: String)

@Serializable
data class BuySSItemPayload(val location: String, val uuid: String)

class InPersonPOSManager {
    suspend fun GetSignShop(at: Location): SignShop? {
        val token = Wings.instance.config.config.token ?: return null
        val endpoint = Wings.instance.config.config.wingsApiEndpoint ?: return null

        val response = Wings.instance.http.get("$endpoint/resolveSignShop") {
            header("Authorization", token)
            contentType(ContentType.Application.Json)
            setBody(GETSignShopPayload(at.strSerialize()))
        }

        if (response.status.value == 200) {
            val body = response.body<SignShopPayload>()

            val shop = SignShop(
                body
            )

            RenderSignShop(shop)

            return shop
        }

        return null
    }

    suspend fun CreateSignShop(at: Location, owner: Player, b64key: String): SignShop? {
        val token = Wings.instance.config.config.token ?: return null
        val endpoint = Wings.instance.config.config.wingsApiEndpoint ?: return null

        val response = Wings.instance.http.post("$endpoint/createSignShop") {
            header("Authorization", token)
            contentType(ContentType.Application.Json)
            setBody(CreateSignShopPayload(owner.uniqueId.toString(), b64key, at.strSerialize()))
        }

        if (response.status.value == 200) {
            val body = response.body<SignShopPayload>()

            val shop = SignShop(
                body
            )

            RenderSignShop(shop)

            return shop
        } else {
            HandleGatewayError(response, owner)
        }

        return null
    }

    suspend fun DeleteSignShop(at: Location, player: Player) {
        val token = Wings.instance.config.config.token ?: return
        val endpoint = Wings.instance.config.config.wingsApiEndpoint ?: return

        val response = Wings.instance.http.delete("$endpoint/deleteSignShop") {
            header("Authorization", token)
            contentType(ContentType.Application.Json)
            setBody(GETSignShopPayload(at.strSerialize()))
        }

        if (!response.status.isSuccess()) {
            HandleGatewayError(response, player)
        }
    }

    fun RenderSignShop(shop: SignShop) {
        val scheduler = Wings.instance.server.scheduler

        scheduler.runTask(Wings.instance, Runnable {
            val sign = shop.location.block.state as Sign
            val ownerPlayer = Wings.instance.server.getOfflinePlayer(shop.owner)

            sign.line(0, shop.item.displayName())
            sign.line(1, LegacyComponentSerializer.legacyAmpersand().deserialize("&d${ownerPlayer.name}"))
            if (shop.lastKnownPrice != null) {
                sign.line(
                    2,
                    LegacyComponentSerializer.legacyAmpersand()
                        .deserialize("&b♦&l${shop.lastKnownPrice} &r&7per &r&b&l${shop.item.amount}")
                )
                sign.line(3, LegacyComponentSerializer.legacyAmpersand().deserialize("&b&l${shop.stock} &r&7left"))
            } else {
                sign.line(2, LegacyComponentSerializer.legacyAmpersand().deserialize("&cNot in stock"))
                sign.line(3, LegacyComponentSerializer.legacyAmpersand().deserialize("&7try again later :("))
            }

            val loc = shop.location.clone()

            if (shop.stock > 0) {
                loc.world?.playSound(loc, org.bukkit.Sound.ENTITY_VILLAGER_YES, 1.0f, 1.0f)
                loc.world?.spawnParticle(org.bukkit.Particle.VILLAGER_HAPPY, loc, 10)
            } else {
                loc.world?.playSound(loc, org.bukkit.Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f)
                loc.world?.spawnParticle(org.bukkit.Particle.VILLAGER_ANGRY, loc, 10)
            }

            sign.update()
        })
    }

    suspend fun BuyItem(shop: SignShop, player: Player) {
        val token = Wings.instance.config.config.token ?: return
        val endpoint = Wings.instance.config.config.wingsApiEndpoint ?: return

        val response = Wings.instance.http.post("$endpoint/buySSItem") {
            header("Authorization", token)
            contentType(ContentType.Application.Json)
            setBody(BuySSItemPayload(shop.location.strSerialize(), player.uniqueId.toString()))
        }

        val scheduler = Wings.instance.server.scheduler

        if (response.status.isSuccess()) {
            GetSignShop(shop.location) // Update the sign
            scheduler.runTask(Wings.instance, Runnable {
                player.sendMessage(ChatRes.success("Sent your bought item into transit, use /deliver to receive it!"))
            })
        } else {
            GetSignShop(shop.location) // Update the sign
            HandleGatewayError(response, player)
        }
    }
}