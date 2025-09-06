package com.example.routes

import com.example.models.Listing
import com.example.services.ListingService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.listingRoutes(listingService: ListingService) {
    route("/api/listings") {
        // Create listing
        post {
            try {
                val listing = call.receive<Listing>()
                val createdListing = listingService.createListing(listing)
                call.respond(HttpStatusCode.Created, createdListing)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }

        // Get all listings with pagination
        get {
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 0
            val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 10
            val listings = listingService.getAllListings(page, pageSize)
            call.respond(listings)
        }

        // Get single listing
        get("{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val listing = listingService.getListing(id)
            if (listing != null) {
                call.respond(listing)
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Listing not found"))
            }
        }

        // Update listing
        put("{id}") {
            val id = call.parameters["id"] ?: return@put call.respond(HttpStatusCode.BadRequest)
            try {
                val listing = call.receive<Listing>()
                val updatedListing = listingService.updateListing(id, listing)
                if (updatedListing != null) {
                    call.respond(updatedListing)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Listing not found"))
                }
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }

        // Delete listing
        delete("{id}") {
            val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
            if (listingService.deleteListing(id)) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Listing not found"))
            }
        }
    }
}
