package com.tristansmp.wings.item

import com.tristansmp.wings.Wings
import com.tristansmp.wings.lib.HandleGatewayError
import com.tristansmp.wings.lib.sendInfo
import com.tristansmp.wings.lib.sendSuccess
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerEditBookEvent
import org.bukkit.inventory.ItemStack

@Serializable
data class BlogBookPayload(val uuid: String, val pages: List<String>, val title: String)

class BlogBook : WingsItem {

    constructor() : super("blog_book") {
        val item = ItemStack(Material.WRITABLE_BOOK)

        this.name = "Blog Book"

        val lore: MutableList<Component> = if (item.itemMeta.hasLore()) item.itemMeta.lore()!! else mutableListOf()

        lore.add(
            LegacyComponentSerializer.legacyAmpersand()
                .deserialize("&f&r&bWireless Antenna &7(connected to tsmp blog)")
        )

        item.itemMeta.lore(lore)

        this.setBaseItem(item)
        this.setRecipe { recipe ->
            recipe.shape("DB")
            recipe.setIngredient('D', Material.DIAMOND)
            recipe.setIngredient('B', Material.WRITABLE_BOOK)
        }
    }

    @EventHandler()
    fun onBookSign(event: PlayerEditBookEvent) {
        if (!event.newBookMeta.persistentDataContainer.has(this.id)) return;

        if (!event.isSigning) return;
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

