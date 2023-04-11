package com.tristansmp.wings.lib

import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

fun Player.completelyDebuff() {
    this.addPotionEffect(
        PotionEffect(
            PotionEffectType.BLINDNESS,
            999999,
            255,
            false,
            false,
            false
        )
    )

    this.addPotionEffect(
        PotionEffect(
            PotionEffectType.JUMP,
            999999,
            255,
            false,
            false,
            false
        )
    )

    this.addPotionEffect(
        PotionEffect(
            PotionEffectType.SLOW,
            999999,
            255,
            false,
            false,
            false
        )
    )

    this.addPotionEffect(
        PotionEffect(
            PotionEffectType.SLOW_DIGGING,
            999999,
            255,
            false,
            false,
            false
        )
    )

    this.addPotionEffect(
        PotionEffect(
            PotionEffectType.WEAKNESS,
            999999,
            255,
            false,
            false,
            false
        )
    )
}