package com.example.services

import com.example.models.Listing
import com.example.models.Price
import com.example.models.Location
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SortingServiceTest {

    private lateinit var sortingService: SortingService
    private lateinit var testListings: List<Listing>

    @BeforeEach
    fun setup() {
        sortingService = SortingService()

        // Create test listings with different locations and prices
        testListings = listOf(
            Listing(
                listingId = UUID.randomUUID(),
                name = "Expensive Item Far Away",
                description = "Test",
                price = Price(currency = "USD", amount = BigDecimal("500.00")),
                category = "Electronics",
                location = Location(
                    country = "US",
                    municipality = "New York",
                    geohash = "dr5ru7v"
                ) // Far from Austin
            ),
            Listing(
                listingId = UUID.randomUUID(),
                name = "Cheap Item Nearby",
                description = "Test",
                price = Price(currency = "USD", amount = BigDecimal("50.00")),
                category = "Electronics",
                location = Location(
                    country = "US",
                    municipality = "Austin",
                    geohash = "dr5regw"
                ) // Austin, TX
            ),
            Listing(
                listingId = UUID.randomUUID(),
                name = "Medium Price Item Nearby",
                description = "Test",
                price = Price(currency = "USD", amount = BigDecimal("200.00")),
                category = "Electronics",
                location = Location(
                    country = "US",
                    municipality = "Austin",
                    geohash = "dr5regw"
                ) // Austin, TX
            )
        )
    }

    @Test
    fun `applyAllSortingRules without location sorts by price only`() = runTest {
        // Act
        val result = sortingService.applyAllSortingRules(testListings, null, null)

        // Assert
        assertEquals(3, result.size)
        // Should be sorted by price (ascending)
        assertEquals(BigDecimal("50.00"), result[0].price.amount)
        assertEquals(BigDecimal("200.00"), result[1].price.amount)
        assertEquals(BigDecimal("500.00"), result[2].price.amount)
    }

    @Test
    fun `applyAllSortingRules with location sorts by distance then price`() = runTest {
        // Arrange - Using Austin, TX coordinates
        val latitude = 30.2672
        val longitude = -97.7431

        // Act
        val result = sortingService.applyAllSortingRules(testListings, latitude, longitude)

        // Assert
        assertEquals(3, result.size)

        // First two items should be from Austin (same distance), sorted by price
        assertEquals(BigDecimal("50.00"), result[0].price.amount) // Cheap item nearby
        assertEquals(BigDecimal("200.00"), result[1].price.amount) // Medium price item nearby

        // Last item should be the expensive item from far away
        assertEquals(BigDecimal("500.00"), result[2].price.amount) // Expensive item far away
        assertEquals("New York", result[2].location.municipality)
    }

    @Test
    fun `applyAllSortingRules with invalid geohash handles safely`() = runTest {
        // Arrange
        val listingWithInvalidGeohash = Listing(
            listingId = UUID.randomUUID(),
            name = "Invalid Geohash Item",
            description = "Test",
            price = Price(currency = "USD", amount = BigDecimal("100.00")),
            category = "Electronics",
            location = Location(
                country = "US",
                municipality = "Unknown",
                geohash = "invalid"
            )
        )

        val listings = listOf(listingWithInvalidGeohash, testListings[1])
        val latitude = 30.2672
        val longitude = -97.7431

        // Act
        val result = sortingService.applyAllSortingRules(listings, latitude, longitude)

        // Assert
        assertEquals(2, result.size)
        // Item with valid geohash should come first (closer)
        assertEquals("dr5regw", result[0].location.geohash)
        // Item with invalid geohash should come last (max distance)
        assertEquals("invalid", result[1].location.geohash)
    }

    @Test
    fun `applyAllSortingRules with empty list returns empty`() = runTest {
        // Arrange
        val emptyList = emptyList<Listing>()

        // Act
        val result = sortingService.applyAllSortingRules(emptyList, 30.2672, -97.7431)

        // Assert
        assertTrue(result.isEmpty())
    }

    @Test
    fun `applyAllSortingRules with same price and distance maintains stable order`() = runTest {
        // Arrange - Create items with identical price and location
        val identicalItems = listOf(
            Listing(
                listingId = UUID.randomUUID(),
                name = "First Item",
                description = "Test",
                price = Price(currency = "USD", amount = BigDecimal("100.00")),
                category = "Electronics",
                location = Location(
                    country = "US",
                    municipality = "Austin",
                    geohash = "dr5regw"
                )
            ),
            Listing(
                listingId = UUID.randomUUID(),
                name = "Second Item",
                description = "Test",
                price = Price(currency = "USD", amount = BigDecimal("100.00")),
                category = "Electronics",
                location = Location(
                    country = "US",
                    municipality = "Austin",
                    geohash = "dr5regw"
                )
            )
        )

        // Act
        val result = sortingService.applyAllSortingRules(identicalItems, 30.2672, -97.7431)

        // Assert
        assertEquals(2, result.size)
        // Both items should be present and maintain their relative order
        assertEquals("First Item", result[0].name)
        assertEquals("Second Item", result[1].name)
    }
}