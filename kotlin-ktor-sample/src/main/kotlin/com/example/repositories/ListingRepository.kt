package com.example.repositories

import com.example.models.Listing
import com.example.models.Price
import com.example.models.Location
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.*
import java.util.UUID
import javax.sql.DataSource

class ListingRepository(private val dataSource: DataSource) : IListingRepository {

    override suspend fun getAllAsync(page: Int, pageSize: Int): List<Listing> = withContext(Dispatchers.IO) {
        val sql = """
            SELECT 
                listing_id,
                name,
                description,
                price_currency,
                price_amount,
                category,
                location_country,
                location_municipality,
                location_geohash
            FROM listings 
            ORDER BY created_at DESC
            LIMIT ? OFFSET ?
        """.trimIndent()

        dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                val offset = (page - 1) * pageSize
                statement.setInt(1, pageSize)
                statement.setInt(2, offset)
                
                statement.executeQuery().use { resultSet ->
                    val listings = mutableListOf<Listing>()
                    while (resultSet.next()) {
                        listings.add(mapToListing(resultSet))
                    }
                    listings
                }
            }
        }
    }

    override suspend fun getTotalCountAsync(): Int = withContext(Dispatchers.IO) {
        val sql = "SELECT COUNT(*) FROM listings"
        
        dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.executeQuery().use { resultSet ->
                    resultSet.next()
                    resultSet.getInt(1)
                }
            }
        }
    }

    override suspend fun getByIdAsync(id: UUID): Listing? = withContext(Dispatchers.IO) {
        val sql = """
            SELECT 
                listing_id,
                name,
                description,
                price_currency,
                price_amount,
                category,
                location_country,
                location_municipality,
                location_geohash
            FROM listings 
            WHERE listing_id = ?
        """.trimIndent()

        dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setObject(1, id)
                
                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {
                        mapToListing(resultSet)
                    } else {
                        null
                    }
                }
            }
        }
    }

    override suspend fun createAsync(listing: Listing): Listing = withContext(Dispatchers.IO) {
        val sql = """
            INSERT INTO listings (
                listing_id, name, description, price_currency, price_amount, 
                category, location_country, location_municipality, location_geohash
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

        val newListing = listing.copy(listingId = UUID.randomUUID())

        dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setObject(1, newListing.listingId)
                statement.setString(2, newListing.name)
                statement.setString(3, newListing.description)
                statement.setString(4, newListing.price.currency)
                statement.setDouble(5, newListing.price.amount)
                statement.setString(6, newListing.category)
                statement.setString(7, newListing.location.country)
                statement.setString(8, newListing.location.municipality)
                statement.setString(9, newListing.location.geohash)
                
                statement.executeUpdate()
            }
        }

        newListing
    }

    override suspend fun updateAsync(listing: Listing): Listing? = withContext(Dispatchers.IO) {
        val sql = """
            UPDATE listings SET 
                name = ?,
                description = ?,
                price_currency = ?,
                price_amount = ?,
                category = ?,
                location_country = ?,
                location_municipality = ?,
                location_geohash = ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE listing_id = ?
        """.trimIndent()

        dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setString(1, listing.name)
                statement.setString(2, listing.description)
                statement.setString(3, listing.price.currency)
                statement.setDouble(4, listing.price.amount)
                statement.setString(5, listing.category)
                statement.setString(6, listing.location.country)
                statement.setString(7, listing.location.municipality)
                statement.setString(8, listing.location.geohash)
                statement.setObject(9, listing.listingId)
                
                val rowsAffected = statement.executeUpdate()
                if (rowsAffected > 0) listing else null
            }
        }
    }

    override suspend fun deleteAsync(id: UUID): Boolean = withContext(Dispatchers.IO) {
        val sql = "DELETE FROM listings WHERE listing_id = ?"
        
        dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setObject(1, id)
                val rowsAffected = statement.executeUpdate()
                rowsAffected > 0
            }
        }
    }

    override suspend fun existsAsync(id: UUID): Boolean = withContext(Dispatchers.IO) {
        val sql = "SELECT COUNT(*) FROM listings WHERE listing_id = ?"
        
        dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setObject(1, id)
                
                statement.executeQuery().use { resultSet ->
                    resultSet.next()
                    resultSet.getInt(1) > 0
                }
            }
        }
    }

    private fun mapToListing(resultSet: ResultSet): Listing {
        return Listing(
            listingId = resultSet.getObject("listing_id", UUID::class.java),
            name = resultSet.getString("name"),
            description = resultSet.getString("description"),
            price = Price(
                currency = resultSet.getString("price_currency"),
                amount = resultSet.getDouble("price_amount")
            ),
            category = resultSet.getString("category"),
            location = Location(
                country = resultSet.getString("location_country"),
                municipality = resultSet.getString("location_municipality"),
                geohash = resultSet.getString("location_geohash")
            )
        )
    }
}