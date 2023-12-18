package com.example

import com.example.plugins.configureRouting
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun main(args: Array<String>) {
    io.ktor.server.cio.EngineMain.main(args)
}

fun Application.module() {
    install(ContentNegotiation) {
        jackson()
    }
    install(StatusPages) {
        exception<Exception> { call, cause ->
            call.respond(HttpStatusCode.InternalServerError, cause.message ?: "Server Error")
        }
    }
    configureRouting()
}
