package com.tristansmp.wings

import com.tristansmp.wings.commands.CommandDeliver
import com.tristansmp.wings.commands.CommandDeposit
import com.tristansmp.wings.commands.CommandLink
import com.tristansmp.wings.commands.CommandPackage
import com.tristansmp.wings.events.ChatListener
import com.tristansmp.wings.lib.ConfigManager
import com.tristansmp.wings.lib.MemoryStore
import com.tristansmp.wings.plugins.configureHTTP
import com.tristansmp.wings.plugins.configureRouting
import com.tristansmp.wings.plugins.configureSerialization
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import net.luckperms.api.LuckPerms
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import io.ktor.client.*
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*

class Wings : JavaPlugin() {

    companion object {
        lateinit var instance: Wings
    }

    lateinit var config: ConfigManager
    lateinit var mstore: MemoryStore
    var lp: LuckPerms? = null
    val http = HttpClient(Java) {
        install(ContentNegotiation) {
            json()
        }
    }

    override fun onEnable() {
        Thread {
            embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
                .start(wait = true)
        }.start()

        instance = this

        config = ConfigManager()
        mstore = MemoryStore()

        val provider = Bukkit.getServicesManager().getRegistration(
            LuckPerms::class.java
        )

        if (provider != null) {
            lp = provider.provider
        }

        server.pluginManager.registerEvents(ChatListener(), this)

        this.getCommand("link")?.setExecutor(CommandLink())
        this.getCommand("deposit")?.setExecutor(CommandDeposit())
        this.getCommand("deliver")?.setExecutor(CommandDeliver())
        this.getCommand("package")?.setExecutor(CommandPackage())
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}

fun Application.module() {
    configureHTTP()
    configureSerialization()
    configureRouting()
}
