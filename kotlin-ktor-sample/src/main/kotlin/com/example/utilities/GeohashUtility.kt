package com.example.utilities

import kotlin.math.*

object GeohashUtility {
    private val base32 = "0123456789bcdefghjkmnpqrstuvwxyz".toCharArray()
    private val base32Map = base32.mapIndexed { index, char -> char to index }.toMap()

    /**
     * Decode a geohash string to latitude and longitude coordinates
     * @param geohash The geohash string to decode
     * @return Pair of (latitude, longitude) coordinates
     * @throws IllegalArgumentException if geohash is invalid
     */
    fun decodeGeohash(geohash: String): Pair<Double, Double> {
        if (geohash.isBlank()) {
            throw IllegalArgumentException("Geohash cannot be null or empty")
        }

        var latRange = doubleArrayOf(-90.0, 90.0)
        var lonRange = doubleArrayOf(-180.0, 180.0)
        var isEvenBit = true

        for (c in geohash.lowercase()) {
            val cd = base32Map[c] ?: throw IllegalArgumentException("Invalid geohash character: $c")

            for (i in 4 downTo 0) {
                val bit = (cd shr i) and 1
                if (isEvenBit) {
                    // longitude
                    val mid = (lonRange[0] + lonRange[1]) / 2
                    if (bit == 1) {
                        lonRange[0] = mid
                    } else {
                        lonRange[1] = mid
                    }
                } else {
                    // latitude
                    val mid = (latRange[0] + latRange[1]) / 2
                    if (bit == 1) {
                        latRange[0] = mid
                    } else {
                        latRange[1] = mid
                    }
                }
                isEvenBit = !isEvenBit
            }
        }

        val lat = (latRange[0] + latRange[1]) / 2
        val lon = (lonRange[0] + lonRange[1]) / 2

        return Pair(lat, lon)
    }
}

object DistanceCalculator {
    private const val EARTH_RADIUS_KM = 6371.0

    /**
     * Calculate the distance between two points on Earth using the Haversine formula
     * @param lat1 Latitude of first point in degrees
     * @param lon1 Longitude of first point in degrees
     * @param lat2 Latitude of second point in degrees
     * @param lon2 Longitude of second point in degrees
     * @return Distance in kilometers
     */
    fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val lat1Rad = degreesToRadians(lat1)
        val lat2Rad = degreesToRadians(lat2)
        val deltaLatRad = degreesToRadians(lat2 - lat1)
        val deltaLonRad = degreesToRadians(lon2 - lon1)

        val a = sin(deltaLatRad / 2).pow(2) +
                cos(lat1Rad) * cos(lat2Rad) *
                sin(deltaLonRad / 2).pow(2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return EARTH_RADIUS_KM * c
    }

    private fun degreesToRadians(degrees: Double): Double {
        return degrees * PI / 180.0
    }
}