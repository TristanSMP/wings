package com.tristansmp.wings.lib

import com.tristansmp.wings.Wings
import org.bukkit.Location

fun Location.strSerialize(): String {
    return "${x},${y},${z},${world.name}"
}

fun LocationStrDeserialize(str: String): Location {
    val split = str.split(",")
    val world = Wings.instance.server.getWorld(split[3]) ?: throw Exception("World '${split[3]}' does not exist.")
    return Location(
        world,
        split[0].toDouble(),
        split[1].toDouble(),
        split[2].toDouble()
    )
}