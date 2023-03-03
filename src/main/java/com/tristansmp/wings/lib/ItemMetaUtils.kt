package com.tristansmp.wings.lib

import org.bukkit.inventory.meta.ItemMeta

fun ItemMeta.getName(): String? {
    val custom = getDisplayName()

    if (custom != "") {
        return custom
    }

    return null
}