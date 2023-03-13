package com.tristansmp.wings.lib

import com.tristansmp.wings.Wings

class RestartManager {
    private var restartWhenNoPlayers = false

    fun setRestartWhenNoPlayers(restartWhenNoPlayers: Boolean) {
        this.restartWhenNoPlayers = restartWhenNoPlayers
    }

    constructor(wings: Wings) {
        wings.server.scheduler.runTaskTimer(wings, Runnable {
            if (restartWhenNoPlayers && wings.server.onlinePlayers.isEmpty()) {
                wings.server.scheduler.runTask(wings, Runnable {
                    wings.server.shutdown()
                })
            }
        }, 0, 20)
    }
}