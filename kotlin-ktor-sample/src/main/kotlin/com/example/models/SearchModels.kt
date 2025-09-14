package com.example.models

import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.annotation.JsonCreator

enum class FilterOperator(@JsonValue val value: String) {
    CONTAINS("contains"),
    EQUALS("equals");
    
    companion object {
        @JsonCreator @JvmStatic
        fun fromString(value: String): FilterOperator {
            return values().find { it.value == value.lowercase() }
                ?: throw IllegalArgumentException("Unknown operator: $value. Supported operators: contains, equals")
        }
    }
}

data class SearchFilter(
    val field: String,
    val operator: FilterOperator,
    val value: Any
) {
    init {
        require(field.isNotBlank()) { "Filter field cannot be blank" }
        require(SUPPORTED_FIELDS.contains(field)) { "Unsupported filter field: $field. Supported fields: ${SUPPORTED_FIELDS.joinToString(", ")}" }
        
        when (field) {
            "category" -> {
                require(operator == FilterOperator.EQUALS) { "Category field only supports 'equals' operator" }
                require(value is String && Listing.VALID_CATEGORIES.contains(value)) { 
                    "Invalid category value. Must be one of: ${Listing.VALID_CATEGORIES.joinToString(", ")}" 
                }
            }
            "name", "description", "location.country", "location.municipality" -> {
                require(operator == FilterOperator.CONTAINS) { "$field field only supports 'contains' operator" }
                require(value is String) { "$field filter value must be a string" }
            }
        }
    }

    companion object {
        val SUPPORTED_FIELDS = setOf(
            "name",
            "description", 
            "category",
            "location.country",
            "location.municipality"
        )
    }
}

data class SearchRequest(
    val filters: List<SearchFilter> = emptyList(),
    val page: Int = 1,
    val pageSize: Int = 10
) {
    init {
        require(page >= 1) { "Page must be >= 1" }
        require(pageSize >= 1) { "Page size must be >= 1" }
        require(pageSize <= 50) { "Page size cannot exceed 50 items" }
    }
}

data class SearchResponse(
    val items: List<Listing>,
    val totalItems: Int,
    val page: Int,
    val pageSize: Int,
    val appliedFilters: List<SearchFilter>
)