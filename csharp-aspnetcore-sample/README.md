# C# ASP.NET Core Marketplace API with PostgreSQL

This is a C# ASP.NET Core implementation of the marketplace API using the Repository pattern with Dapper and PostgreSQL for data persistence.

## Features

- Full CRUD operations for product listings
- PostgreSQL database with Dapper micro-ORM
- Repository pattern for clean separation of concerns
- Proper validation and error handling
- OpenAPI/Swagger documentation
- Pagination support

## Prerequisites

- .NET 9.0 SDK
- Docker and Docker Compose (for easy PostgreSQL setup)
- Alternatively: PostgreSQL 15+ if you prefer manual installation

## Quick Start

1. **Clone and navigate to the root directory:**
   ```bash
   cd api-templates
   ```

2. **Start the PostgreSQL database (from root directory):**
   ```bash
   docker-compose up -d
   ```

3. **Navigate to the C# project:**
   ```bash
   cd csharp-aspnetcore-sample
   ```

3. **Run the API:**
   ```bash
   dotnet run
   ```

4. **Access the API:**
   - Swagger UI: https://localhost:5001/swagger
   - API Base URL: https://localhost:5001/api/listings

5. **Stop everything:**
   ```bash
   # Stop the API with Ctrl+C
   # Stop the database
   docker-compose down
   ```

## Database Setup

### Option 1: Using Docker Compose (Recommended for local development)

1. **Start PostgreSQL with Docker Compose (from the root api-templates directory):**
   ```bash
   cd api-templates
   docker-compose up -d
   ```
   This will:
   - Start a PostgreSQL 15 container
   - Create the `marketplace` database
   - Automatically run the schema script from the shared `database/schema.sql`
   - Expose PostgreSQL on port 5432

2. **Stop the database:**
   ```bash
   docker-compose down
   ```

3. **Remove volumes (if you want to reset the database):**
   ```bash
   docker-compose down -v
   ```

### Option 2: Manual PostgreSQL Installation

1. **Create a PostgreSQL database:**
   ```sql
   CREATE DATABASE marketplace;
   CREATE DATABASE marketplace_dev; -- for development
   ```

2. **Run the database schema:**
   Execute the SQL script located in the shared `../database/schema.sql` against your PostgreSQL database:
   ```bash
   psql -U postgres -d marketplace -f ../database/schema.sql
   psql -U postgres -d marketplace_dev -f ../database/schema.sql
   ```

## Configuration

1. **Update connection strings** in `appsettings.json` and `appsettings.Development.json`:
   ```json
   {
     "ConnectionStrings": {
       "DefaultConnection": "Host=localhost;Database=marketplace;Username=your_username;Password=your_password"
     }
   }
   ```

2. **Environment-specific configuration:**
   - Production: Update `appsettings.json`
   - Development: Update `appsettings.Development.json`

## Architecture

### Repository Pattern Implementation

The application uses the Repository pattern to abstract data access:

- **`IListingRepository`**: Interface defining repository operations
- **`ListingRepository`**: Implementation using Dapper for PostgreSQL operations
- **Dependency Injection**: Repository is registered as a scoped service

### Key Components

1. **Models** (`Models/`):
   - `Listing`: Main entity with validation attributes
   - `Price`: Value object for pricing information
   - `Location`: Value object for geographic information
   - `Category`: Enum for listing categories

2. **Repositories** (`Repositories/`):
   - `IListingRepository`: Repository interface
   - `ListingRepository`: Dapper-based PostgreSQL implementation

3. **Controllers** (`Controllers/`):
   - `ListingsController`: RESTful API endpoints

## Running the Application

1. **Restore dependencies:**
   ```bash
   dotnet restore
   ```

2. **Build the project:**
   ```bash
   dotnet build
   ```

3. **Run the application:**
   ```bash
   dotnet run
   ```

The API will be available at:
- HTTP: `http://localhost:5000`
- HTTPS: `https://localhost:5001`
- Swagger UI: `https://localhost:5001/swagger`

## API Endpoints

### Create Listing
```bash
POST /api/listings
Content-Type: application/json

{
  "name": "Vintage Guitar",
  "description": "1970s Fender Stratocaster in excellent condition",
  "price": {
    "currency": "USD",
    "amount": 1299.99
  },
  "category": "Music",
  "location": {
    "country": "US",
    "municipality": "Austin",
    "geohash": "dr5regw"
  }
}
```

### Get All Listings (with pagination)
```bash
GET /api/listings?page=1&pageSize=10
```

### Get Single Listing
```bash
GET /api/listings/{id}
```

### Update Listing
```bash
PUT /api/listings/{id}
Content-Type: application/json

{
  "listingId": "{id}",
  "name": "Updated Vintage Guitar",
  "description": "1970s Fender Stratocaster in mint condition",
  "price": {
    "currency": "USD",
    "amount": 1399.99
  },
  "category": "Music",
  "location": {
    "country": "US",
    "municipality": "Austin",
    "geohash": "dr5regw"
  }
}
```

### Delete Listing
```bash
DELETE /api/listings/{id}
```

## Database Schema

The PostgreSQL schema includes:

- **listings table**: Main table for storing listing data
- **UUID primary key**: Using PostgreSQL UUID extension
- **JSON-like storage**: Complex objects (Price, Location) stored as separate columns
- **Indexes**: Performance optimization for common queries
- **Constraints**: Data validation at database level
- **Triggers**: Automatic timestamp updates

## Development

### Adding New Features

1. Update the database schema in the shared `../database/schema.sql`
2. Modify the `Listing` model if needed
3. Update repository interface and implementation
4. Modify controller endpoints
5. Test the changes

### Testing

You can test the API using:
- Swagger UI at `/swagger`
- Postman or similar tools
- The provided `.http` file in VS Code

## Production Deployment

1. Update connection strings for your production database
2. Ensure PostgreSQL server is properly configured
3. Run database migrations/schema
4. Deploy the application using your preferred method (Docker, Azure, AWS, etc.)

## Docker Support

### PostgreSQL with Docker Compose

The project uses a shared `docker-compose.yml` file in the root directory for easy PostgreSQL setup:

```yaml
# ../docker-compose.yml
services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: marketplace
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./database/schema.sql:/docker-entrypoint-initdb.d/schema.sql
```

**Key features:**
- PostgreSQL 15 image
- Automatic database creation (`marketplace`)
- Schema initialization on first run
- Persistent data storage
- Exposed on standard port 5432

### Application Docker Support

The application also includes a `Dockerfile` for containerization. To build and run with Docker:

```bash
# Build the Docker image
docker build -t marketplace-api-csharp-image .

# Run the container
docker run -d -p 8081:80 --name marketplace-api-csharp-container marketplace-api-csharp-image
```

The API will be available at `http://localhost:8081`. You can access the Swagger documentation at `http://localhost:8081/swagger`.

**Note:** Make sure to configure the PostgreSQL connection string to point to an accessible database server when running the application in Docker.

## Project Structure

- `Controllers/` - API controllers
- `Models/` - Data models and DTOs
- `Repositories/` - Repository pattern implementation
- `Properties/` - Launch settings and configuration
- `Program.cs` - Application entry point and DI configuration
- `Dockerfile` - Docker configuration for containerization
- `../database/` - Shared database schema and scripts (root level)
- `../docker-compose.yml` - Shared PostgreSQL container setup (root level)
