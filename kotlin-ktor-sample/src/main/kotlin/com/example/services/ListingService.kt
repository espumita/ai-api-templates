package com.example.services

import com.example.models.Listing
import com.example.models.PaginatedListingsResponse
import com.example.repositories.IListingRepository
import java.util.UUID

class ListingService(private val listingRepository: IListingRepository) {

    suspend fun createListing(listing: Listing): Listing {
        return listingRepository.createAsync(listing)
    }

    suspend fun getListing(id: UUID): Listing? {
        return listingRepository.getByIdAsync(id)
    }

    suspend fun getAllListings(page: Int, pageSize: Int): PaginatedListingsResponse {
        val items = listingRepository.getAllAsync(page, pageSize)
        val totalItems = listingRepository.getTotalCountAsync()
        
        return PaginatedListingsResponse(
            items = items,
            totalItems = totalItems,
            page = page,
            pageSize = pageSize
        )
    }

    suspend fun updateListing(id: UUID, listing: Listing): Listing? {
        val updatedListing = listing.copy(listingId = id)
        return listingRepository.updateAsync(updatedListing)
    }

    suspend fun deleteListing(id: UUID): Boolean {
        return listingRepository.deleteAsync(id)
    }

    suspend fun listingExists(id: UUID): Boolean {
        return listingRepository.existsAsync(id)
    }
}
