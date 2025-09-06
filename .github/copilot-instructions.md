# API Templates - Marketplace Application

This repository contains example projects implementing a simple marketplace API in different programming languages. Currently, it includes:

- C# ASP.NET Core implementation
- Kotlin Ktor implementation

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

#### Required API Endpoints

Each implementation should provide the following endpoints:

1. **Create Listing**
   - POST /api/listings
   - Creates a new listing

2. **Get All Listings**
   - GET /api/listings
   - Returns all available listings
   - Should support pagination and filtering

3. **Get Single Listing**
   - GET /api/listings/{id}
   - Returns details for a specific listing

4. **Update Listing**
   - PUT /api/listings/{id}
   - Updates an existing listing

5. **Delete Listing**
   - DELETE /api/listings/{id}
   - Removes a listing

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
    "category": "Music"
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
  "category": "Music"
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
      "category": "Music"
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
  "category": "Music"
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
    "category": "Music"
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
  "category": "Music"
}
```

5. **Delete Listing Example**
```bash
curl -X DELETE http://localhost:8080/api/listings/123e4567-e89b-12d3-a456-426614174000
```
Response (204 No Content)

### Implementation Guidelines

- Follow RESTful API design principles
- Implement proper error handling and validation
- Use appropriate HTTP status codes
- Implement OpenAPI 3.0 documentation accessible via `/swagger` endpoint
- Follow the language/framework specific best practices and conventions
- Implement proper data validation for all fields
- Use appropriate data types for each field

### Future Considerations

While not part of the initial implementation, consider designing the API with these potential future features in mind:

- User authentication and authorization
- Image upload support for listings
- Search and filtering capabilities

## Getting Started

Each implementation includes its own README with specific instructions for:
- Setting up the development environment
- Building the project
- Running the application
- Running tests
- API documentation access
