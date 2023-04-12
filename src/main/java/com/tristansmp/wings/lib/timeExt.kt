package com.tristansmp.wings.lib

import org.bukkit.World

fun World.get12HourTime(): String {
    val time = this.time
    val hour = (time / 1000 + 6) % 24
    val minute = (time % 1000) / 1000 * 60
    val ampm = if (hour < 12) "am" else "pm"
    val hour12 = if (hour == 0L) 12 else if (hour > 12) hour - 12 else hour
    return "$hour12:${minute.toString().padStart(2, '0')}$ampm"
}