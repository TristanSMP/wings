package com.tristansmp.wings

import com.tristansmp.wings.InPersonPOS.InPersonPOSManager
import com.tristansmp.wings.commands.*
import com.tristansmp.wings.events.ChatListener
import com.tristansmp.wings.events.InPersonPOS
import com.tristansmp.wings.events.RecipeHandler
import com.tristansmp.wings.events.ResourcesListener
import com.tristansmp.wings.item.WingsItemManager
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
import net.luckperms.api.LuckPerms
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin


class Wings : JavaPlugin() {

    companion object {
        lateinit var instance: Wings
        val WINGS_API_CHANNEL = "wings:api"
    }

    lateinit var config: ConfigManager
    lateinit var mstore: MemoryStore
    lateinit var inPersonPOSManager: InPersonPOSManager
    lateinit var commandRatelimiter: CommandRatelimiter
    lateinit var itemManager: WingsItemManager

    var lp: LuckPerms? = null

    val http = HttpClient(Java) {
        install(ContentNegotiation) {
            json()
        }
    }
    private var engine: NettyApplicationEngine? = null

    override fun onEnable() {
        // API
        Thread {
            engine = embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
                .start(wait = true)
        }.start()

        // Singleton
        instance = this

        // Managers
        config = ConfigManager()
        mstore = MemoryStore()
        inPersonPOSManager = InPersonPOSManager()
        commandRatelimiter = CommandRatelimiter(this)
        itemManager = WingsItemManager()

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
        server.pluginManager.registerEvents(RecipeHandler(), this)
        server.pluginManager.registerEvents(ResourcesListener(), this)

        // Link commands
        this.getCommand("link")?.setExecutor(CommandLink())
        this.getCommand("deposit")?.setExecutor(CommandDeposit())
        this.getCommand("deliver")?.setExecutor(CommandDeliver())
        this.getCommand("package")?.setExecutor(CommandPackage())
        this.getCommand("create-sign-shop")?.setExecutor(CommandCreateSignShop())
        this.getCommand("ott")?.setExecutor(CommandOTT())
        this.getCommand("wi")?.setExecutor(CommandWingsItem())
        this.getCommand("spawn")?.setExecutor(CommandSpawn())
        this.getCommand("itemb64")?.setExecutor(CommandItemB64())
        this.getCommand("talk")?.setExecutor(CommandTalk())

        // Plugin messages
        this.server.messenger.registerOutgoingPluginChannel(this, WINGS_API_CHANNEL);
        this.server.messenger.registerIncomingPluginChannel(this, WINGS_API_CHANNEL, WingsAPI());
    }


    override fun onDisable() {
        engine?.stop(0, 0)
        http.close()

        this.server.messenger.unregisterOutgoingPluginChannel(this);
        this.server.messenger.unregisterIncomingPluginChannel(this);
    }
}

fun Application.module() {
    configureHTTP()
    configureSerialization()
    configureRouting()
}
