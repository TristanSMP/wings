package com.tristansmp.wings

import com.tristansmp.wings.InPersonPOS.InPersonPOSManager
import com.tristansmp.wings.commands.*
import com.tristansmp.wings.events.BlogBook
import com.tristansmp.wings.events.ChatListener
import com.tristansmp.wings.events.InPersonPOS
import com.tristansmp.wings.events.RecipeHandler
import com.tristansmp.wings.lib.CommandRatelimiter
import com.tristansmp.wings.lib.ConfigManager
import com.tristansmp.wings.lib.MemoryStore
import com.tristansmp.wings.messaging.WingsAPI
import com.tristansmp.wings.plugins.configureHTTP
import com.tristansmp.wings.plugins.configureRouting
import com.tristansmp.wings.plugins.configureSerialization
import io.ktor.client.*
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.luckperms.api.LuckPerms
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin


class Wings : JavaPlugin() {

    companion object {
        lateinit var instance: Wings
        val WINGS_API_CHANNEL = "wings:api"
    }

    val registeredRecipes = mutableListOf<NamespacedKey>()


    lateinit var config: ConfigManager
    lateinit var mstore: MemoryStore
    lateinit var inPersonPOSManager: InPersonPOSManager
    lateinit var namespace: Namespace
    lateinit var commandRatelimiter: CommandRatelimiter

    var lp: LuckPerms? = null
    val http = HttpClient(Java) {
        install(ContentNegotiation) {
            json()
        }
    }

    override fun onEnable() {
        // API
        Thread {
            embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
                .start(wait = true)
        }.start()

        // Singleton
        instance = this

        // Managers
        config = ConfigManager()
        mstore = MemoryStore()
        inPersonPOSManager = InPersonPOSManager()
        namespace = Namespace(this)
        commandRatelimiter = CommandRatelimiter(this)

        // Luckperms Hook
        val provider = Bukkit.getServicesManager().getRegistration(
            LuckPerms::class.java
        )

        if (provider != null) {
            lp = provider.provider
        }

        // Register event listeners
        server.pluginManager.registerEvents(ChatListener(), this)
        server.pluginManager.registerEvents(InPersonPOS(), this)
        server.pluginManager.registerEvents(BlogBook(), this)
        server.pluginManager.registerEvents(RecipeHandler(), this)

        // Link commands
        this.getCommand("link")?.setExecutor(CommandLink())
        this.getCommand("deposit")?.setExecutor(CommandDeposit())
        this.getCommand("deliver")?.setExecutor(CommandDeliver())
        this.getCommand("package")?.setExecutor(CommandPackage())
        this.getCommand("create-sign-shop")?.setExecutor(CommandCreateSignShop())
        this.getCommand("ott")?.setExecutor(CommandOTT())

        // Blog book recipe register
        val item = ItemStack(Material.WRITABLE_BOOK)

        val meta = item.itemMeta

        val lore: MutableList<Component> = if (meta.hasLore()) meta.lore()!! else mutableListOf()

        lore.add(
            LegacyComponentSerializer.legacyAmpersand()
                .deserialize("&f&r&bWireless Antenna &7(connected to tsmp blog)")
        )

        meta.lore(lore)

        meta.persistentDataContainer.set(
            namespace.BlogBookTag,
            PersistentDataType.STRING,
            "true"
        )

        item.itemMeta = meta

        val key = NamespacedKey(this, "blog_book")
        val recipe = ShapedRecipe(key, item)

        recipe.shape("DB")
        recipe.setIngredient('D', Material.DIAMOND)
        recipe.setIngredient('B', Material.WRITABLE_BOOK)

        server.addRecipe(recipe)
        registeredRecipes.add(key)

        // Plugin messages
        this.server.messenger.registerOutgoingPluginChannel(this, WINGS_API_CHANNEL);
        this.server.messenger.registerIncomingPluginChannel(this, WINGS_API_CHANNEL, WingsAPI());
    }


    override fun onDisable() {
        // Plugin shutdown logic
        this.server.messenger.unregisterOutgoingPluginChannel(this);
        this.server.messenger.unregisterIncomingPluginChannel(this);
    }
}

fun Application.module() {
    configureHTTP()
    configureSerialization()
    configureRouting()
}
