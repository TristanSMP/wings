package com.tristansmp.wings.InPersonPOS

import com.tristansmp.wings.Wings
import com.tristansmp.wings.lib.SerializeUtils
import kotlinx.serialization.Serializable
import org.bukkit.Location
import org.bukkit.inventory.ItemStack
import java.util.UUID

@Serializable
data class SignShopPayload(
    val id: String,
    val location: String,
    val owner: String,
    val item: String,
    val price: Int?,
    val stock: Int
)

class SignShop {
    val id: String

    val location: Location

    val owner: UUID

    val item: ItemStack

    val lastKnownPrice: Int?
    val stock: Int

    constructor(payload: SignShopPayload) {
        this.id = payload.id
        val location = payload.location.split(",")
        this.location = Location(
            Wings.instance.server.getWorld(location[3]),
            location[0].toDouble(),
            location[1].toDouble(),
            location[2].toDouble()
        )
        this.owner = UUID.fromString(payload.owner)
        this.item = SerializeUtils.itemStackFromBase64(payload.item)
        this.lastKnownPrice = payload.price
        this.stock = payload.stock
    }

}