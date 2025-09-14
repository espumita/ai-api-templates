package com.example.repositories

import com.example.models.Listing
import com.example.models.SearchFilter
import java.util.UUID

interface IListingRepository {
    fun getAll(page: Int, pageSize: Int): List<Listing>
    fun getTotalCount(): Int
    fun getById(id: UUID): Listing?
    fun create(listing: Listing): Listing
    fun update(listing: Listing): Listing?
    fun delete(id: UUID): Boolean
    fun exists(id: UUID): Boolean
    fun searchListings(filters: List<SearchFilter>, page: Int, pageSize: Int): List<Listing>
    fun getSearchResultCount(filters: List<SearchFilter>): Int
}