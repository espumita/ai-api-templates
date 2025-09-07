````markdown
# Kotlin Ktor Sample API

This is a sample Ktor API project that demonstrates a marketplace listings service using Kotlin and the Ktor framework.

## Getting Started

### Prerequisites
- JDK 24 or later
- Gradle 9.0 or later (project uses 9.0.0 via wrapper)
- Docker (for containerized deployment)

### Running Locally
1. Navigate to the project directory
2. Run the application:
   ```bash
   ./gradlew run
   ```
3. The API will be available at `http://localhost:8080`
4. Access the Swagger documentation at `http://localhost:8080/swagger`

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
docker run -d -p 8082:8080 --name marketplace-api-kotlin-container marketplace-api-kotlin-image
```

The API will be available at `http://localhost:8082`. You can access the Swagger documentation at `http://localhost:8082/swagger`.

**Note:** Make sure you have Docker installed and running on your machine before executing these commands.

## API Endpoints

The API provides endpoints for managing marketplace listings. Refer to the Swagger documentation for detailed information about available endpoints and their usage.

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

- `src/main/kotlin/` - Kotlin source files
  - `Application.kt` - Application entry point and configuration
  - `Routing.kt` - API route definitions
  - `com/example/` - Package structure for models, routes, and services
- `src/main/resources/` - Configuration files and resources
- `build.gradle.kts` - Build configuration and dependencies
- `Dockerfile` - Docker configuration for containerization

## Features

This project includes the following Ktor features:

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

````

