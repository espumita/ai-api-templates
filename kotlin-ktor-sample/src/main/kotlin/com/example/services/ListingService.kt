package com.example.services

import com.example.models.Listing
import java.util.concurrent.ConcurrentHashMap

class ListingService {
    private val listings = ConcurrentHashMap<String, Listing>()

    fun createListing(listing: Listing): Listing {
        listings[listing.listingId] = listing
        return listing
    }

    fun getListing(id: String): Listing? = listings[id]

    fun getAllListings(page: Int, pageSize: Int): Map<String, Any> {
        val startIndex = page * pageSize
        val items = listings.values.drop(startIndex).take(pageSize).toList()
        return mapOf(
            "items" to items,
            "totalItems" to listings.size,
            "page" to page,
            "pageSize" to pageSize
        )
    }

    fun updateListing(id: String, listing: Listing): Listing? {
        if (!listings.containsKey(id)) return null
        val updatedListing = listing.copy(listingId = id)
        listings[id] = updatedListing
        return updatedListing
    }

    fun deleteListing(id: String): Boolean = listings.remove(id) != null
}
