package com.tristansmp.wings.item

import com.tristansmp.wings.Wings
import org.bukkit.ChatColor
import org.bukkit.NamespacedKey
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.persistence.PersistentDataType

open class WingsItem : Listener {
    val id: NamespacedKey
    val cmId: Int

    var recipe: ShapedRecipe? = null
    var name = "A Wings Item"

    private var baseItem: ItemStack? = null

    constructor(id: String, cmId: Int) {
        this.id = NamespacedKey(Wings.instance, id)
        this.cmId = cmId
    }

    protected fun setRecipe(cb: (ShapedRecipe) -> Unit) {
        val recipe = ShapedRecipe(this.id, this.createItemStack())
        cb(recipe)
        this.recipe = recipe
    }

    protected fun setBaseItem(item: ItemStack) {
        this.baseItem = item.clone()
    }


    fun createItemStack(): ItemStack {
        val item = this.baseItem ?: throw Error("Base item not set for ${this.id}")
        val meta = item.itemMeta

        meta.setCustomModelData(cmId)

        meta.setDisplayName("${ChatColor.RESET}${this.name}")

        meta.persistentDataContainer.set(
            this.id,
            PersistentDataType.STRING,
            "true"
        )

        item.itemMeta = meta

        return item
    }


    fun registerItem() {
        Wings.instance.server.addRecipe(this.recipe)
    }
}