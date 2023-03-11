package com.tristansmp.wings.events

import com.tristansmp.wings.Wings
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class RecipeHandler : Listener {
    @EventHandler()
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player

        Wings.instance.registeredRecipes.forEach { recipe ->
            player.discoverRecipe(recipe)
        }
    }
}