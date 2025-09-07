# C# ASP.NET Core Sample API

This is a sample ASP.NET Core API project that demonstrates a marketplace listings service.

## Getting Started

### Prerequisites
- .NET 9.0 SDK
- Docker (for containerized deployment)

### Running Locally
1. Navigate to the project directory
2. Run the application:
   ```bash
   dotnet run
   ```
3. The API will be available at `https://localhost:5001` (HTTPS) or `http://localhost:5000` (HTTP)
4. Access the Swagger documentation at `https://localhost:5001/swagger`

## Docker Support

The application can be run in a Docker container. Here are the commands to build and run the Docker image:

### Clean up existing containers and images (if needed)
```bash
docker stop marketplace-api-csharp-container; docker rm marketplace-api-csharp-container; docker rmi marketplace-api-csharp-image
```

### Build the Docker image
```bash
docker build -t marketplace-api-csharp-image .
```

### Run the container
```bash
docker run -d -p 8081:80 --name marketplace-api-csharp-container marketplace-api-csharp-image
```

The API will be available at `http://localhost:8081`. You can access the Swagger documentation at `http://localhost:8081/swagger`.

**Note:** Make sure you have Docker installed and running on your machine before executing these commands.

## API Endpoints

The API provides endpoints for managing marketplace listings. Refer to the Swagger documentation for detailed information about available endpoints and their usage.

## Project Structure

- `Controllers/` - API controllers
- `Models/` - Data models
- `Properties/` - Launch settings and configuration
- `Program.cs` - Application entry point
- `Dockerfile` - Docker configuration for containerization
