package com.tristansmp.wings.lib

import com.tristansmp.wings.Wings
import org.bukkit.Location

fun Location.strSerialize(): String {
    return "${x},${y},${z},${world.name}"
}

fun LocationStrDeserialize(str: String): Location {
    val split = str.split(",")
    return Location(
        Wings.instance.server.getWorld(split[3]),
        split[0].toDouble(),
        split[1].toDouble(),
        split[2].toDouble()
    )
}