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
            // @OpenAPI(
            //   summary = "Create a new listing",
            //   responses = [
            //     OpenApiResponse(status = "201", description = "Listing created successfully"),
            //     OpenApiResponse(status = "400", description = "Invalid listing data")
            //   ]
            // )
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
            // @OpenAPI(
            //   summary = "Get all listings with pagination",
            //   responses = [
            //     OpenApiResponse(status = "200", description = "Successful response with listings"),
            //     OpenApiResponse(status = "400", description = "Invalid page or pageSize parameters")
            //   ]
            // )
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 0
            val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 10
            val listings = listingService.getAllListings(page, pageSize)
            call.respond(listings)
        }

        // Get single listing
        get("{id}") {
            // @OpenAPI(
            //   summary = "Get listing by ID",
            //   responses = [
            //     OpenApiResponse(status = "200", description = "Successful response with listing details"),
            //     OpenApiResponse(status = "404", description = "Listing not found")
            //   ]
            // )
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
            // @OpenAPI(
            //   summary = "Update listing by ID",
            //   responses = [
            //     OpenApiResponse(status = "204", description = "Listing updated successfully"),
            //     OpenApiResponse(status = "400", description = "Invalid data or ID mismatch"),
            //     OpenApiResponse(status = "404", description = "Listing not found")
            //   ]
            // )
            val id = call.parameters["id"] ?: return@put call.respond(HttpStatusCode.BadRequest)
            try {
                val listing = call.receive<Listing>()
                val updatedListing = listingService.updateListing(id, listing)
                if (updatedListing != null) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Listing not found"))
                }
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }

        // Delete listing
        delete("{id}") {
            // @OpenAPI(
            //   summary = "Delete listing by ID",
            //   responses = [
            //     OpenApiResponse(status = "204", description = "Listing deleted successfully"),
            //     OpenApiResponse(status = "404", description = "Listing not found")
            //   ]
            // )
            val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
            if (listingService.deleteListing(id)) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Listing not found"))
            }
        }
    }
}
