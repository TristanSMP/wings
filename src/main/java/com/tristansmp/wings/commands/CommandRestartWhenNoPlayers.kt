package com.tristansmp.wings.commands

import com.tristansmp.wings.Wings
import kotlinx.serialization.Serializable
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

@Serializable

class CommandRestartWhenNoPlayers : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (!sender.hasPermission("wings.restart-when-no-players")) {
            sender.sendMessage("You don't have permission to use this command!")
            return true
        }

        if (args == null) {
            sender.sendMessage("You must specify a value! (on/off)")
            return true
        }

        if (args.size != 1) {
            sender.sendMessage("You must specify a value! (on/off)")
            return true
        }

        val value = args[0]

        if (value != "on" && value != "off") {
            sender.sendMessage("You must specify a valid value! (on/off)")
            return true
        }

        Wings.instance.restartManager.setRestartWhenNoPlayers(value == "on")

        sender.sendMessage("Restart when no players set to $value")

        return true
    }
}