package com.example.routes

import com.example.models.Listing
import com.example.models.SearchRequest
import com.example.services.ListingService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

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
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to create listing"))
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
            try {
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 10
                val latitude = call.request.queryParameters["latitude"]?.toDoubleOrNull()
                val longitude = call.request.queryParameters["longitude"]?.toDoubleOrNull()
                
                if (pageSize > 50) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Page size cannot exceed 50 items"))
                    return@get
                }
                
                // Validate latitude and longitude if provided
                if (latitude != null && (latitude < -90 || latitude > 90)) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Latitude must be between -90 and 90 degrees"))
                    return@get
                }
                
                if (longitude != null && (longitude < -180 || longitude > 180)) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Longitude must be between -180 and 180 degrees"))
                    return@get
                }
                
                // Both or neither latitude and longitude must be provided
                if ((latitude != null && longitude == null) || (latitude == null && longitude != null)) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Both latitude and longitude must be provided together for proximity sorting"))
                    return@get
                }
                
                val listings = listingService.getAllListings(page, pageSize, latitude, longitude)
                call.respond(listings)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to retrieve listings"))
            }
        }

        // Search and filter listings
        post("search") {
            // @OpenAPI(
            //   summary = "Search and filter listings",
            //   responses = [
            //     OpenApiResponse(status = "200", description = "Successful response with filtered results"),
            //     OpenApiResponse(status = "400", description = "Invalid filter criteria or operators")
            //   ]
            // )
            try {
                val searchRequest = call.receive<SearchRequest>()
                                
                if (searchRequest.pageSize > 50) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Page size cannot exceed 50 items"))
                    return@post
                }
                
                val searchResponse = listingService.searchListings(searchRequest)
                call.respond(HttpStatusCode.OK, searchResponse)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to search listings"))
            }
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
            try {
                val idString = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                val id = UUID.fromString(idString)
                val listing = listingService.getListing(id)
                if (listing != null) {
                    call.respond(listing)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Listing not found"))
                }
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid UUID format"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to retrieve listing"))
            }
        }

        // Update listing
        put("{id}") {
            // @OpenAPI(
            //   summary = "Update listing by ID",
            //   responses = [
            //     OpenApiResponse(status = "200", description = "Listing updated successfully with the updated listing object"),
            //     OpenApiResponse(status = "400", description = "Invalid data or ID mismatch"),
            //     OpenApiResponse(status = "404", description = "Listing not found")
            //   ]
            // )
            try {
                val idString = call.parameters["id"] ?: return@put call.respond(HttpStatusCode.BadRequest)
                val id = UUID.fromString(idString)
                val listing = call.receive<Listing>()
                val updatedListing = listingService.updateListing(id, listing)
                if (updatedListing != null) {
                    call.respond(HttpStatusCode.OK, updatedListing)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Listing not found"))
                }
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to update listing"))
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
            try {
                val idString = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                val id = UUID.fromString(idString)
                if (listingService.deleteListing(id)) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Listing not found"))
                }
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid UUID format"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to delete listing"))
            }
        }
    }
}
