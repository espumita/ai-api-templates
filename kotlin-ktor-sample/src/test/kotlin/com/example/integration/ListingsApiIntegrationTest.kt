package com.example.integration

import com.example.database.DatabaseConfig
import com.example.models.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.*
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.math.BigDecimal
import java.util.*
import javax.sql.DataSource
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Testcontainers
class ListingsApiIntegrationTest {

    companion object {
        @Container
        val postgres: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:15-alpine")
            .withDatabaseName("marketplace_test")
            .withUsername("test")
            .withPassword("test")

        private lateinit var dataSource: DataSource
        private val objectMapper = ObjectMapper().apply {
            registerModule(KotlinModule.Builder().build())
        }

        @BeforeAll
        @JvmStatic
        fun setUp() {
            postgres.start()
            
            // Create database schema
            dataSource = DatabaseConfig.createDataSource(
                postgres.jdbcUrl,
                postgres.username,
                postgres.password
            )
            
            createSchema()
        }

        private fun createSchema() {
            dataSource.connection.use { connection ->
                connection.createStatement().execute("""
                    CREATE TABLE IF NOT EXISTS listings (
                        listing_id UUID PRIMARY KEY,
                        name VARCHAR(100) NOT NULL,
                        description TEXT NOT NULL,
                        price_currency VARCHAR(3) NOT NULL,
                        price_amount DECIMAL(10, 2) NOT NULL,
                        category VARCHAR(50) NOT NULL,
                        location_country VARCHAR(2) NOT NULL,
                        location_municipality VARCHAR(100) NOT NULL,
                        location_geohash VARCHAR(7) NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    )
                """.trimIndent())
            }
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            postgres.stop()
        }
    }

    @BeforeEach
    fun clearDatabase() {
        dataSource.connection.use { connection ->
            connection.createStatement().execute("DELETE FROM listings")
        }
    }

    @Test
    fun `POST api listings creates listing and returns 201`() = testApplication {
        // Arrange
        val client = createClientWithJson()
        
        val listing = Listing(
            name = "Test Guitar",
            description = "A beautiful acoustic guitar",
            price = Price(currency = "USD", amount = BigDecimal("299.99")),
            category = "Music",
            location = Location(
                country = "US",
                municipality = "Austin",
                geohash = "dr5regw"
            )
        )

        // Act
        val response = client.post("/api/listings") {
            contentType(ContentType.Application.Json)
            setBody(listing)
        }

        // Assert
        assertEquals(HttpStatusCode.Created, response.status)
        
        val createdListing: Listing = response.body()
        assertEquals("Test Guitar", createdListing.name)
        assertEquals("A beautiful acoustic guitar", createdListing.description)
        assertEquals("USD", createdListing.price.currency)
        assertEquals(BigDecimal("299.99"), createdListing.price.amount)
        assertEquals("Music", createdListing.category)
    }

    @Test
    fun `POST api listings with invalid data returns 400`() = testApplication {
        // Arrange
        val client = createClientWithJson()
        
        val invalidListing = mapOf(
            "name" to "",  // Invalid: blank name
            "description" to "Test description",
            "price" to mapOf("currency" to "USD", "amount" to 99.99),
            "category" to "Music",
            "location" to mapOf(
                "country" to "US",
                "municipality" to "Austin",
                "geohash" to "dr5regw"
            )
        )

        // Act
        val response = client.post("/api/listings") {
            contentType(ContentType.Application.Json)
            setBody(invalidListing)
        }

        // Assert
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `GET api listings returns all listings with pagination`() = testApplication {
        // Arrange
        val client = createClientWithJson()
        val listingId = createTestListing("Test Item", BigDecimal("99.99"))

        // Act
        val response = client.get("/api/listings?page=1&pageSize=10")

        // Assert
        assertEquals(HttpStatusCode.OK, response.status)
        
        val paginatedResponse: PaginatedListingsResponse = response.body()
        assertEquals(1, paginatedResponse.totalItems)
        assertEquals(1, paginatedResponse.page)
        assertEquals(10, paginatedResponse.pageSize)
        assertEquals(1, paginatedResponse.items.size)
        assertEquals(listingId, paginatedResponse.items[0].listingId)
    }

    @Test
    fun `GET api listings with page size exceeding 50 returns 400`() = testApplication {
        // Arrange
        val client = createClientWithJson()

        // Act
        val response = client.get("/api/listings?page=1&pageSize=51")

        // Assert
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `GET api listings id returns specific listing`() = testApplication {
        // Arrange
        val client = createClientWithJson()
        val listingId = createTestListing("Specific Item", BigDecimal("199.99"))

        // Act
        val response = client.get("/api/listings/$listingId")

        // Assert
        assertEquals(HttpStatusCode.OK, response.status)
        
        val listing: Listing = response.body()
        assertEquals(listingId, listing.listingId)
        assertEquals("Specific Item", listing.name)
        assertEquals(BigDecimal("199.99"), listing.price.amount)
    }

    @Test
    fun `GET api listings id with non-existent ID returns 404`() = testApplication {
        // Arrange
        val client = createClientWithJson()
        val nonExistentId = UUID.randomUUID()

        // Act
        val response = client.get("/api/listings/$nonExistentId")

        // Assert
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `PUT api listings id updates listing and returns updated object`() = testApplication {
        // Arrange
        val client = createClientWithJson()
        val listingId = createTestListing("Original Item", BigDecimal("100.00"))

        val updatedListing = Listing(
            listingId = listingId,
            name = "Updated Item",
            description = "Updated description",
            price = Price(currency = "USD", amount = BigDecimal("150.00")),
            category = "Music",
            location = Location(
                country = "US",
                municipality = "Austin",
                geohash = "dr5regw"
            )
        )

        // Act
        val response = client.put("/api/listings/$listingId") {
            contentType(ContentType.Application.Json)
            setBody(updatedListing)
        }

        // Assert
        assertEquals(HttpStatusCode.OK, response.status)
        
        val returnedListing: Listing = response.body()
        assertEquals("Updated Item", returnedListing.name)
        assertEquals("Updated description", returnedListing.description)
        assertEquals(BigDecimal("150.00"), returnedListing.price.amount)
    }

    @Test
    fun `PUT api listings id with non-existent ID returns 404`() = testApplication {
        // Arrange
        val client = createClientWithJson()
        val nonExistentId = UUID.randomUUID()

        val updatedListing = Listing(
            listingId = nonExistentId,
            name = "Updated Item",
            description = "Updated description",
            price = Price(currency = "USD", amount = BigDecimal("150.00")),
            category = "Music",
            location = Location(
                country = "US",
                municipality = "Austin",
                geohash = "dr5regw"
            )
        )

        // Act
        val response = client.put("/api/listings/$nonExistentId") {
            contentType(ContentType.Application.Json)
            setBody(updatedListing)
        }

        // Assert
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `DELETE api listings id removes listing and returns 204`() = testApplication {
        // Arrange
        val client = createClientWithJson()
        val listingId = createTestListing("Item to Delete", BigDecimal("50.00"))

        // Act
        val response = client.delete("/api/listings/$listingId")

        // Assert
        assertEquals(HttpStatusCode.NoContent, response.status)

        // Verify item is deleted
        val getResponse = client.get("/api/listings/$listingId")
        assertEquals(HttpStatusCode.NotFound, getResponse.status)
    }

    @Test
    fun `POST api listings search returns filtered results`() = testApplication {
        // Arrange
        val client = createClientWithJson()
        
        // Create test listings with different categories
        createTestListing("Guitar", BigDecimal("299.99"), "Music")
        createTestListing("Laptop", BigDecimal("999.99"), "Electronics")
        createTestListing("Piano", BigDecimal("1999.99"), "Music")

        val searchRequest = SearchRequest(
            filters = listOf(
                SearchFilter(
                    field = "category",
                    operator = FilterOperator.EQUALS,
                    value = "Music"
                )
            ),
            page = 1,
            pageSize = 10
        )

        // Act
        val response = client.post("/api/listings/search") {
            contentType(ContentType.Application.Json)
            setBody(searchRequest)
        }

        // Assert
        assertEquals(HttpStatusCode.OK, response.status)
        
        val searchResponse: SearchResponse = response.body()
        assertEquals(2, searchResponse.totalItems) // Should find 2 Music items
        assertEquals(2, searchResponse.items.size)
        assertEquals(1, searchResponse.appliedFilters.size)
        assertTrue(searchResponse.items.all { it.category == "Music" })
    }

    private fun createTestListing(
        name: String, 
        amount: BigDecimal, 
        category: String = "Electronics"
    ): UUID {
        val listingId = UUID.randomUUID()
        
        dataSource.connection.use { connection ->
            val sql = """
                INSERT INTO listings (
                    listing_id, name, description, price_currency, price_amount, 
                    category, location_country, location_municipality, location_geohash
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()
            
            connection.prepareStatement(sql).use { statement ->
                statement.setObject(1, listingId)
                statement.setString(2, name)
                statement.setString(3, "Test description for $name")
                statement.setString(4, "USD")
                statement.setBigDecimal(5, amount)
                statement.setString(6, category)
                statement.setString(7, "US")
                statement.setString(8, "Austin")
                statement.setString(9, "dr5regw")
                statement.executeUpdate()
            }
        }
        
        return listingId
    }

    private fun ApplicationTestBuilder.createClientWithJson(): HttpClient {
        return createClient {
            install(ContentNegotiation) {
                jackson {
                    registerModule(KotlinModule.Builder().build())
                }
            }
        }
    }
}