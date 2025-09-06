package com.example

import com.example.routes.listingRoutes
import com.example.services.ListingService
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    // Configure JSON serialization
    install(ContentNegotiation) {
        jackson()
    }

    // Initialize services
    val listingService = ListingService()

    // Configure routing
    routing {
        listingRoutes(listingService)
    }
}
