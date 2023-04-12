package com.tristansmp.wings.lib

import com.earth2me.essentials.utils.DescParseTickFormat
import org.bukkit.World

fun World.get12HourTime(): String {
    val fmt = DescParseTickFormat.format12(this.time)
    return fmt.substring(0, fmt.length - 2) + fmt.substring(fmt.length - 2).lowercase()
}