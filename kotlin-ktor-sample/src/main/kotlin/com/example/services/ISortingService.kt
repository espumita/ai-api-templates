package com.example.services

import com.example.models.Listing

interface ISortingService {
    suspend fun applyAllSortingRules(
        listings: List<Listing>,
        latitude: Double?,
        longitude: Double?
    ): List<Listing>
}