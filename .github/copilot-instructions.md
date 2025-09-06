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
| ListingId | Unique identifier for the listing |
| Name | The name/title of the listing |
| Description | Detailed description of the listing |
| Price | Object containing currency and amount fields |
| Category | The category the listing belongs to |

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

### Implementation Guidelines

- Follow RESTful API design principles
- Implement proper error handling and validation
- Use appropriate HTTP status codes
- Include basic API documentation (e.g., Swagger/OpenAPI)
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
