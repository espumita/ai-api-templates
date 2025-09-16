# C# ASP.NET Core Sample Tests

This project contains comprehensive unit and integration tests for the `csharp-aspnetcore-sample-api` project using NUnit 3.

## Test Structure

The test project is organized into the following directories:

- **Controllers/** - Tests for API controllers
  - `ListingsControllerTests.cs` - Tests for the ListingsController endpoints
  
- **Services/** - Tests for business logic services  
  - `SortingServiceTests.cs` - Tests for the sorting functionality
  
- **Models/** - Tests for data models and validation
  - `ListingValidationTests.cs` - Tests for model validation attributes
  
- **Utilities/** - Tests for utility classes
  - `GeohashUtilityTests.cs` - Tests for geohash decoding and distance calculations
  
- **Integration/** - End-to-end integration tests
  - `ListingsApiIntegrationTests.cs` - Full API integration tests

## Testing Framework and Libraries

- **NUnit 3** - Primary testing framework
- **FluentAssertions** - Fluent assertion library for better test readability
- **Moq** - Mocking framework for isolating dependencies
- **Microsoft.AspNetCore.Mvc.Testing** - ASP.NET Core testing utilities for integration tests

## Running the Tests

### Prerequisites
- .NET 9.0 SDK
- All project dependencies restored

### Run All Tests
```bash
cd csharp-aspnetcore-sample
dotnet test
```

### Run Specific Test Categories
```bash
# Run only unit tests (exclude integration tests)
dotnet test --filter "FullyQualifiedName!~Integration"

# Run only integration tests
dotnet test --filter "FullyQualifiedName~Integration"

# Run tests with verbose output
dotnet test --verbosity normal
```

### Run Tests with Coverage
```bash
# Install coverage tool (one time)
dotnet tool install --global dotnet-coverage

# Run tests with coverage
dotnet-coverage collect dotnet test
```

## Test Coverage

The tests cover:

### Unit Tests
- **Controller Logic**: Validation, error handling, response formatting
- **Business Logic**: Sorting algorithms, distance calculations  
- **Data Validation**: Model validation attributes and custom validation
- **Utilities**: Geohash decoding, distance calculations

### Integration Tests  
- **API Endpoints**: Full HTTP request/response cycle
- **JSON Serialization**: Proper serialization of complex objects
- **Error Scenarios**: Bad requests, not found, validation errors
- **Swagger Documentation**: Ensures documentation endpoint is accessible

## Mock Strategy

The tests use Moq to create mock implementations of:
- `IListingRepository` - Data access layer
- HTTP clients for integration testing

This allows testing business logic in isolation from external dependencies like databases.

## Best Practices Demonstrated

- **AAA Pattern** - Arrange, Act, Assert structure in all tests
- **Descriptive Test Names** - Clear test method names that describe the scenario
- **Isolated Tests** - Each test is independent and can run in any order
- **Comprehensive Coverage** - Both happy path and error scenarios
- **Realistic Test Data** - Using valid geohashes, coordinates, and business data

## Adding New Tests

When adding new tests:

1. Follow the existing naming conventions
2. Use the AAA pattern (Arrange, Act, Assert)
3. Include both success and failure scenarios
4. Use FluentAssertions for readable assertions
5. Mock external dependencies with Moq
6. Add integration tests for new API endpoints

## Troubleshooting

### Common Issues

1. **Build Errors**: Ensure the main API project builds successfully first
2. **Test Failures**: Check that mock setups match expected method calls
3. **Integration Test Failures**: Verify the test web application factory configuration

### Debug Tests

To debug tests in Visual Studio Code:
1. Open the test file
2. Set breakpoints
3. Use "Debug Test" option in the test explorer or code lens