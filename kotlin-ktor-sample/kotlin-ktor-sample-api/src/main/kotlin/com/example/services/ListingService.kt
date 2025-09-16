package com.example.services

import com.example.models.Listing
import com.example.models.PaginatedListingsResponse
import com.example.models.SearchRequest
import com.example.models.SearchResponse
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

    fun searchListings(searchRequest: SearchRequest): SearchResponse {
        val items = listingRepository.searchListings(
            filters = searchRequest.filters,
            page = searchRequest.page,
            pageSize = searchRequest.pageSize
        )
        
        val totalItems = listingRepository.getSearchResultCount(searchRequest.filters)
        
        return SearchResponse(
            items = items,
            totalItems = totalItems,
            page = searchRequest.page,
            pageSize = searchRequest.pageSize,
            appliedFilters = searchRequest.filters
        )
    }
}
