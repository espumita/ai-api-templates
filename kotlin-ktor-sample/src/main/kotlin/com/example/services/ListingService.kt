package com.example.services

import com.example.models.Listing
import com.example.models.PaginatedListingsResponse
import com.example.repositories.IListingRepository
import java.util.UUID

class ListingService(private val listingRepository: IListingRepository) {

    fun createListing(listing: Listing): Listing {
        return listingRepository.create(listing)
    }

    fun getListing(id: UUID): Listing? {
        return listingRepository.getById(id)
    }

    fun getAllListings(page: Int, pageSize: Int): PaginatedListingsResponse {
        val items = listingRepository.getAll(page, pageSize)
        val totalItems = listingRepository.getTotalCount()
        
        return PaginatedListingsResponse(
            items = items,
            totalItems = totalItems,
            page = page,
            pageSize = pageSize
        )
    }

    fun updateListing(id: UUID, listing: Listing): Listing? {
        val updatedListing = listing.copy(listingId = id)
        return listingRepository.update(updatedListing)
    }

    fun deleteListing(id: UUID): Boolean {
        return listingRepository.delete(id)
    }

    fun listingExists(id: UUID): Boolean {
        return listingRepository.exists(id)
    }
}
