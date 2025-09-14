# API Templates - Marketplace Application

This repository contains example projects implementing a simple marketplace API in different programming languages. Currently, it includes:

- C# ASP.NET Core implementation (`csharp-aspnetcore-sample/`)
- Kotlin Ktor implementation (`kotlin-ktor-sample/`)

## Project Overview

The goal is to create a minimal marketplace API where users can create and manage listings. Each implementation should provide the same functionality while following the idioms and best practices of its respective language and framework.

### Core Features

#### 1. Listing CRUD Operations

The API should support full CRUD (Create, Read, Update, Delete) operations for listings with the following fields:

| Field | Description |
|-------|-------------|
| ListingId | Unique identifier for the listing (must be a GUID/UUID) |
| Name | The name/title of the listing |
| Description | Detailed description of the listing |
| Price | Object containing currency and amount fields |
| Category | Must be one of: "Electronics", "Fashion", "Home & Garden", "Motors", "Collectibles & Art", "Sporting Goods", "Toys & Hobbies", "Business & Industrial", "Music", "Health & Beauty", "Books", "Cameras & Photo", "Computers, Tablets & Networking", "Cell Phones & Accessories", "Video Games & Consoles" |
| Location | Object containing country, municipality, and geohash fields for geographic location |

#### Required API Endpoints

Each implementation should provide the following endpoints with their respective response codes:

1. **Create Listing**
   - POST /api/listings
   - Creates a new listing
   - Response codes:
     - 201 Created: Listing created successfully
     - 400 Bad Request: Invalid listing data

2. **Get All Listings**
   - GET /api/listings
   - Returns all available listings
   - Should support pagination with a maximum limit of 50 items per page
   - Response codes:
     - 200 OK: Successful response with listings
     - 400 Bad Request: Invalid page or pageSize parameters

3. **Get Single Listing**
   - GET /api/listings/{id}
   - Returns details for a specific listing
   - Response codes:
     - 200 OK: Successful response with listing details
     - 404 Not Found: Listing not found

4. **Update Listing**
   - PUT /api/listings/{id}
   - Updates an existing listing
   - Response codes:
     - 200 OK: Listing updated successfully with the updated listing object
     - 400 Bad Request: Invalid data or ID mismatch
     - 404 Not Found: Listing not found

5. **Delete Listing**
   - DELETE /api/listings/{id}
   - Removes a listing
   - Response codes:
     - 204 No Content: Listing deleted successfully
     - 404 Not Found: Listing not found

#### 2. Advanced Listing Search and Filtering

The API should provide advanced search and filtering capabilities through a dedicated endpoint that allows clients to apply multiple filters with different operators to various fields.

6. **Search and Filter Listings**
   - POST /api/listings/search
   - Returns filtered listings with pagination support (maximum 50 items per page)
   - Supports extensible filtering system with operators
   - Response codes:
     - 200 OK: Successful response with filtered results
     - 400 Bad Request: Invalid filter criteria or operators

**Filter Structure:**

The filtering system uses a flexible structure where each filter consists of:
- `field`: The property to filter on
- `operator`: The comparison operator to apply
- `value`: The value to compare against

**Supported Filter Fields:**
- `name`: Listing name
- `description`: Listing description
- `category`: Listing category (exact match)
- `location.country`: Location country
- `location.municipality`: Location municipality

**Supported Operators:**
- `contains`: Text contains substring (case-insensitive)

**Filter Request Body Structure:**
```json
{
  "filters": [
    {
      "field": "string",
      "operator": "string",
      "value": "any"
    }
  ],
  "page": 1,
  "pageSize": 10
}
```

#### API Examples

Here are examples of how to interact with the API using curl commands:

1. **Create Listing Example**
```bash
curl -X POST http://localhost:8080/api/listings `
  -H "Content-Type: application/json" `
  -d '{
    "name": "Vintage Guitar",
    "description": "1970s Fender Stratocaster in excellent condition",
    "price": {
      "currency": "USD",
      "amount": 1299.99
    },
    "category": "Music",
    "location": {
      "country": "United States",
      "municipality": "Austin",
      "geohash": "dr5regw"
    }
  }'
```
Response (201 Created):
```json
{
  "listingId": "123e4567-e89b-12d3-a456-426614174000",
  "name": "Vintage Guitar",
  "description": "1970s Fender Stratocaster in excellent condition",
  "price": {
    "currency": "USD",
    "amount": 1299.99
  },
  "category": "Music",
  "location": {
    "country": "United States",
    "municipality": "Austin",
    "geohash": "dr5regw"
  }
}
```

2. **Get All Listings Example**
```bash
# Get all listings with pagination
curl -X GET "http://localhost:8080/api/listings?page=1&pageSize=10"

```
Response (200 OK):
```json
{
  "items": [
    {
      "listingId": "123e4567-e89b-12d3-a456-426614174000",
      "name": "Vintage Guitar",
      "description": "1970s Fender Stratocaster in excellent condition",
      "price": {
        "currency": "USD",
        "amount": 1299.99
      },
      "category": "Music",
      "location": {
        "country": "United States",
        "municipality": "Austin",
        "geohash": "dr5regw"
      }
    }
  ],
  "totalItems": 1,
  "page": 1,
  "pageSize": 10
}
```

3. **Get Single Listing Example**
```bash
curl -X GET http://localhost:8080/api/listings/123e4567-e89b-12d3-a456-426614174000
```
Response (200 OK):
```json
{
  "listingId": "123e4567-e89b-12d3-a456-426614174000",
  "name": "Vintage Guitar",
  "description": "1970s Fender Stratocaster in excellent condition",
  "price": {
    "currency": "USD",
    "amount": 1299.99
  },
  "category": "Music",
  "location": {
    "country": "United States",
    "municipality": "Austin",
    "geohash": "dr5regw"
  }
}
```

4. **Update Listing Example**
```bash
curl -X PUT http://localhost:8080/api/listings/123e4567-e89b-12d3-a456-426614174000 `
  -H "Content-Type: application/json" `
  -d '{
    "name": "Vintage Guitar",
    "description": "1970s Fender Stratocaster in mint condition",
    "price": {
      "currency": "USD",
      "amount": 1399.99
    },
    "category": "Music",
    "location": {
      "country": "United States",
      "municipality": "Austin",
      "geohash": "dr5regw"
    }
  }'
```
Response (200 OK):
```json
{
  "listingId": "123e4567-e89b-12d3-a456-426614174000",
  "name": "Vintage Guitar",
  "description": "1970s Fender Stratocaster in mint condition",
  "price": {
    "currency": "USD",
    "amount": 1399.99
  },
  "category": "Music",
  "location": {
    "country": "United States",
    "municipality": "Austin",
    "geohash": "dr5regw"
  }
}
```

5. **Delete Listing Example**
```bash
curl -X DELETE http://localhost:8080/api/listings/123e4567-e89b-12d3-a456-426614174000
```
Response (204 No Content)

6. **Search and Filter Listings Examples**

**Basic Text Search:**
```bash
curl -X POST http://localhost:8080/api/listings/search `
  -H "Content-Type: application/json" `
  -d '{
    "filters": [
      {
        "field": "name",
        "operator": "contains",
        "value": "guitar"
      }
    ],
    "page": 1,
    "pageSize": 10
  }'
```

**Multiple Filters with Price Range:**
```bash
curl -X POST http://localhost:8080/api/listings/search `
  -H "Content-Type: application/json" `
  -d '{
    "filters": [
      {
        "field": "name",
        "operator": "contains",
        "value": "guitar"
      },
      {
        "field": "category",
        "operator": "equals",
        "value": "Music"
      }
    ],
    "page": 1,
    "pageSize": 20
  }'
```

**Filter Response (200 OK):**
```json
{
  "items": [
    {
      "listingId": "123e4567-e89b-12d3-a456-426614174000",
      "name": "Vintage Guitar",
      "description": "1970s Fender Stratocaster in excellent condition",
      "price": {
        "currency": "USD",
        "amount": 1299.99
      },
      "category": "Music",
      "location": {
        "country": "United States",
        "municipality": "Austin",
        "geohash": "dr5regw"
      }
    }
  ],
  "totalItems": 1,
  "page": 1,
  "pageSize": 10,
  "appliedFilters": [
    {
      "field": "name",
      "operator": "contains",
      "value": "guitar"
    }
  ]
}
```

### Architecture and Data Persistence

#### Data Persistence Layer

Each implementation should include a well-structured data persistence layer to provide a clean separation between the business logic and data access:

- **Data Access Abstraction**: Create appropriate abstractions for data operations following the language/framework conventions
- **Database Integration**: Implement proper database connection and query handling
- **Dependency Injection**: Use dependency injection to manage data access dependencies
- **Transaction Management**: Implement proper transaction handling for data consistency

#### PostgreSQL Database Requirements

- **Database Setup**: Use PostgreSQL as the primary database
- **Connection Management**: Implement proper connection string management and connection pooling
- **Schema Management**: Include database migrations or schema setup scripts
- **Table Structure**: Create appropriate tables with proper indexing for listings
- **Data Types**: Use appropriate PostgreSQL data types (UUID for IDs, JSONB for complex objects like Price and Location)
- **Constraints**: Implement proper database constraints (foreign keys, check constraints for categories, etc.)

### Implementation Guidelines

- Follow RESTful API design principles
- Implement proper error handling and validation
- Use appropriate HTTP status codes
- Implement OpenAPI 3.0 documentation accessible via `/swagger` endpoint
- Follow the language/framework specific best practices and conventions
- Implement proper data validation for all fields
- Use appropriate data types for each field
- **Code Formatting**: Each implementation includes a `.editorconfig` file that defines consistent code formatting rules. All code should be formatted according to these rules to ensure consistency across the project
- **Data Persistence Layer**: Implement a proper data persistence layer to separate data access logic from business logic following language/framework conventions
- **PostgreSQL Database**: Use PostgreSQL as the database implementation for data persistence. Configure connection strings and implement proper database migrations/schema setup

### Future Considerations

While not part of the initial implementation, consider designing the API with these potential future features in mind:

- User authentication and authorization
- Image upload support for listings
- Additional filter fields and operators as needed

## Getting Started

Each implementation includes its own README with specific instructions for:
- Setting up the development environment
- Building the project
- Running the application
- Running tests
- API documentation access
