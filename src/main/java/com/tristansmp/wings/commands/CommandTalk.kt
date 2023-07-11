package com.tristansmp.wings.commands

import com.tristansmp.wings.lib.sendError
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CommandTalk : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (sender !is Player) {
            sender.sendMessage("You must be a player to use this command!")
            return true
        }

        if (!sender.hasPermission("wings.talk")) {
            sender.sendError("You don't have permission to use this command!")
            return true
        }

        // we need a target, name, and message argument
        if (args == null || args.size < 4) {
            return false
        }

        val targetName = args[0]
        val name = args[1]
        val sound = args[2]
        val message = args.slice(3 until args.size).joinToString(" ")

        val target = sender.server.getPlayer(targetName)

        if (target == null) {
            sender.sendError("Player '$targetName' does not exist!")
            return true
        }

        target.playSound(target.location, sound, 1f, 1f)
        target.sendMessage("${ChatColor.GOLD}\u2604 ${ChatColor.LIGHT_PURPLE}$name${ChatColor.GRAY}: $message")

        return true
    }
}