package com.tristansmp.wings.item

import com.tristansmp.wings.Wings
import com.tristansmp.wings.lib.LocationStrDeserialize
import com.tristansmp.wings.lib.sendActionBarS
import net.kyori.adventure.text.Component
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class UnknownSubstance : WingsItem {
    constructor() : super("unknown_substance", 7000000) {
        val item = ItemStack(Material.MILK_BUCKET)

        this.name = "${ChatColor.LIGHT_PURPLE}Unknown Substance"

        val lore: MutableList<Component> = mutableListOf()

        lore.add(
            Component.text("${ChatColor.RESET}${ChatColor.GRAY}takes you places...")
        )

        lore.add(
            Component.text("${ChatColor.RESET}${ChatColor.GRAY}${ChatColor.ITALIC}but where?")
        )

        lore.add(
            Component.text("${ChatColor.RESET}${ChatColor.RED}one-time use")
        )

        val meta = item.itemMeta
        meta.lore(lore)
        item.itemMeta = meta

        this.setBaseItem(item)
        this.setRecipe { recipe ->
            recipe.shape("NSN", "AMA", "NEN")

            recipe.setIngredient('N', Material.NETHERITE_BLOCK)
            recipe.setIngredient('S', Material.NETHER_STAR)
            recipe.setIngredient('A', Material.ENCHANTED_GOLDEN_APPLE)
            recipe.setIngredient('M', Material.MILK_BUCKET)
            recipe.setIngredient('E', Material.ENDER_EYE)
        }
    }

    @EventHandler
    fun onMilkDrink(event: PlayerItemConsumeEvent) {
        if (!event.item?.itemMeta?.persistentDataContainer?.has(this.id)!!) return;

        event.player.sendActionBarS("${ChatColor.GRAY}You feel a strange sensation...")

        event.player.addPotionEffect(
            PotionEffect(
                PotionEffectType.CONFUSION,
                20 * 5,
                1,
                false,
                false,
                false
            )
        )

        val scheduler = Wings.instance.server.scheduler

        scheduler.runTaskLater(Wings.instance, Runnable {
            try {
                val theAbyss = LocationStrDeserialize("865,68,-421,the_abyss")
                event.player.teleport(theAbyss)
            } catch (e: Exception) {
                val theAbyssDev = LocationStrDeserialize("865,68,-421,world")
                event.player.teleport(theAbyssDev)
            }
        }, 20 * 5)
    }

}

