package com.tristansmp.wings.events

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class ResourcesListener : Listener {
    @EventHandler()
    fun onPlayerJoin(event: PlayerJoinEvent) {
        event.player.setResourcePack(
            "https://re.tristansmp.com/resources.zip", null, true,
        )

    }
}
