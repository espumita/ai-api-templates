package com.example.services

import com.example.models.Listing
import com.example.models.PaginatedListingsResponse
import com.example.models.SearchRequest
import com.example.models.SearchResponse
import com.example.repositories.IListingRepository
import java.util.UUID

class ListingService(
    private val listingRepository: IListingRepository,
    private val sortingService: ISortingService
) {

    fun createListing(listing: Listing): Listing {
        return listingRepository.create(listing)
    }

    fun getListing(id: UUID): Listing? {
        return listingRepository.getById(id)
    }

    suspend fun getAllListings(page: Int, pageSize: Int, latitude: Double? = null, longitude: Double? = null): PaginatedListingsResponse {
        val items = listingRepository.getAll(page, pageSize)
        val totalItems = listingRepository.getTotalCount()
        
        // Apply sorting if location parameters are provided
        val sortedItems = if (latitude != null && longitude != null) {
            sortingService.applyAllSortingRules(items, latitude, longitude)
        } else {
            // Apply sorting without location (only price sorting)
            sortingService.applyAllSortingRules(items, null, null)
        }
        
        return PaginatedListingsResponse(
            items = sortedItems,
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

    suspend fun searchListings(searchRequest: SearchRequest): SearchResponse {
        val items = listingRepository.searchListings(
            filters = searchRequest.filters,
            page = searchRequest.page,
            pageSize = searchRequest.pageSize
        )
        
        // Apply sorting
        val sortedItems = sortingService.applyAllSortingRules(
            items, 
            searchRequest.latitude, 
            searchRequest.longitude
        )
        
        val totalItems = listingRepository.getSearchResultCount(searchRequest.filters)
        
        return SearchResponse(
            items = sortedItems,
            totalItems = totalItems,
            page = searchRequest.page,
            pageSize = searchRequest.pageSize,
            appliedFilters = searchRequest.filters
        )
    }
}
