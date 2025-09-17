package com.example.models

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SearchModelsTest {

    @Test
    fun `FilterOperator fromString with valid values works correctly`() {
        assertEquals(FilterOperator.CONTAINS, FilterOperator.fromString("contains"))
        assertEquals(FilterOperator.EQUALS, FilterOperator.fromString("equals"))
        assertEquals(FilterOperator.CONTAINS, FilterOperator.fromString("CONTAINS"))
        assertEquals(FilterOperator.EQUALS, FilterOperator.fromString("EQUALS"))
    }

    @Test
    fun `FilterOperator fromString with invalid value throws IllegalArgumentException`() {
        val exception = assertThrows<IllegalArgumentException> {
            FilterOperator.fromString("invalid")
        }
        assertTrue(exception.message!!.contains("Unknown operator: invalid"))
    }

    @Test
    fun `SearchFilter with valid name field and contains operator works`() {
        val filter = SearchFilter(
            field = "name",
            operator = FilterOperator.CONTAINS,
            value = "guitar"
        )

        assertEquals("name", filter.field)
        assertEquals(FilterOperator.CONTAINS, filter.operator)
        assertEquals("guitar", filter.value)
    }

    @Test
    fun `SearchFilter with valid category field and equals operator works`() {
        val filter = SearchFilter(
            field = "category",
            operator = FilterOperator.EQUALS,
            value = "Music"
        )

        assertEquals("category", filter.field)
        assertEquals(FilterOperator.EQUALS, filter.operator)
        assertEquals("Music", filter.value)
    }

    @Test
    fun `SearchFilter with blank field throws IllegalArgumentException`() {
        val exception = assertThrows<IllegalArgumentException> {
            SearchFilter(
                field = "",
                operator = FilterOperator.CONTAINS,
                value = "test"
            )
        }
        assertTrue(exception.message!!.contains("Filter field cannot be blank"))
    }

    @Test
    fun `SearchFilter with unsupported field throws IllegalArgumentException`() {
        val exception = assertThrows<IllegalArgumentException> {
            SearchFilter(
                field = "unsupported",
                operator = FilterOperator.CONTAINS,
                value = "test"
            )
        }
        assertTrue(exception.message!!.contains("Unsupported filter field: unsupported"))
    }

    @Test
    fun `SearchFilter with category field and contains operator throws IllegalArgumentException`() {
        val exception = assertThrows<IllegalArgumentException> {
            SearchFilter(
                field = "category",
                operator = FilterOperator.CONTAINS,
                value = "Music"
            )
        }
        assertTrue(exception.message!!.contains("Category field only supports 'equals' operator"))
    }

    @Test
    fun `SearchFilter with category field and invalid category value throws IllegalArgumentException`() {
        val exception = assertThrows<IllegalArgumentException> {
            SearchFilter(
                field = "category",
                operator = FilterOperator.EQUALS,
                value = "InvalidCategory"
            )
        }
        assertTrue(exception.message!!.contains("Invalid category value"))
    }

    @Test
    fun `SearchFilter with name field and equals operator throws IllegalArgumentException`() {
        val exception = assertThrows<IllegalArgumentException> {
            SearchFilter(
                field = "name",
                operator = FilterOperator.EQUALS,
                value = "test"
            )
        }
        assertTrue(exception.message!!.contains("name field only supports 'contains' operator"))
    }

    @Test
    fun `SearchFilter with description field and non-string value throws IllegalArgumentException`() {
        val exception = assertThrows<IllegalArgumentException> {
            SearchFilter(
                field = "description",
                operator = FilterOperator.CONTAINS,
                value = 123
            )
        }
        assertTrue(exception.message!!.contains("description filter value must be a string"))
    }

    @Test
    fun `SearchRequest with valid data works correctly`() {
        val filters = listOf(
            SearchFilter(
                field = "name",
                operator = FilterOperator.CONTAINS,
                value = "guitar"
            )
        )

        val request = SearchRequest(
            filters = filters,
            page = 1,
            pageSize = 20,
            latitude = 30.2672,
            longitude = -97.7431
        )

        assertEquals(1, request.filters.size)
        assertEquals(1, request.page)
        assertEquals(20, request.pageSize)
        assertEquals(30.2672, request.latitude)
        assertEquals(-97.7431, request.longitude)
    }

    @Test
    fun `SearchRequest with page less than 1 throws IllegalArgumentException`() {
        val exception = assertThrows<IllegalArgumentException> {
            SearchRequest(page = 0, pageSize = 10)
        }
        assertTrue(exception.message!!.contains("Page must be >= 1"))
    }

    @Test
    fun `SearchRequest with pageSize less than 1 throws IllegalArgumentException`() {
        val exception = assertThrows<IllegalArgumentException> {
            SearchRequest(page = 1, pageSize = 0)
        }
        assertTrue(exception.message!!.contains("Page size must be >= 1"))
    }

    @Test
    fun `SearchRequest with pageSize exceeding 50 throws IllegalArgumentException`() {
        val exception = assertThrows<IllegalArgumentException> {
            SearchRequest(page = 1, pageSize = 51)
        }
        assertTrue(exception.message!!.contains("Page size cannot exceed 50 items"))
    }

    @Test
    fun `SearchRequest with invalid latitude throws IllegalArgumentException`() {
        val exception = assertThrows<IllegalArgumentException> {
            SearchRequest(
                page = 1,
                pageSize = 10,
                latitude = 91.0,
                longitude = 0.0
            )
        }
        assertTrue(exception.message!!.contains("Latitude must be between -90 and 90 degrees"))
    }

    @Test
    fun `SearchRequest with invalid longitude throws IllegalArgumentException`() {
        val exception = assertThrows<IllegalArgumentException> {
            SearchRequest(
                page = 1,
                pageSize = 10,
                latitude = 0.0,
                longitude = 181.0
            )
        }
        assertTrue(exception.message!!.contains("Longitude must be between -180 and 180 degrees"))
    }

    @Test
    fun `SearchRequest with only latitude throws IllegalArgumentException`() {
        val exception = assertThrows<IllegalArgumentException> {
            SearchRequest(
                page = 1,
                pageSize = 10,
                latitude = 30.0,
                longitude = null
            )
        }
        assertTrue(exception.message!!.contains("Both latitude and longitude must be provided together"))
    }

    @Test
    fun `SearchRequest with only longitude throws IllegalArgumentException`() {
        val exception = assertThrows<IllegalArgumentException> {
            SearchRequest(
                page = 1,
                pageSize = 10,
                latitude = null,
                longitude = -97.0
            )
        }
        assertTrue(exception.message!!.contains("Both latitude and longitude must be provided together"))
    }

    @Test
    fun `SearchResponse with valid data works correctly`() {
        val filters = listOf(
            SearchFilter(
                field = "name",
                operator = FilterOperator.CONTAINS,
                value = "guitar"
            )
        )

        val response = SearchResponse(
            items = emptyList(),
            totalItems = 0,
            page = 1,
            pageSize = 10,
            appliedFilters = filters
        )

        assertEquals(0, response.items.size)
        assertEquals(0, response.totalItems)
        assertEquals(1, response.page)
        assertEquals(10, response.pageSize)
        assertEquals(1, response.appliedFilters.size)
    }
}