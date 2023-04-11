package com.tristansmp.wings.item

import com.tristansmp.wings.Wings
import net.kyori.adventure.text.Component
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

class CoffeeShopDisc : WingsItem {

    private val JUKEBOXES_PLAYING_COFFEE_SHOP = mutableSetOf<org.bukkit.block.Jukebox>()

    constructor() : super("music_disc_coffee_shop", 7733267) {
        val item = ItemStack(Material.WARPED_FUNGUS_ON_A_STICK)

        this.name = "${ChatColor.AQUA}Music Disc"

        val lore: MutableList<Component> = mutableListOf()

        lore.add(
            Component.text("${ChatColor.RESET}${ChatColor.GRAY}Tristan Camejo - Coffee Shop")
        )

        val meta = item.itemMeta

        meta.lore(lore)

        item.itemMeta = meta

        this.setBaseItem(item)
    }

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        if (!event.item?.itemMeta?.persistentDataContainer?.has(this.id)!!) return;

        if (event.action != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return;
        if (event.clickedBlock?.type != Material.JUKEBOX) return;

        val block = event.clickedBlock!!

        if (JUKEBOXES_PLAYING_COFFEE_SHOP.contains(block.state as org.bukkit.block.Jukebox)) {
            event.isCancelled = true
            return
        }

        if (block.state.type != Material.JUKEBOX) return;
        val jukebox = block.state as org.bukkit.block.Jukebox
        if (jukebox.record != null) {
            jukebox.eject()
            jukebox.update()
        }

        JUKEBOXES_PLAYING_COFFEE_SHOP.add(jukebox)

        block.world.playSound(block.location, "tsmp:music.coffee_shop", 1f, 1f)

        val scheduler = Wings.instance.server.scheduler

        scheduler.runTaskLater(Wings.instance, Runnable {
            if (!JUKEBOXES_PLAYING_COFFEE_SHOP.contains(jukebox)) return@Runnable
            JUKEBOXES_PLAYING_COFFEE_SHOP.remove(jukebox)
            block.world.dropItemNaturally(block.location.add(0.5, 0.5, 0.5), this.createItemStack())
        }, 113 * 20)

        event.item?.amount = event.item?.amount?.minus(1) ?: 0

        event.isCancelled = true
    }

    @EventHandler
    fun onJukeboxInteract(event: PlayerInteractEvent) {
        if (event.action != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return;
        if (event.clickedBlock?.type != Material.JUKEBOX) return;

        val block = event.clickedBlock!!

        if (block.state.type != Material.JUKEBOX) return;
        val jukebox = block.state as org.bukkit.block.Jukebox
        if (jukebox.record == null) return;

        if (JUKEBOXES_PLAYING_COFFEE_SHOP.contains(jukebox)) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onJukeboxStop(event: org.bukkit.event.block.BlockBreakEvent) {
        if (event.block.type != Material.JUKEBOX) return;
        val jukebox = event.block.state as org.bukkit.block.Jukebox
        if (jukebox.record == null) return;

        if (JUKEBOXES_PLAYING_COFFEE_SHOP.contains(jukebox)) {
            JUKEBOXES_PLAYING_COFFEE_SHOP.remove(jukebox)
            event.block.world.dropItemNaturally(event.block.location.add(0.5, 0.5, 0.5), this.createItemStack())
            event.block.location.getNearbyEntities(10.0, 10.0, 10.0).forEach {
                if (it is org.bukkit.entity.Player) {
                    it.stopSound("tsmp:music.coffee_shop")
                }
            }
        }
    }

}

