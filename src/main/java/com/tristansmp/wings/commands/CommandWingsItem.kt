package com.tristansmp.wings.commands

import com.tristansmp.wings.Wings
import com.tristansmp.wings.lib.sendError
import com.tristansmp.wings.lib.sendSuccessIndicator
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

class CommandWingsItem : TabExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (sender !is Player) {
            return false
        }

        if (args == null || args.size != 1) {
            return false
        }

        val wingsItemName = args[0]

        val wingsItem = Wings.instance.itemManager.getWingsItem(wingsItemName)

        if (wingsItem == null) {
            sender.sendError("Wings item '$wingsItemName' does not exist.")
            return true
        }

        val itemStack = wingsItem.createItemStack()

        sender.inventory.addItem(itemStack)

        sender.sendSuccessIndicator("1 x ${wingsItem.name} obtained")

        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>?
    ): MutableList<String> {
        if (args == null || args.size != 1) {
            return mutableListOf()
        }

        val wingsItemNames = Wings.instance.itemManager.getWingsItems()

        return wingsItemNames.map { wingsItem ->
            wingsItem.id.key
        }.toMutableList()
    }
}