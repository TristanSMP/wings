package com.tristansmp.wings.commands

import com.earth2me.essentials.Trade
import com.earth2me.essentials.api.IAsyncTeleport
import com.tristansmp.wings.Wings
import com.tristansmp.wings.lib.LocationStrDeserialize
import com.tristansmp.wings.lib.SerializeUtils
import com.tristansmp.wings.lib.sendError
import com.tristansmp.wings.lib.sendSuccess
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import net.ess3.api.IEssentials
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause
import redempt.redlib.inventorygui.InventoryGUI
import redempt.redlib.inventorygui.ItemButton
import redempt.redlib.itemutils.ItemBuilder
import java.util.concurrent.CompletableFuture


@Serializable
data class SpawnLocation(
    val seriLoc: String,
    val name: String,
    val b64ItemIcon: String
)

@Serializable
data class GETSpawnLocations(
    val spawns: List<SpawnLocation>
)


class CommandSpawn : CommandExecutor {
    private val CachedSpawns = mutableListOf<SpawnLocation>()

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (sender !is Player) {
            sender.sendMessage("You must be a player to use this command!")
            return true
        }

        if (!Wings.instance.commandRatelimiter.canRunCommand(sender)) {
            sender.sendError("You are sending commands too fast! Please wait a few seconds before trying again.")
            return true
        }

        if (!sender.hasPermission("wings.spawn")) {
            sender.sendError("You don't have permission to use this command!")
            return true
        }

        val token = Wings.instance.config.config.token ?: return false
        val endpoint = Wings.instance.config.config.wingsApiEndpoint ?: return false

        val scheduler = Wings.instance.server.scheduler


        scheduler.runTaskAsynchronously(Wings.instance, Runnable {
            runBlocking {
                val response = Wings.instance.http.post("$endpoint/getSpawnLocations") {
                    header("Authorization", token)
                    contentType(ContentType.Application.Json)
                }

                if (CachedSpawns.isNotEmpty()) {
                    renderSpawnSelector(sender, CachedSpawns)
                }

                if (response.status.isSuccess()) {
                    try {
                        val data = response.body<GETSpawnLocations>()

                        CachedSpawns.clear()
                        CachedSpawns.addAll(data.spawns)

                        if (CachedSpawns.isEmpty()) {
                            renderSpawnSelector(sender, data.spawns)
                        }
                    } catch (e: Exception) {
                        sender.sendError("Malformed response from server! Please try again later.")
                        e.printStackTrace()
                    }
                } else {
                    sender.sendError("Failed to discover new spawn locations. Please inform staff.")
                }
            }
        })

        return true
    }

    private fun renderSpawnSelector(sender: Player, spawns: List<SpawnLocation>) {
        val scheduler = Wings.instance.server.scheduler

        scheduler.runTask(Wings.instance, Runnable {
            val size = 27
            val gui =
                InventoryGUI(Bukkit.createInventory(null, size, Component.text("Spawn Selection")))

            spawns.forEachIndexed { index, spawn ->
                val button = ItemButton.create(
                    ItemBuilder(SerializeUtils.itemStackFromBase64(spawn.b64ItemIcon))
                        .setName("${ChatColor.RESET}${spawn.name}")
                ) { e: InventoryClickEvent ->
                    sender.closeInventory()

                    val ess = Wings.instance.server.pluginManager.getPlugin("Essentials") as IEssentials

                    val loc = LocationStrDeserialize(spawn.seriLoc)
                    val asyncTeleporter = ess.getUser(sender).asyncTeleport as IAsyncTeleport
                    val future: CompletableFuture<Boolean> = CompletableFuture()

                    future.thenAccept { success: Boolean ->
                        if (success) {
                            sender.sendSuccess("Warp successful!")
                        }
                    }

                    asyncTeleporter.teleport(loc, Trade("spawn", ess), TeleportCause.COMMAND, future)
                }

                gui.addButton(button, index + 9)
            }

            gui.open(sender)
            gui.destroysOnClose()
        })
    }
}