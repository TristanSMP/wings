package com.tristansmp.wings.events

import com.tristansmp.wings.Wings
import com.tristansmp.wings.commands.PackagePayload
import com.tristansmp.wings.lib.HandleGatewayError
import com.tristansmp.wings.lib.sendInfo
import com.tristansmp.wings.lib.sendSuccess
import com.tristansmp.wings.lib.toJsonObject
import com.tristansmp.wings.routes.toJson
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.*
import io.papermc.paper.event.player.AsyncChatEvent
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import net.kyori.adventure.text.TextComponent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerEditBookEvent

@Serializable
data class BlogBookPayload(val uuid: String, val pages: List<String>, val title: String)

class BlogBook : Listener {
    @EventHandler()
    fun onBookSign(event: PlayerEditBookEvent) {
        if (!event.isSigning) return;
        if (!event.newBookMeta.persistentDataContainer.has(Wings.instance.namespace.BlogBookTag)) return;
        val player = event.player
        val uuid = player.uniqueId.toString()
        val token = Wings.instance.config.config.token ?: return
        val endpoint = Wings.instance.config.config.wingsApiEndpoint ?: return

        event.isCancelled = true;
        player.inventory.setItemInMainHand(null)

        val pages = event.newBookMeta.pages().map { it as TextComponent }.map { it.content() }
        val title = event.newBookMeta.title() as TextComponent
        val titleString = title.content()

        Wings.instance.logger.info("took a book from ${player.name} with title $titleString and pages $pages")
        event.player.sendInfo("Submitting \"$titleString\"...")

        val scheduler = Wings.instance.server.scheduler

        scheduler.runTaskAsynchronously(Wings.instance, Runnable {
            runBlocking {
                val response = Wings.instance.http.post("$endpoint/blogBookCollect") {
                    header("Authorization", token)
                    contentType(ContentType.Application.Json)
                    setBody(BlogBookPayload(uuid, pages, titleString))
                }

                if (!response.status.isSuccess()) {
                    HandleGatewayError(response, player)
                } else {
                    player.sendSuccess("Successfully submitted! Your blog post will be edited and considered for publication.")
                }
            }
        })
    }
}
