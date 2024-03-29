package com.tristansmp.wings.lib

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.entity.Player

object ChatRes {
    fun success(message: String): Component {
        return LegacyComponentSerializer.legacyAmpersand().deserialize("&d[TSMP] &a$message")
    }

    fun error(message: String): Component {
        return LegacyComponentSerializer.legacyAmpersand().deserialize("&d[TSMP] &c$message")
    }

    fun info(message: String): Component {
        return LegacyComponentSerializer.legacyAmpersand().deserialize("&d[TSMP] &b$message")
    }
}

fun Player.sendSuccess(message: String) {
    this.sendMessage(ChatRes.success(message))
}

fun Player.sendError(message: String) {
    this.sendMessage(ChatRes.error(message))
}

fun Player.sendInfo(message: String) {
    this.sendMessage(ChatRes.info(message))
}

fun Player.sendSuccessIndicator(message: String) {
    this.sendActionBar(ChatRes.success(message))
}

fun Player.sendErrorIndicator(message: String) {
    this.sendActionBar(ChatRes.error(message))
}

fun Player.sendInfoIndicator(message: String) {
    this.sendActionBar(ChatRes.info(message))
}

fun Player.sendActionBarS(message: String) {
    this.sendActionBar(Component.text(message))
}