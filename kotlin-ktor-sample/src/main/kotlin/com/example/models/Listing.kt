package com.example.models

import java.math.BigDecimal
import java.util.UUID

data class Price(
    val currency: String,
    val amount: BigDecimal
) {
    init {
        require(currency.length == 3) { "Currency must be a 3-character code" }
        require(amount > BigDecimal.ZERO) { "Amount must be greater than 0" }
    }
}

data class Location(
    val country: String,
    val municipality: String,
    val geohash: String
) {
    init {
        require(country.length == 2) { "Country must be a valid ISO 3166 2-character code" }
        require(municipality.isNotBlank()) { "Municipality cannot be blank" }
        require(geohash.length == 7) { "Geohash must be exactly 7 characters" }
    }
}

data class Listing(
    val listingId: UUID = UUID.randomUUID(),
    val name: String,
    val description: String,
    val price: Price,
    val category: String,
    val location: Location
) {
    init {
        require(name.isNotBlank()) { "Name cannot be blank" }
        require(name.length <= 100) { "Name cannot exceed 100 characters" }
        require(description.isNotBlank()) { "Description cannot be blank" }
        require(VALID_CATEGORIES.contains(category)) { "Invalid category: $category" }
    }

    companion object {
        val VALID_CATEGORIES = setOf(
            "Electronics",
            "Fashion",
            "Home & Garden",
            "Motors",
            "Collectibles & Art",
            "Sporting Goods",
            "Toys & Hobbies",
            "Business & Industrial",
            "Music",
            "Health & Beauty",
            "Books",
            "Cameras & Photo",
            "Computers, Tablets & Networking",
            "Cell Phones & Accessories",
            "Video Games & Consoles"
        )
    }
}

data class PaginatedListingsResponse(
    val items: List<Listing>,
    val totalItems: Int,
    val page: Int,
    val pageSize: Int
)
