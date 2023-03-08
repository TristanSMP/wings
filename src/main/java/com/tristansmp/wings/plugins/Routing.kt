package com.tristansmp.wings.plugins

import com.tristansmp.wings.Wings
import com.tristansmp.wings.routes.Health
import com.tristansmp.wings.routes.LuckPerms
import com.tristansmp.wings.routes.Player
import com.tristansmp.wings.routes.SignShop
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.application.ApplicationCallPipeline.ApplicationPhase.Plugins
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }
    }

    routing {
        intercept(Plugins) {
            if (Wings.instance.config.config.token != null && Wings.instance.config.config.token != call.request.headers["Authorization"]) {
                call.respond(HttpStatusCode.Unauthorized)
                return@intercept finish()
            }

            return@intercept proceed()
        }

        get("/") {
            call.respondRedirect("https://github.com/tristansmp/wings")
        }

        Health()
        Player()
        LuckPerms()
        SignShop()
    }
}
