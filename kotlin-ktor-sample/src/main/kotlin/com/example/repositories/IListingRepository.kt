package com.example.repositories

import com.example.models.Listing
import java.util.UUID

interface IListingRepository {
    suspend fun getAllAsync(page: Int, pageSize: Int): List<Listing>
    suspend fun getTotalCountAsync(): Int
    suspend fun getByIdAsync(id: UUID): Listing?
    suspend fun createAsync(listing: Listing): Listing
    suspend fun updateAsync(listing: Listing): Listing?
    suspend fun deleteAsync(id: UUID): Boolean
    suspend fun existsAsync(id: UUID): Boolean
}