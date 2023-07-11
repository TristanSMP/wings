package com.tristansmp.wings.commands

import com.tristansmp.wings.lib.SerializeUtils
import com.tristansmp.wings.lib.sendError
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.TextColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CommandItemB64 : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (sender !is Player) {
            sender.sendMessage("You must be a player to use this command!")
            return true
        }

        if (!sender.hasPermission("wings.itemb64")) {
            sender.sendError("You don't have permission to use this command!")
            return true
        }

        val heldItem = sender.inventory.itemInMainHand


        if (heldItem == null) {
            sender.sendError("You must be holding an item to use this command!")
            return true
        }

        val b64Item = SerializeUtils.itemStackToBase64(heldItem)

        sender.sendMessage(
            Component.text("Click to copy to clipboard").color(TextColor.fromHexString("#00FF00")).clickEvent(
                ClickEvent.copyToClipboard(b64Item)
            )
        )

        return true
    }
}