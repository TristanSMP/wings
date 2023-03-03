package com.tristansmp.wings.lib

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor

object ChatRes {
    fun error(message: String?): Component {
        return Component.text(message!!).color(TextColor.fromHexString("#FFCCCC"))
    }

    fun success(message: String?): Component {
        return Component.text(message!!).color(TextColor.fromHexString("#60ff42"))
    }
}