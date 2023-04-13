package com.tristansmp.wings.item

import com.tristansmp.wings.Wings
import org.bukkit.NamespacedKey

class WingsItemManager {
    private val wingsItems = mutableMapOf<NamespacedKey, WingsItem>()

    constructor() {
        registerWingsItem(BlogBook())
        registerWingsItem(CoffeeShopDisc())
        registerWingsItem(UnknownSubstance())
    }

    private fun registerWingsItem(wingsItem: WingsItem) {
        if (wingsItems.containsKey(wingsItem.id)) {
            throw IllegalArgumentException("WingsItem with namespaced id ${wingsItem.id} already exists.")
        }

        Wings.instance.server.pluginManager.registerEvents(wingsItem, Wings.instance)
        wingsItem.registerItem()

        wingsItems[wingsItem.id] = wingsItem
    }

    fun getWingsItem(id: NamespacedKey): WingsItem? {
        return wingsItems[id]
    }

    fun getWingsItem(id: String): WingsItem? {
        return getWingsItem(NamespacedKey(Wings.instance, id))
    }

    fun getWingsItems(): List<WingsItem> {
        return wingsItems.values.toList()
    }

}