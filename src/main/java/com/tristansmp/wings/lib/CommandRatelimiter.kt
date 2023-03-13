package com.tristansmp.wings.lib

import com.tristansmp.wings.Wings
import org.bukkit.entity.Player
import java.util.*

class CommandRatelimiter {
    private val lastCommandTimes = mutableMapOf<UUID, Long>()

    fun canRunCommand(player: Player): Boolean {
        val lastCommandTime = lastCommandTimes[player.uniqueId]
        val currentTime = System.currentTimeMillis()
        if (lastCommandTime == null || currentTime - lastCommandTime > 1000) {
            lastCommandTimes[player.uniqueId] = currentTime
            return true
        }
        return false
    }

    constructor(wings: Wings) {
        wings.server.scheduler.runTaskTimer(wings, Runnable {
            lastCommandTimes.clear()
        }, 0, 20)
    }
}