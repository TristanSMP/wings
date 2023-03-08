package com.tristansmp.wings.routes

import com.tristansmp.wings.Wings
import com.tristansmp.wings.lib.LocationStrDeserialize
import com.tristansmp.wings.lib.strSerialize
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

data class SignShopRefreshPOST(
    val location: String,
)

fun Route.SignShop() {
    route("/signshop") {

        post("/refresh") {
            val req = call.receive<SignShopRefreshPOST>()
            val location = LocationStrDeserialize(req.location)
            Wings.instance.logger.info("Refreshing signshop at ${location.strSerialize()}")
            val newSign = Wings.instance.inPersonPOSManager.GetSignShop(location)

            if (newSign == null) {
                call.response.status(HttpStatusCode.BadRequest)
                call.respond(mapOf("status" to "error", "message" to "SignShop not found"))
                return@post
            }

            call.respond(mapOf("status" to "ok"))
        }
    }
}