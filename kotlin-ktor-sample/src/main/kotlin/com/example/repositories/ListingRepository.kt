package com.example.repositories

import com.example.models.Listing
import com.example.models.Price
import com.example.models.Location
import com.example.models.SearchFilter
import com.example.models.FilterOperator
import java.math.BigDecimal
import java.sql.*
import java.util.UUID
import javax.sql.DataSource

class ListingRepository(private val dataSource: DataSource) : IListingRepository {

    override fun getAll(page: Int, pageSize: Int): List<Listing> {
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

        return dataSource.connection.use { connection ->
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

    override fun getTotalCount(): Int {
        val sql = "SELECT COUNT(*) FROM listings"
        
        return dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.executeQuery().use { resultSet ->
                    resultSet.next()
                    resultSet.getInt(1)
                }
            }
        }
    }

    override fun getById(id: UUID): Listing? {
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

        return dataSource.connection.use { connection ->
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

    override fun create(listing: Listing): Listing {
        val sql = """
            INSERT INTO listings (
                listing_id, name, description, price_currency, price_amount, 
                category, location_country, location_municipality, location_geohash
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

        val newListing = listing.copy(listingId = UUID.randomUUID())

        dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                connection.prepareStatement(sql).use { statement ->
                    statement.setObject(1, newListing.listingId)
                    statement.setString(2, newListing.name)
                    statement.setString(3, newListing.description)
                    statement.setString(4, newListing.price.currency)
                    statement.setBigDecimal(5, newListing.price.amount)
                    statement.setString(6, newListing.category)
                    statement.setString(7, newListing.location.country)
                    statement.setString(8, newListing.location.municipality)
                    statement.setString(9, newListing.location.geohash)
                    
                    statement.executeUpdate()
                }
                connection.commit()
            } catch (e: Exception) {
                connection.rollback()
                throw e
            }
        }

        return newListing
    }

    override fun update(listing: Listing): Listing? {
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

        return dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                connection.prepareStatement(sql).use { statement ->
                    statement.setString(1, listing.name)
                    statement.setString(2, listing.description)
                    statement.setString(3, listing.price.currency)
                    statement.setBigDecimal(4, listing.price.amount)
                    statement.setString(5, listing.category)
                    statement.setString(6, listing.location.country)
                    statement.setString(7, listing.location.municipality)
                    statement.setString(8, listing.location.geohash)
                    statement.setObject(9, listing.listingId)
                    
                    val rowsAffected = statement.executeUpdate()
                    connection.commit()
                    if (rowsAffected > 0) listing else null
                }
            } catch (e: Exception) {
                connection.rollback()
                throw e
            }
        }
    }

    override fun delete(id: UUID): Boolean {
        val sql = "DELETE FROM listings WHERE listing_id = ?"
        
        return dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                connection.prepareStatement(sql).use { statement ->
                    statement.setObject(1, id)
                    val rowsAffected = statement.executeUpdate()
                    connection.commit()
                    rowsAffected > 0
                }
            } catch (e: Exception) {
                connection.rollback()
                throw e
            }
        }
    }

    override fun exists(id: UUID): Boolean {
        val sql = "SELECT COUNT(*) FROM listings WHERE listing_id = ?"
        
        return dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setObject(1, id)
                
                statement.executeQuery().use { resultSet ->
                    resultSet.next()
                    resultSet.getInt(1) > 0
                }
            }
        }
    }

    override fun searchListings(filters: List<SearchFilter>, page: Int, pageSize: Int): List<Listing> {
        val baseQuery = """
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
        """.trimIndent()

        val (whereClause, parameters) = buildWhereClause(filters)
        val sql = if (whereClause.isNotEmpty()) {
            "$baseQuery WHERE $whereClause ORDER BY created_at DESC LIMIT ? OFFSET ?"
        } else {
            "$baseQuery ORDER BY created_at DESC LIMIT ? OFFSET ?"
        }

        return dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                var paramIndex = 1
                
                // Set filter parameters
                for ((_, value) in parameters) {
                    statement.setObject(paramIndex++, value)
                }
                
                // Set pagination parameters
                val offset = (page - 1) * pageSize
                statement.setInt(paramIndex++, pageSize)
                statement.setInt(paramIndex, offset)
                
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

    override fun getSearchResultCount(filters: List<SearchFilter>): Int {
        val baseQuery = "SELECT COUNT(*) FROM listings"
        val (whereClause, parameters) = buildWhereClause(filters)
        val sql = if (whereClause.isNotEmpty()) {
            "$baseQuery WHERE $whereClause"
        } else {
            baseQuery
        }

        return dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                var paramIndex = 1
                
                // Set filter parameters
                for ((_, value) in parameters) {
                    statement.setObject(paramIndex++, value)
                }
                
                statement.executeQuery().use { resultSet ->
                    resultSet.next()
                    resultSet.getInt(1)
                }
            }
        }
    }

    private fun buildWhereClause(filters: List<SearchFilter>): Pair<String, List<Pair<String, Any>>> {
        if (filters.isEmpty()) {
            return Pair("", emptyList())
        }

        val conditions = mutableListOf<String>()
        val parameters = mutableListOf<Pair<String, Any>>()

        for (filter in filters) {
            val columnName = mapFilterFieldToColumn(filter.field)
            
            when (filter.operator) {
                FilterOperator.CONTAINS -> {
                    conditions.add("LOWER($columnName) LIKE LOWER(?)")
                    parameters.add(Pair(columnName, "%${filter.value}%"))
                }
                FilterOperator.EQUALS -> {
                    conditions.add("$columnName = ?")
                    parameters.add(Pair(columnName, filter.value))
                }
            }
        }

        return Pair(conditions.joinToString(" AND "), parameters)
    }

    private fun mapFilterFieldToColumn(field: String): String {
        return when (field) {
            "name" -> "name"
            "description" -> "description"
            "category" -> "category"
            "location.country" -> "location_country"
            "location.municipality" -> "location_municipality"
            else -> throw IllegalArgumentException("Unsupported filter field: $field")
        }
    }

    private fun mapToListing(resultSet: ResultSet): Listing {
        return Listing(
            listingId = resultSet.getObject("listing_id", UUID::class.java),
            name = resultSet.getString("name"),
            description = resultSet.getString("description"),
            price = Price(
                currency = resultSet.getString("price_currency"),
                amount = resultSet.getBigDecimal("price_amount")
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