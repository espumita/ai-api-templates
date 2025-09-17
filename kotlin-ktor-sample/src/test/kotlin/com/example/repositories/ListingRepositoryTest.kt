package com.example.repositories

import com.example.database.DatabaseConfig
import com.example.models.*
import org.junit.jupiter.api.*
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.math.BigDecimal
import java.util.*
import javax.sql.DataSource
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@Testcontainers
class ListingRepositoryTest {

    companion object {
        @Container
        val postgres: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:15-alpine")
            .withDatabaseName("marketplace_test")
            .withUsername("test")
            .withPassword("test")

        private lateinit var dataSource: DataSource
        private lateinit var repository: ListingRepository

        @BeforeAll
        @JvmStatic
        fun setUp() {
            postgres.start()
            
            dataSource = DatabaseConfig.createDataSource(
                postgres.jdbcUrl,
                postgres.username,
                postgres.password
            )
            
            createSchema()
            repository = ListingRepository(dataSource)
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
    fun `create saves listing to database and generates new ID`() {
        // Arrange
        val listing = createTestListing("Test Guitar", BigDecimal("299.99"))

        // Act
        val createdListing = repository.create(listing)

        // Assert
        assertNotNull(createdListing.listingId)
        assertEquals("Test Guitar", createdListing.name)
        assertEquals(BigDecimal("299.99"), createdListing.price.amount)
        assertTrue(repository.exists(createdListing.listingId))
    }

    @Test
    fun `getById returns listing when exists`() {
        // Arrange
        val createdListing = repository.create(createTestListing("Test Item", BigDecimal("99.99")))

        // Act
        val retrievedListing = repository.getById(createdListing.listingId)

        // Assert
        assertNotNull(retrievedListing)
        assertEquals(createdListing.listingId, retrievedListing!!.listingId)
        assertEquals("Test Item", retrievedListing.name)
        assertEquals(BigDecimal("99.99"), retrievedListing.price.amount)
    }

    @Test
    fun `getById returns null when listing does not exist`() {
        // Arrange
        val nonExistentId = UUID.randomUUID()

        // Act
        val result = repository.getById(nonExistentId)

        // Assert
        assertNull(result)
    }

    @Test
    fun `getAll returns paginated listings`() {
        // Arrange
        repository.create(createTestListing("Item 1", BigDecimal("100.00")))
        repository.create(createTestListing("Item 2", BigDecimal("200.00")))
        repository.create(createTestListing("Item 3", BigDecimal("300.00")))

        // Act
        val page1 = repository.getAll(page = 1, pageSize = 2)
        val page2 = repository.getAll(page = 2, pageSize = 2)

        // Assert
        assertEquals(2, page1.size)
        assertEquals(1, page2.size)
        
        val totalCount = repository.getTotalCount()
        assertEquals(3, totalCount)
    }

    @Test
    fun `update modifies existing listing`() {
        // Arrange
        val createdListing = repository.create(createTestListing("Original Name", BigDecimal("100.00")))
        val updatedListing = createdListing.copy(
            name = "Updated Name",
            price = Price(currency = "USD", amount = BigDecimal("150.00"))
        )

        // Act
        val result = repository.update(updatedListing)

        // Assert
        assertNotNull(result)
        assertEquals("Updated Name", result!!.name)
        assertEquals(BigDecimal("150.00"), result.price.amount)

        // Verify in database
        val retrievedListing = repository.getById(createdListing.listingId)
        assertEquals("Updated Name", retrievedListing!!.name)
        assertEquals(BigDecimal("150.00"), retrievedListing.price.amount)
    }

    @Test
    fun `update returns null when listing does not exist`() {
        // Arrange
        val nonExistentListing = createTestListing("Non-existent", BigDecimal("100.00"))
            .copy(listingId = UUID.randomUUID())

        // Act
        val result = repository.update(nonExistentListing)

        // Assert
        assertNull(result)
    }

    @Test
    fun `delete removes listing from database`() {
        // Arrange
        val createdListing = repository.create(createTestListing("To Delete", BigDecimal("100.00")))

        // Act
        val result = repository.delete(createdListing.listingId)

        // Assert
        assertTrue(result)
        assertFalse(repository.exists(createdListing.listingId))
        assertNull(repository.getById(createdListing.listingId))
    }

    @Test
    fun `delete returns false when listing does not exist`() {
        // Arrange
        val nonExistentId = UUID.randomUUID()

        // Act
        val result = repository.delete(nonExistentId)

        // Assert
        assertFalse(result)
    }

    @Test
    fun `exists returns true when listing exists`() {
        // Arrange
        val createdListing = repository.create(createTestListing("Existing Item", BigDecimal("100.00")))

        // Act
        val result = repository.exists(createdListing.listingId)

        // Assert
        assertTrue(result)
    }

    @Test
    fun `exists returns false when listing does not exist`() {
        // Arrange
        val nonExistentId = UUID.randomUUID()

        // Act
        val result = repository.exists(nonExistentId)

        // Assert
        assertFalse(result)
    }

    @Test
    fun `searchListings with name filter returns matching items`() {
        // Arrange
        repository.create(createTestListing("Guitar Hero", BigDecimal("50.00"), "Music"))
        repository.create(createTestListing("Piano Master", BigDecimal("100.00"), "Music"))
        repository.create(createTestListing("Guitar Pro", BigDecimal("75.00"), "Music"))
        repository.create(createTestListing("Laptop", BigDecimal("800.00"), "Electronics"))

        val filters = listOf(
            SearchFilter(
                field = "name",
                operator = FilterOperator.CONTAINS,
                value = "guitar"
            )
        )

        // Act
        val results = repository.searchListings(filters, page = 1, pageSize = 10)
        val totalCount = repository.getSearchResultCount(filters)

        // Assert
        assertEquals(2, results.size)
        assertEquals(2, totalCount)
        assertTrue(results.all { it.name.contains("Guitar", ignoreCase = true) })
    }

    @Test
    fun `searchListings with category filter returns matching items`() {
        // Arrange
        repository.create(createTestListing("Guitar", BigDecimal("300.00"), "Music"))
        repository.create(createTestListing("Piano", BigDecimal("1000.00"), "Music"))
        repository.create(createTestListing("Laptop", BigDecimal("800.00"), "Electronics"))
        repository.create(createTestListing("Phone", BigDecimal("500.00"), "Electronics"))

        val filters = listOf(
            SearchFilter(
                field = "category",
                operator = FilterOperator.EQUALS,
                value = "Music"
            )
        )

        // Act
        val results = repository.searchListings(filters, page = 1, pageSize = 10)
        val totalCount = repository.getSearchResultCount(filters)

        // Assert
        assertEquals(2, results.size)
        assertEquals(2, totalCount)
        assertTrue(results.all { it.category == "Music" })
    }

    @Test
    fun `searchListings with multiple filters returns items matching all criteria`() {
        // Arrange
        repository.create(createTestListing("Electric Guitar", BigDecimal("400.00"), "Music"))
        repository.create(createTestListing("Acoustic Guitar", BigDecimal("300.00"), "Music"))
        repository.create(createTestListing("Guitar Case", BigDecimal("50.00"), "Music"))
        repository.create(createTestListing("Piano", BigDecimal("1000.00"), "Music"))
        repository.create(createTestListing("Guitar Laptop Bag", BigDecimal("40.00"), "Electronics"))

        val filters = listOf(
            SearchFilter(
                field = "name",
                operator = FilterOperator.CONTAINS,
                value = "guitar"
            ),
            SearchFilter(
                field = "category",
                operator = FilterOperator.EQUALS,
                value = "Music"
            )
        )

        // Act
        val results = repository.searchListings(filters, page = 1, pageSize = 10)
        val totalCount = repository.getSearchResultCount(filters)

        // Assert
        assertEquals(3, results.size)
        assertEquals(3, totalCount)
        assertTrue(results.all { it.name.contains("Guitar", ignoreCase = true) && it.category == "Music" })
    }

    @Test
    fun `searchListings with pagination works correctly`() {
        // Arrange
        repeat(5) { i ->
            repository.create(createTestListing("Guitar $i", BigDecimal("${100 + i * 50}.00"), "Music"))
        }

        val filters = listOf(
            SearchFilter(
                field = "category",
                operator = FilterOperator.EQUALS,
                value = "Music"
            )
        )

        // Act
        val page1 = repository.searchListings(filters, page = 1, pageSize = 2)
        val page2 = repository.searchListings(filters, page = 2, pageSize = 2)
        val page3 = repository.searchListings(filters, page = 3, pageSize = 2)
        val totalCount = repository.getSearchResultCount(filters)

        // Assert
        assertEquals(2, page1.size)
        assertEquals(2, page2.size)
        assertEquals(1, page3.size)
        assertEquals(5, totalCount)
    }

    private fun createTestListing(
        name: String,
        amount: BigDecimal,
        category: String = "Electronics"
    ): Listing {
        return Listing(
            name = name,
            description = "Test description for $name",
            price = Price(currency = "USD", amount = amount),
            category = category,
            location = Location(
                country = "US",
                municipality = "Austin",
                geohash = "dr5regw"
            )
        )
    }
}