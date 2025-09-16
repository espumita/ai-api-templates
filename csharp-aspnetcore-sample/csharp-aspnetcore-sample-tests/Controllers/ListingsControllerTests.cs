using csharp_aspnetcore_sample.Controllers;
using csharp_aspnetcore_sample.Models;
using csharp_aspnetcore_sample.Repositories;
using Microsoft.AspNetCore.Mvc;

namespace csharp_aspnetcore_sample_tests.Controllers;

[TestFixture]
public class ListingsControllerTests {
    [SetUp]
    public void Setup() {
        _mockRepository = new Mock<IListingRepository>();
        _controller = new ListingsController(_mockRepository.Object);

        _sampleListing = new Listing {
            ListingId = Guid.NewGuid(),
            Name = "Test Listing",
            Description = "Test Description",
            Price = new Price { Currency = "USD", Amount = 100.50m },
            Category = Category.Electronics,
            Location = new Location { Country = "US", Municipality = "Test City", Geohash = "dr5regw" }
        };
    }

    private Mock<IListingRepository> _mockRepository = null!;
    private ListingsController _controller = null!;
    private Listing _sampleListing = null!;

    [Test]
    public async Task GetListings_WithValidParameters_ReturnsOkResult() {
        // Arrange
        var listings = new List<Listing> { _sampleListing };
        _mockRepository.Setup(r => r.GetAllAsync(1, 10, null, null))
            .ReturnsAsync(listings);
        _mockRepository.Setup(r => r.GetTotalCountAsync())
            .ReturnsAsync(1);

        // Act
        ActionResult<PaginatedListingsResponse> result = await _controller.GetListings();

        // Assert
        result.Result.Should().BeOfType<OkObjectResult>();
        var okResult = result.Result as OkObjectResult;
        var response = okResult!.Value as PaginatedListingsResponse;
        response!.Items.Should().HaveCount(1);
        response.TotalItems.Should().Be(1);
    }

    [Test]
    public async Task GetListings_WithPageSizeOver50_ReturnsBadRequest() {
        // Act
        ActionResult<PaginatedListingsResponse>? result = await _controller.GetListings(1, 51);

        // Assert
        result.Result.Should().BeOfType<BadRequestObjectResult>();
        var badRequestResult = result.Result as BadRequestObjectResult;
        badRequestResult!.Value.Should().Be("Page size cannot exceed 50 items");
    }

    [Test]
    public async Task GetListings_WithInvalidLatitude_ReturnsBadRequest() {
        // Act
        ActionResult<PaginatedListingsResponse>? result = await _controller.GetListings(1, 10, -95, -100);

        // Assert
        result.Result.Should().BeOfType<BadRequestObjectResult>();
        var badRequestResult = result.Result as BadRequestObjectResult;
        badRequestResult!.Value.Should().Be("Latitude must be between -90 and 90 degrees");
    }

    [Test]
    public async Task GetListings_WithOnlyLatitude_ReturnsBadRequest() {
        // Act
        ActionResult<PaginatedListingsResponse>? result = await _controller.GetListings(1, 10, 30.0);

        // Assert
        result.Result.Should().BeOfType<BadRequestObjectResult>();
        var badRequestResult = result.Result as BadRequestObjectResult;
        badRequestResult!.Value.Should()
            .Be("Both latitude and longitude must be provided together for proximity sorting");
    }

    [Test]
    public async Task GetListing_WithExistingId_ReturnsOkResult() {
        // Arrange
        Guid id = _sampleListing.ListingId;
        _mockRepository.Setup(r => r.GetByIdAsync(id))
            .ReturnsAsync(_sampleListing);

        // Act
        ActionResult<Listing>? result = await _controller.GetListing(id);

        // Assert
        result.Result.Should().BeOfType<OkObjectResult>();
        var okResult = result.Result as OkObjectResult;
        var listing = okResult!.Value as Listing;
        listing!.ListingId.Should().Be(id);
    }

    [Test]
    public async Task GetListing_WithNonExistentId_ReturnsNotFound() {
        // Arrange
        var id = Guid.NewGuid();
        _mockRepository.Setup(r => r.GetByIdAsync(id))
            .ReturnsAsync((Listing?)null);

        // Act
        ActionResult<Listing>? result = await _controller.GetListing(id);

        // Assert
        result.Result.Should().BeOfType<NotFoundResult>();
    }

    [Test]
    public async Task CreateListing_WithValidListing_ReturnsCreatedResult() {
        // Arrange
        _mockRepository.Setup(r => r.CreateAsync(_sampleListing))
            .ReturnsAsync(_sampleListing);

        // Act
        ActionResult<Listing>? result = await _controller.CreateListing(_sampleListing);

        // Assert
        result.Result.Should().BeOfType<CreatedAtActionResult>();
        var createdResult = result.Result as CreatedAtActionResult;
        createdResult!.ActionName.Should().Be(nameof(ListingsController.GetListing));
        createdResult.Value.Should().Be(_sampleListing);
    }

    [Test]
    public async Task UpdateListing_WithValidListing_ReturnsOkResult() {
        // Arrange
        Guid id = _sampleListing.ListingId;
        _mockRepository.Setup(r => r.ExistsAsync(id))
            .ReturnsAsync(true);
        _mockRepository.Setup(r => r.UpdateAsync(_sampleListing))
            .ReturnsAsync(_sampleListing);

        // Act
        ActionResult<Listing>? result = await _controller.UpdateListing(id, _sampleListing);

        // Assert
        result.Result.Should().BeOfType<OkObjectResult>();
        var okResult = result.Result as OkObjectResult;
        okResult!.Value.Should().Be(_sampleListing);
    }

    [Test]
    public async Task UpdateListing_WithMismatchedId_ReturnsBadRequest() {
        // Arrange
        var differentId = Guid.NewGuid();

        // Act
        ActionResult<Listing>? result = await _controller.UpdateListing(differentId, _sampleListing);

        // Assert
        result.Result.Should().BeOfType<BadRequestObjectResult>();
        var badRequestResult = result.Result as BadRequestObjectResult;
        badRequestResult!.Value.Should().Be("The ID in the URL must match the ID in the request body");
    }

    [Test]
    public async Task UpdateListing_WithNonExistentId_ReturnsNotFound() {
        // Arrange
        Guid id = _sampleListing.ListingId;
        _mockRepository.Setup(r => r.ExistsAsync(id))
            .ReturnsAsync(false);

        // Act
        ActionResult<Listing>? result = await _controller.UpdateListing(id, _sampleListing);

        // Assert
        result.Result.Should().BeOfType<NotFoundResult>();
    }

    [Test]
    public async Task DeleteListing_WithExistingId_ReturnsNoContent() {
        // Arrange
        var id = Guid.NewGuid();
        _mockRepository.Setup(r => r.DeleteAsync(id))
            .ReturnsAsync(true);

        // Act
        IActionResult? result = await _controller.DeleteListing(id);

        // Assert
        result.Should().BeOfType<NoContentResult>();
    }

    [Test]
    public async Task DeleteListing_WithNonExistentId_ReturnsNotFound() {
        // Arrange
        var id = Guid.NewGuid();
        _mockRepository.Setup(r => r.DeleteAsync(id))
            .ReturnsAsync(false);

        // Act
        IActionResult? result = await _controller.DeleteListing(id);

        // Assert
        result.Should().BeOfType<NotFoundResult>();
    }

    [Test]
    public async Task SearchListings_WithValidRequest_ReturnsOkResult() {
        // Arrange
        var filterRequest = new FilterRequest {
            Filters = new List<Filter> { new() { Field = "name", Operator = "contains", Value = "test" } },
            Page = 1,
            PageSize = 10
        };

        var listings = new List<Listing> { _sampleListing };
        _mockRepository.Setup(r => r.SearchAsync(filterRequest.Filters, 1, 10, null, null))
            .ReturnsAsync((listings, 1));

        // Act
        ActionResult<SearchResponse> result = await _controller.SearchListings(filterRequest);

        // Assert
        result.Result.Should().BeOfType<OkObjectResult>();
        var okResult = result.Result as OkObjectResult;
        var response = okResult!.Value as SearchResponse;
        response!.Items.Should().HaveCount(1);
        response.TotalItems.Should().Be(1);
        response.AppliedFilters.Should().HaveCount(1);
    }

    [Test]
    public async Task SearchListings_WithInvalidField_ReturnsBadRequest() {
        // Arrange
        var filterRequest = new FilterRequest {
            Filters = new List<Filter> { new() { Field = "invalidField", Operator = "contains", Value = "test" } }
        };

        // Act
        ActionResult<SearchResponse> result = await _controller.SearchListings(filterRequest);

        // Assert
        result.Result.Should().BeOfType<BadRequestObjectResult>();
        var badRequestResult = result.Result as BadRequestObjectResult;
        badRequestResult!.Value.ToString().Should().Contain("Invalid filter field: invalidField");
    }

    [Test]
    public async Task SearchListings_WithCategoryAndContainsOperator_ReturnsBadRequest() {
        // Arrange
        var filterRequest = new FilterRequest {
            Filters = new List<Filter> { new() { Field = "category", Operator = "contains", Value = "Electronics" } }
        };

        // Act
        ActionResult<SearchResponse> result = await _controller.SearchListings(filterRequest);

        // Assert
        result.Result.Should().BeOfType<BadRequestObjectResult>();
        var badRequestResult = result.Result as BadRequestObjectResult;
        badRequestResult!.Value.Should().Be("Category field only supports 'equals' operator");
    }
}
