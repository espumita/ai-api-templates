using System.Net;
using System.Text;
using csharp_aspnetcore_sample.Models;
using csharp_aspnetcore_sample.Repositories;
using Microsoft.AspNetCore.Mvc.Testing;
using Microsoft.Extensions.DependencyInjection;

namespace csharp_aspnetcore_sample_tests.Integration;

[TestFixture]
public class ListingsApiIntegrationTests {
    [OneTimeSetUp]
    public void OneTimeSetUp() {
        _mockRepository = new Mock<IListingRepository>();

        _factory = new WebApplicationFactory<Program>()
            .WithWebHostBuilder(builder => {
                builder.ConfigureServices(services => {
                    // Remove the real repository registration
                    ServiceDescriptor? descriptor = services.SingleOrDefault(
                        d => d.ServiceType == typeof(IListingRepository));
                    if (descriptor != null) {
                        services.Remove(descriptor);
                    }

                    // Add the mock repository
                    services.AddSingleton(_mockRepository.Object);
                });
            });

        _client = _factory.CreateClient();
    }

    [OneTimeTearDown]
    public void OneTimeTearDown() {
        _client?.Dispose();
        _factory?.Dispose();
    }

    [SetUp]
    public void Setup() {
        _mockRepository.Reset();
    }

    private WebApplicationFactory<Program> _factory = null!;
    private HttpClient _client = null!;
    private Mock<IListingRepository> _mockRepository = null!;

    [Test]
    public async Task GetListings_ReturnsSuccessStatusCode() {
        // Arrange
        Listing sampleListing = CreateSampleListing();
        _mockRepository.Setup(r => r.GetAllAsync(1, 10, null, null))
            .ReturnsAsync(new List<Listing> { sampleListing });
        _mockRepository.Setup(r => r.GetTotalCountAsync())
            .ReturnsAsync(1);

        // Act
        HttpResponseMessage response = await _client.GetAsync("/api/listings?page=1&pageSize=10");

        // Assert
        response.IsSuccessStatusCode.Should().BeTrue();
        response.StatusCode.Should().Be(HttpStatusCode.OK);

        string content = await response.Content.ReadAsStringAsync();
        PaginatedListingsResponse? result = JsonSerializer.Deserialize<PaginatedListingsResponse>(content,
            new JsonSerializerOptions {
                PropertyNameCaseInsensitive = true, Converters = { new JsonStringEnumConverter() }
            });

        result.Should().NotBeNull();
        result!.Items.Should().HaveCount(1);
        result.TotalItems.Should().Be(1);
    }

    [Test]
    public async Task GetListings_WithInvalidPageSize_ReturnsBadRequest() {
        // Act
        HttpResponseMessage? response = await _client.GetAsync("/api/listings?page=1&pageSize=100");

        // Assert
        response.StatusCode.Should().Be(HttpStatusCode.BadRequest);
    }

    [Test]
    public async Task GetListing_WithExistingId_ReturnsListing() {
        // Arrange
        Listing sampleListing = CreateSampleListing();
        _mockRepository.Setup(r => r.GetByIdAsync(sampleListing.ListingId))
            .ReturnsAsync(sampleListing);

        // Act
        HttpResponseMessage response = await _client.GetAsync($"/api/listings/{sampleListing.ListingId}");

        // Assert
        response.IsSuccessStatusCode.Should().BeTrue();

        string content = await response.Content.ReadAsStringAsync();
        Listing? result = JsonSerializer.Deserialize<Listing>(content,
            new JsonSerializerOptions {
                PropertyNameCaseInsensitive = true, Converters = { new JsonStringEnumConverter() }
            });

        result.Should().NotBeNull();
        result!.ListingId.Should().Be(sampleListing.ListingId);
        result.Name.Should().Be(sampleListing.Name);
    }

    [Test]
    public async Task GetListing_WithNonExistentId_ReturnsNotFound() {
        // Arrange
        var nonExistentId = Guid.NewGuid();
        _mockRepository.Setup(r => r.GetByIdAsync(nonExistentId))
            .ReturnsAsync((Listing?)null);

        // Act
        HttpResponseMessage? response = await _client.GetAsync($"/api/listings/{nonExistentId}");

        // Assert
        response.StatusCode.Should().Be(HttpStatusCode.NotFound);
    }

    [Test]
    public async Task PostListing_WithValidListing_ReturnsCreated() {
        // Arrange
        Listing newListing = CreateSampleListing();
        _mockRepository.Setup(r => r.CreateAsync(It.IsAny<Listing>()))
            .ReturnsAsync(newListing);

        string json = JsonSerializer.Serialize(newListing,
            new JsonSerializerOptions { Converters = { new JsonStringEnumConverter() } });
        var content = new StringContent(json, Encoding.UTF8, "application/json");

        // Act
        HttpResponseMessage response = await _client.PostAsync("/api/listings", content);

        // Assert
        response.StatusCode.Should().Be(HttpStatusCode.Created);
        response.Headers.Location.Should().NotBeNull();

        string responseContent = await response.Content.ReadAsStringAsync();
        Listing? result = JsonSerializer.Deserialize<Listing>(responseContent,
            new JsonSerializerOptions {
                PropertyNameCaseInsensitive = true, Converters = { new JsonStringEnumConverter() }
            });

        result.Should().NotBeNull();
        result!.Name.Should().Be(newListing.Name);
    }

    [Test]
    public async Task PostSearchListings_WithValidRequest_ReturnsResults() {
        // Arrange
        Listing sampleListing = CreateSampleListing();
        var searchRequest = new FilterRequest {
            Filters = new List<Filter> { new() { Field = "name", Operator = "contains", Value = "test" } },
            Page = 1,
            PageSize = 10
        };

        _mockRepository.Setup(r => r.SearchAsync(
                It.IsAny<List<Filter>>(),
                It.IsAny<int>(),
                It.IsAny<int>(),
                It.IsAny<double?>(),
                It.IsAny<double?>()))
            .ReturnsAsync((new List<Listing> { sampleListing }, 1));

        string json = JsonSerializer.Serialize(searchRequest);
        var content = new StringContent(json, Encoding.UTF8, "application/json");

        // Act
        HttpResponseMessage response = await _client.PostAsync("/api/listings/search", content);

        // Assert
        response.IsSuccessStatusCode.Should().BeTrue();

        string responseContent = await response.Content.ReadAsStringAsync();
        SearchResponse? result = JsonSerializer.Deserialize<SearchResponse>(responseContent,
            new JsonSerializerOptions {
                PropertyNameCaseInsensitive = true, Converters = { new JsonStringEnumConverter() }
            });

        result.Should().NotBeNull();
        result!.Items.Should().HaveCount(1);
        result.AppliedFilters.Should().HaveCount(1);
    }

    [Test]
    public async Task PostSearchListings_WithInvalidField_ReturnsBadRequest() {
        // Arrange
        var searchRequest = new FilterRequest {
            Filters = new List<Filter> { new() { Field = "invalidField", Operator = "contains", Value = "test" } }
        };

        string json = JsonSerializer.Serialize(searchRequest);
        var content = new StringContent(json, Encoding.UTF8, "application/json");

        // Act
        HttpResponseMessage response = await _client.PostAsync("/api/listings/search", content);

        // Assert
        response.StatusCode.Should().Be(HttpStatusCode.BadRequest);
    }

    [Test]
    public async Task SwaggerEndpoint_IsAccessible() {
        // Act
        HttpResponseMessage? response = await _client.GetAsync("/swagger/index.html");

        // Assert
        response.IsSuccessStatusCode.Should().BeTrue();
    }

    private static Listing CreateSampleListing() {
        return new Listing {
            ListingId = Guid.NewGuid(),
            Name = "Integration Test Listing",
            Description = "This is a test listing for integration tests",
            Price = new Price { Currency = "USD", Amount = 199.99m },
            Category = Category.Electronics,
            Location = new Location { Country = "US", Municipality = "Austin", Geohash = "dr5regw" }
        };
    }
}
