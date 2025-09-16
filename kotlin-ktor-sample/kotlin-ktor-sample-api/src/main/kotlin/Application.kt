package com.example

import com.example.database.DatabaseConfig
import com.example.repositories.IListingRepository
import com.example.repositories.ListingRepository
import com.example.routes.listingRoutes
import com.example.services.ListingService
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    // Configure CORS
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Accept)
        anyHost() // WARNING: This allows any host - only use in development
        allowCredentials = true
        maxAgeInSeconds = 3600
    }

    // Configure JSON serialization
    install(ContentNegotiation) {
        jackson {
            // Enable pretty printing for development
            enable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT)
            // Prevent failure on empty beans
            disable(com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS)
        }
    }

    // Initialize database and services
    val dataSource = DatabaseConfig.createDataSource()
    val listingRepository: IListingRepository = ListingRepository(dataSource)
    val listingService = ListingService(listingRepository)

    // Configure OpenAPI and Swagger
    routing {
        // Temporarily disabled due to schema compatibility issues
        // openAPI(path = "openapi", swaggerFile = "openapi/documentation.yaml")
        swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")
    }

    // Configure routing
    routing {
        listingRoutes(listingService)
    }
}
