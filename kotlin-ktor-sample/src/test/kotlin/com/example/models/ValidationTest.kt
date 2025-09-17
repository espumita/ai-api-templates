package com.example.models

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ListingValidationTest {

    @Test
    fun `Listing with valid data creates successfully`() {
        // Arrange & Act
        val listing = Listing(
            name = "Test Item",
            description = "Test Description",
            price = Price(currency = "USD", amount = BigDecimal("99.99")),
            category = "Electronics",
            location = Location(
                country = "US",
                municipality = "Austin",
                geohash = "dr5regw"
            )
        )

        // Assert
        assertEquals("Test Item", listing.name)
        assertEquals("Test Description", listing.description)
        assertEquals("USD", listing.price.currency)
        assertEquals(BigDecimal("99.99"), listing.price.amount)
        assertEquals("Electronics", listing.category)
    }

    @Test
    fun `Listing with blank name throws IllegalArgumentException`() {
        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            Listing(
                name = "",
                description = "Test Description",
                price = Price(currency = "USD", amount = BigDecimal("99.99")),
                category = "Electronics",
                location = Location(
                    country = "US",
                    municipality = "Austin",
                    geohash = "dr5regw"
                )
            )
        }
        assertTrue(exception.message!!.contains("Name cannot be blank"))
    }

    @Test
    fun `Listing with name exceeding 100 characters throws IllegalArgumentException`() {
        // Arrange
        val longName = "a".repeat(101)

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            Listing(
                name = longName,
                description = "Test Description",
                price = Price(currency = "USD", amount = BigDecimal("99.99")),
                category = "Electronics",
                location = Location(
                    country = "US",
                    municipality = "Austin",
                    geohash = "dr5regw"
                )
            )
        }
        assertTrue(exception.message!!.contains("Name cannot exceed 100 characters"))
    }

    @Test
    fun `Listing with blank description throws IllegalArgumentException`() {
        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            Listing(
                name = "Test Item",
                description = "",
                price = Price(currency = "USD", amount = BigDecimal("99.99")),
                category = "Electronics",
                location = Location(
                    country = "US",
                    municipality = "Austin",
                    geohash = "dr5regw"
                )
            )
        }
        assertTrue(exception.message!!.contains("Description cannot be blank"))
    }

    @Test
    fun `Listing with invalid category throws IllegalArgumentException`() {
        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            Listing(
                name = "Test Item",
                description = "Test Description",
                price = Price(currency = "USD", amount = BigDecimal("99.99")),
                category = "InvalidCategory",
                location = Location(
                    country = "US",
                    municipality = "Austin",
                    geohash = "dr5regw"
                )
            )
        }
        assertTrue(exception.message!!.contains("Invalid category: InvalidCategory"))
    }

    @Test
    fun `Listing with all valid categories creates successfully`() {
        val validCategories = Listing.VALID_CATEGORIES

        validCategories.forEach { category ->
            val listing = Listing(
                name = "Test Item",
                description = "Test Description",
                price = Price(currency = "USD", amount = BigDecimal("99.99")),
                category = category,
                location = Location(
                    country = "US",
                    municipality = "Austin",
                    geohash = "dr5regw"
                )
            )
            assertEquals(category, listing.category)
        }
    }
}

class PriceValidationTest {

    @Test
    fun `Price with valid data creates successfully`() {
        // Arrange & Act
        val price = Price(currency = "USD", amount = BigDecimal("99.99"))

        // Assert
        assertEquals("USD", price.currency)
        assertEquals(BigDecimal("99.99"), price.amount)
    }

    @Test
    fun `Price with invalid currency code throws IllegalArgumentException`() {
        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            Price(currency = "US", amount = BigDecimal("99.99"))
        }
        assertTrue(exception.message!!.contains("Currency must be a 3-character code"))
    }

    @Test
    fun `Price with currency code longer than 3 characters throws IllegalArgumentException`() {
        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            Price(currency = "USDD", amount = BigDecimal("99.99"))
        }
        assertTrue(exception.message!!.contains("Currency must be a 3-character code"))
    }

    @Test
    fun `Price with zero amount throws IllegalArgumentException`() {
        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            Price(currency = "USD", amount = BigDecimal.ZERO)
        }
        assertTrue(exception.message!!.contains("Amount must be greater than 0"))
    }

    @Test
    fun `Price with negative amount throws IllegalArgumentException`() {
        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            Price(currency = "USD", amount = BigDecimal("-10.00"))
        }
        assertTrue(exception.message!!.contains("Amount must be greater than 0"))
    }
}

class LocationValidationTest {

    @Test
    fun `Location with valid data creates successfully`() {
        // Arrange & Act
        val location = Location(
            country = "US",
            municipality = "Austin",
            geohash = "dr5regw"
        )

        // Assert
        assertEquals("US", location.country)
        assertEquals("Austin", location.municipality)
        assertEquals("dr5regw", location.geohash)
    }

    @Test
    fun `Location with invalid country code throws IllegalArgumentException`() {
        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            Location(
                country = "USA",
                municipality = "Austin",
                geohash = "dr5regw"
            )
        }
        assertTrue(exception.message!!.contains("Country must be a valid ISO 3166 2-character code"))
    }

    @Test
    fun `Location with single character country code throws IllegalArgumentException`() {
        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            Location(
                country = "U",
                municipality = "Austin",
                geohash = "dr5regw"
            )
        }
        assertTrue(exception.message!!.contains("Country must be a valid ISO 3166 2-character code"))
    }

    @Test
    fun `Location with blank municipality throws IllegalArgumentException`() {
        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            Location(
                country = "US",
                municipality = "",
                geohash = "dr5regw"
            )
        }
        assertTrue(exception.message!!.contains("Municipality cannot be blank"))
    }

    @Test
    fun `Location with invalid geohash length throws IllegalArgumentException`() {
        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            Location(
                country = "US",
                municipality = "Austin",
                geohash = "dr5reg" // 6 characters instead of 7
            )
        }
        assertTrue(exception.message!!.contains("Geohash must be exactly 7 characters"))
    }

    @Test
    fun `Location with geohash longer than 7 characters throws IllegalArgumentException`() {
        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            Location(
                country = "US",
                municipality = "Austin",
                geohash = "dr5regwu" // 8 characters instead of 7
            )
        }
        assertTrue(exception.message!!.contains("Geohash must be exactly 7 characters"))
    }
}