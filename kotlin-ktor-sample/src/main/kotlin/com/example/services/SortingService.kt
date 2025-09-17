package com.example.services

import com.example.models.Listing
import com.example.utilities.DistanceCalculator
import com.example.utilities.GeohashUtility

class SortingService : ISortingService {
    
    override suspend fun applyAllSortingRules(
        listings: List<Listing>,
        latitude: Double?,
        longitude: Double?
    ): List<Listing> {
        // Create a list of tuples with calculated values for sorting
        val listingsWithSortData = listings.map { listing ->
            var distance = Double.MAX_VALUE // Default value when no location provided

            // Calculate distance if client location is provided
            if (latitude != null && longitude != null) {
                try {
                    val (listingLat, listingLon) = GeohashUtility.decodeGeohash(listing.location.geohash)
                    distance = DistanceCalculator.calculateDistanceKm(
                        latitude, longitude,
                        listingLat, listingLon
                    )
                } catch (e: IllegalArgumentException) {
                    // If geohash is invalid, keep max distance
                    distance = Double.MAX_VALUE
                }
            }

            Triple(listing, distance, listing.price.amount)
        }

        // Apply compound sorting:
        // Rule 1: Sort by distance (ascending - closest first)
        // Rule 2: Then by price (ascending - cheaper first) 
        // Rule 3: Placeholder for future sorting (currently no additional sorting)
        return listingsWithSortData
            .sortedWith(
                compareBy<Triple<Listing, Double, Double>> { it.second } // Rule 1: Distance
                    .thenBy { it.third } // Rule 2: Price
                    // Rule 3: Placeholder - can add .thenBy() for future sorting criteria
            )
            .map { it.first }
    }
}