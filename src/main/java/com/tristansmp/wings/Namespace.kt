package com.tristansmp.wings

import org.bukkit.NamespacedKey

class Namespace {
    val BlogBookTag: NamespacedKey

    constructor(instance: Wings) {
        BlogBookTag = NamespacedKey(instance, "blog-book")
    }
}