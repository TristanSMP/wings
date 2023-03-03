package com.tristansmp.wings.events

import com.tristansmp.wings.Wings
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.TextComponent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

class ChatListener : Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerChat(event: AsyncChatEvent) {
        try {
            val message = event.message() as TextComponent


            if (!message.content().startsWith("~")) {
                return
            }

            val content = message.content().substring(1)

            val needsCollection = Wings.instance.mstore.get<Boolean>("cc:${event.player.uniqueId}:needs_collection")

            if (needsCollection == null || !needsCollection) {
                return
            }

            event.isCancelled = true

            Wings.instance.mstore.set("cc:${event.player.uniqueId}:results", content)

            event.player.sendMessage("§d[Wings] §aMessage collected.")

            Wings.instance.mstore.remove("cc:${event.player.uniqueId}:needs_collection")
        } catch (e: ClassCastException) {
            event.player.sendMessage("§d[Wings] §cMessage could not be collected.")
        }
    }
}
