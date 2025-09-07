package com.example.models

import java.util.UUID

data class Price(
    val currency: String,
    val amount: Double
)

data class Location(
    val country: String,
    val municipality: String,
    val geohash: String
)

data class Listing(
    val listingId: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val price: Price,
    val category: String,
    val location: Location
) {
    init {
        require(name.isNotBlank()) { "Name cannot be blank" }
        require(description.isNotBlank()) { "Description cannot be blank" }
        require(price.amount > 0) { "Price amount must be positive" }
        require(VALID_CATEGORIES.contains(category)) { "Invalid category: $category" }
        require(location.country.length == 2) { "Country must be a valid ISO 3166 2-character code" }
        require(location.municipality.isNotBlank()) { "Municipality cannot be blank" }
        require(location.geohash.length == 7) { "Geohash must be exactly 7 characters" }
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
