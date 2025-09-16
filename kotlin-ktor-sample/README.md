# Kotlin Ktor Sample API

This is a sample Ktor API project that demonstrates a marketplace listings service using Kotlin and the Ktor framework, with PostgreSQL database persistence.

## Getting Started

### Prerequisites
- JDK 24 or later
- Gradle 9.0 or later (project uses 9.0.0 via wrapper)
- PostgreSQL database
- Docker (for containerized deployment)

### Database Setup

The application uses PostgreSQL for data persistence and shares the database schema with other implementations in this repository.

#### Option 1: Using Shared Docker Compose (Recommended)
1. Navigate to the root directory of the repository:
   ```bash
   cd ../
   ```
2. Start the shared PostgreSQL service:
   ```bash
   docker-compose up -d
   ```
   This will start PostgreSQL with the shared schema automatically loaded from `database/schema.sql`

#### Option 2: Local PostgreSQL Installation
1. Install PostgreSQL on your local machine
2. Create a database named `marketplace`
3. Run the schema SQL script located at `../database/schema.sql`

#### Option 3: Manual Docker PostgreSQL
```bash
docker run --name marketplace-postgres \
  -e POSTGRES_DB=marketplace \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -d postgres:15
```

After the container starts, execute the shared schema:
```bash
docker exec -i marketplace-postgres psql -U postgres -d marketplace < ../database/schema.sql
```

### Environment Variables

Configure the database connection using these environment variables:

| Variable | Default Value | Description |
|----------|---------------|-------------|
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/marketplace` | PostgreSQL JDBC connection string |
| `DATABASE_USERNAME` | `postgres` | Database username |
| `DATABASE_PASSWORD` | `postgres` | Database password |

### Running Locally
1. Navigate to the project directory
2. Set up the database (see Database Setup section)
3. If using the shared Docker Compose, make sure to run it from the root directory:
   ```bash
   cd ../; docker-compose up -d; cd kotlin-ktor-sample
   ```
4. Run the application:
   ```bash
   ./gradlew run
   ```
5. The API will be available at `http://localhost:8080`
6. Access the Swagger documentation at `http://localhost:8080/swagger`

## Data Persistence Architecture

The application implements a clean separation between business logic and data access:

### Repository Pattern
- **`IListingRepository`**: Interface defining data access contract
- **`ListingRepository`**: PostgreSQL implementation using JDBC
- **`DatabaseConfig`**: Database connection configuration with HikariCP connection pooling

### Models
- **`Listing`**: Main entity with UUID-based identification
- **`Price`**: Embedded object for currency and amount
- **`Location`**: Embedded object for geographic data
- **`PaginatedListingsResponse`**: Response wrapper for paginated results

### Database Features
- **Connection Pooling**: Uses HikariCP for efficient connection management
- **UUID Primary Keys**: Uses PostgreSQL UUID extension for unique identifiers
- **Async Operations**: All database operations use Kotlin coroutines
- **Automatic Timestamps**: Tracks creation and update times
- **Database Constraints**: Enforces data integrity at the database level

## Docker Support

The application can be run in a Docker container. Here are the commands to build and run the Docker image:

### Clean up existing containers and images (if needed)
```bash
docker stop marketplace-api-kotlin-container; docker rm marketplace-api-kotlin-container; docker rmi marketplace-api-kotlin-image
```

### Build the Docker image
```bash
docker build -t marketplace-api-kotlin-image .
```

### Run the container
```bash
docker run -d -p 8082:8080 \
  -e DATABASE_URL=jdbc:postgresql://host.docker.internal:5432/marketplace \
  --name marketplace-api-kotlin-container \
  marketplace-api-kotlin-image
```

The API will be available at `http://localhost:8082`. You can access the Swagger documentation at `http://localhost:8082/swagger`.

**Note:** Make sure you have Docker installed and running on your machine, and adjust the database URL to connect to your PostgreSQL instance.

## API Endpoints

The API provides the following endpoints for managing marketplace listings:

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/listings` | Create a new listing |
| GET | `/api/listings` | Get all listings (paginated) |
| GET | `/api/listings/{id}` | Get a specific listing by UUID |
| PUT | `/api/listings/{id}` | Update an existing listing |
| DELETE | `/api/listings/{id}` | Delete a listing |
| POST | `/api/listings/search` | Search and filter listings with advanced criteria |

### Advanced Listing Search and Filtering

The `/api/listings/search` endpoint provides powerful filtering capabilities:

#### Supported Filter Fields
- `name`: Listing name (supports `contains` operator)
- `description`: Listing description (supports `contains` operator)
- `category`: Listing category (supports `equals` operator only)
- `location.country`: Location country (supports `contains` operator)
- `location.municipality`: Location municipality (supports `contains` operator)

#### Supported Operators
- `contains`: Text contains substring (case-insensitive)
- `equals`: Exact match

#### Example Search Requests

**Basic Text Search:**
```json
POST /api/listings/search
{
  "filters": [
    {
      "field": "name",
      "operator": "contains", 
      "value": "guitar"
    }
  ],
  "page": 1,
  "pageSize": 10
}
```

**Multiple Filters:**
```json
POST /api/listings/search
{
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
}
```

**Location-based Search:**
```json
POST /api/listings/search
{
  "filters": [
    {
      "field": "location.country",
      "operator": "contains",
      "value": "US"
    },
    {
      "field": "location.municipality",
      "operator": "contains", 
      "value": "austin"
    }
  ],
  "page": 1,
  "pageSize": 10
}
```

#### Search Response Format
```json
{
  "items": [/* array of matching listings */],
  "totalItems": 25,
  "page": 1,
  "pageSize": 10,
  "appliedFilters": [/* array of applied filters */]
}
```

Refer to the Swagger documentation at `/swagger` for detailed information about request/response schemas and examples.

## Building & Running

To build or run the project, use one of the following Gradle tasks:

| Task                          | Description                                                          |
| -------------------------------|---------------------------------------------------------------------- |
| `./gradlew test`              | Run the tests                                                        |
| `./gradlew build`             | Build everything                                                     |
| `./gradlew buildFatJar`       | Build an executable JAR of the server with all dependencies included |
| `./gradlew run`               | Run the server                                                       |

If the server starts successfully, you'll see the following output:

```
2024-12-04 14:32:45.584 [main] INFO  Application - Application started in 0.303 seconds.
2024-12-04 14:32:45.682 [main] INFO  Application - Responding at http://0.0.0.0:8080
```

## Project Structure

```
kotlin-ktor-sample-api/src/main/kotlin/
├── Application.kt                    # Application entry point and configuration
├── Routing.kt                        # API route definitions
└── com/example/
    ├── database/
    │   └── DatabaseConfig.kt         # Database connection configuration
    ├── models/
    │   ├── Listing.kt                # Core data models and DTOs
    │   └── SearchModels.kt           # Search and filtering models
    ├── repositories/
    │   ├── IListingRepository.kt     # Repository interface
    │   └── ListingRepository.kt      # PostgreSQL repository implementation
    ├── routes/
    │   └── ListingRoutes.kt          # API route handlers
    ├── services/
    │   └── ListingService.kt         # Business logic layer
    └── validation/
        └── ListingValidation.kt      # Input validation logic

../database/
└── schema.sql                       # Shared PostgreSQL database schema

../docker-compose.yml                # Shared PostgreSQL service configuration
```

## Dependencies

### Core Dependencies
- **Ktor**: Web framework for Kotlin
- **PostgreSQL Driver**: Database connectivity
- **HikariCP**: Connection pooling
- **Kotlinx Coroutines**: Asynchronous programming support
- **Jackson**: JSON serialization/deserialization

### Ktor Features

| Feature                       | Description                                                 |
| ------------------------------|-------------------------------------------------------------|
| Content Negotiation           | JSON serialization/deserialization using Jackson           |
| CORS                          | Cross-Origin Resource Sharing support                      |
| OpenAPI                       | API documentation generation                                |
| Swagger UI                    | Interactive API documentation interface                     |
| Routing                       | Structured route definitions and handlers                   |

## Useful Links

- [Ktor Documentation](https://ktor.io/docs/home.html)
- [Ktor GitHub page](https://github.com/ktorio/ktor)
- [Kotlin Documentation](https://kotlinlang.org/docs/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [HikariCP Documentation](https://github.com/brettwooldridge/HikariCP)

