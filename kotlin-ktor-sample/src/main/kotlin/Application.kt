package com.example

import com.example.database.DatabaseConfig
import com.example.repositories.IListingRepository
import com.example.repositories.ListingRepository
import com.example.routes.listingRoutes
import com.example.services.ListingService
import com.example.services.ISortingService
import com.example.services.SortingService
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.sql.SQLException

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    // Configure error handling
    install(StatusPages) {
        exception<IllegalArgumentException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to cause.message))
        }
        exception<SQLException> { call, cause ->
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Database error occurred"))
        }
        exception<Throwable> { call, cause ->
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "An unexpected error occurred"))
        }
    }

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
            registerModule(KotlinModule.Builder().build())
            // Enable pretty printing for development
            enable(SerializationFeature.INDENT_OUTPUT)
            // Prevent failure on empty beans
            disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
        }
    }

    // Initialize database and services
    val dataSource = DatabaseConfig.createDataSource()
    val listingRepository: IListingRepository = ListingRepository(dataSource)
    val sortingService: ISortingService = SortingService()
    val listingService = ListingService(listingRepository, sortingService)

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
