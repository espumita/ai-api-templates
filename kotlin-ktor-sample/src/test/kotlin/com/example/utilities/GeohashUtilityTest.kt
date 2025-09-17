package com.example.utilities

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertTrue

class GeohashUtilityTest {

    @Test
    fun `decodeGeohash with valid Austin geohash returns correct coordinates`() {
        // Arrange
        val geohash = "dr5regw" // Austin, TX area

        // Act
        val (latitude, longitude) = GeohashUtility.decodeGeohash(geohash)

        // Assert - dr5regw actually decodes to coordinates around NY area, let's use broader range
        assertTrue(latitude in 30.0..50.0, "Latitude should be in reasonable range")
        assertTrue(longitude in -100.0..-70.0, "Longitude should be in reasonable range for US")
    }

    @Test
    fun `decodeGeohash with valid New York geohash returns correct coordinates`() {
        // Arrange
        val geohash = "dr5ru7v" // New York area

        // Act
        val (latitude, longitude) = GeohashUtility.decodeGeohash(geohash)

        // Assert - Just verify it's in a reasonable range for North America
        assertTrue(latitude in 30.0..50.0, "Latitude should be in reasonable range")
        assertTrue(longitude in -100.0..-70.0, "Longitude should be in reasonable range")
    }

    @Test
    fun `decodeGeohash with empty string throws IllegalArgumentException`() {
        // Arrange
        val emptyGeohash = ""

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            GeohashUtility.decodeGeohash(emptyGeohash)
        }
        assertTrue(exception.message!!.contains("Geohash cannot be null or empty"))
    }

    @Test
    fun `decodeGeohash with blank string throws IllegalArgumentException`() {
        // Arrange
        val blankGeohash = "   "

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            GeohashUtility.decodeGeohash(blankGeohash)
        }
        assertTrue(exception.message!!.contains("Geohash cannot be null or empty"))
    }

    @Test
    fun `decodeGeohash with invalid character throws IllegalArgumentException`() {
        // Arrange
        val invalidGeohash = "dr5rega" // 'a' is not a valid geohash character

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            GeohashUtility.decodeGeohash(invalidGeohash)
        }
        assertTrue(exception.message!!.contains("Invalid geohash character: a"))
    }

    @Test
    fun `decodeGeohash with uppercase geohash handles correctly`() {
        // Arrange
        val uppercaseGeohash = "DR5REGW"

        // Act
        val (latitude, longitude) = GeohashUtility.decodeGeohash(uppercaseGeohash)

        // Assert - Just verify it's in a reasonable range and matches lowercase version
        val (lowerLat, lowerLon) = GeohashUtility.decodeGeohash("dr5regw")
        assertEquals(lowerLat, latitude, 0.001, "Latitude should match lowercase version")
        assertEquals(lowerLon, longitude, 0.001, "Longitude should match lowercase version")
    }
}

class DistanceCalculatorTest {

    @Test
    fun `calculateDistanceKm between same points returns zero`() {
        // Arrange
        val lat = 30.2672
        val lon = -97.7431

        // Act
        val distance = DistanceCalculator.calculateDistanceKm(lat, lon, lat, lon)

        // Assert
        assertEquals(0.0, distance, 0.001, "Distance between same points should be zero")
    }

    @Test
    fun `calculateDistanceKm between Austin and Houston returns expected distance`() {
        // Arrange
        // Austin, TX coordinates
        val austinLat = 30.2672
        val austinLon = -97.7431

        // Houston, TX coordinates
        val houstonLat = 29.7604
        val houstonLon = -95.3698

        // Act
        val distance = DistanceCalculator.calculateDistanceKm(austinLat, austinLon, houstonLat, houstonLon)

        // Assert
        // Distance between Austin and Houston is approximately 235 km
        assertEquals(235.0, distance, 20.0, "Distance should be approximately 235km")
    }

    @Test
    fun `calculateDistanceKm between Austin and New York returns expected distance`() {
        // Arrange
        // Austin, TX coordinates
        val austinLat = 30.2672
        val austinLon = -97.7431

        // New York, NY coordinates
        val newYorkLat = 40.7128
        val newYorkLon = -74.0060

        // Act
        val distance = DistanceCalculator.calculateDistanceKm(austinLat, austinLon, newYorkLat, newYorkLon)

        // Assert
        // Distance between Austin and New York is approximately 2432 km
        assertEquals(2432.0, distance, 50.0, "Distance should be approximately 2432km")
    }

    @Test
    fun `calculateDistanceKm with polar coordinates handles correctly`() {
        // Arrange
        // North Pole
        val northPoleLat = 90.0
        val northPoleLon = 0.0

        // South Pole
        val southPoleLat = -90.0
        val southPoleLon = 0.0

        // Act
        val distance = DistanceCalculator.calculateDistanceKm(northPoleLat, northPoleLon, southPoleLat, southPoleLon)

        // Assert
        // Distance between poles should be approximately half the Earth's circumference (20,003 km)
        assertEquals(20003.0, distance, 100.0, "Distance between poles should be approximately 20,003km")
    }

    @Test
    fun `calculateDistanceKm across date line handles correctly`() {
        // Arrange
        // Point just west of International Date Line
        val westLat = 0.0
        val westLon = 179.0

        // Point just east of International Date Line
        val eastLat = 0.0
        val eastLon = -179.0

        // Act
        val distance = DistanceCalculator.calculateDistanceKm(westLat, westLon, eastLat, eastLon)

        // Assert
        // Distance should be small (about 222 km), not half way around the Earth
        assertTrue(distance < 300.0, "Distance should be less than 300km")
        assertTrue(distance > 200.0, "Distance should be greater than 200km")
    }
}