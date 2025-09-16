using csharp_aspnetcore_sample.Models;
using csharp_aspnetcore_sample.Services;

namespace csharp_aspnetcore_sample_tests.Services;

[TestFixture]
public class SortingServiceTests {
    [SetUp]
    public void Setup() {
        _sortingService = new SortingService();

        // Create test listings with different locations and prices
        _testListings = new List<Listing> {
            new() {
                ListingId = Guid.NewGuid(),
                Name = "Expensive Item Far Away",
                Description = "Test",
                Price = new Price { Currency = "USD", Amount = 500.00m },
                Category = Category.Electronics,
                Location = new Location {
                    Country = "US", Municipality = "New York", Geohash = "dr5ru7v"
                } // Far from Austin
            },
            new() {
                ListingId = Guid.NewGuid(),
                Name = "Cheap Item Nearby",
                Description = "Test",
                Price = new Price { Currency = "USD", Amount = 50.00m },
                Category = Category.Electronics,
                Location = new Location { Country = "US", Municipality = "Austin", Geohash = "dr5regw" } // Austin, TX
            },
            new() {
                ListingId = Guid.NewGuid(),
                Name = "Medium Price Item Nearby",
                Description = "Test",
                Price = new Price { Currency = "USD", Amount = 200.00m },
                Category = Category.Electronics,
                Location = new Location { Country = "US", Municipality = "Austin", Geohash = "dr5regw" } // Austin, TX
            }
        };
    }

    private SortingService _sortingService = null!;
    private List<Listing> _testListings = null!;

    [Test]
    public async Task ApplyAllSortingRulesAsync_WithoutLocation_SortsByPriceOnly() {
        // Act
        IEnumerable<Listing> result = await _sortingService.ApplyAllSortingRulesAsync(_testListings, null, null);
        var sortedListings = result.ToList();

        // Assert
        sortedListings.Should().HaveCount(3);
        // Should be sorted by price (ascending)
        sortedListings[0].Price.Amount.Should().Be(50.00m);
        sortedListings[1].Price.Amount.Should().Be(200.00m);
        sortedListings[2].Price.Amount.Should().Be(500.00m);
    }

    [Test]
    public async Task ApplyAllSortingRulesAsync_WithLocation_SortsByDistanceThenPrice() {
        // Arrange - Using Austin, TX coordinates
        double latitude = 30.2672;
        double longitude = -97.7431;

        // Act
        IEnumerable<Listing> result =
            await _sortingService.ApplyAllSortingRulesAsync(_testListings, latitude, longitude);
        var sortedListings = result.ToList();

        // Assert
        sortedListings.Should().HaveCount(3);

        // First two items should be from Austin (same distance), sorted by price
        sortedListings[0].Price.Amount.Should().Be(50.00m); // Cheap item nearby
        sortedListings[1].Price.Amount.Should().Be(200.00m); // Medium price item nearby

        // Last item should be the expensive item from far away
        sortedListings[2].Price.Amount.Should().Be(500.00m); // Expensive item far away
        sortedListings[2].Location.Municipality.Should().Be("New York");
    }

    [Test]
    public async Task ApplyAllSortingRulesAsync_WithInvalidGeohash_HandlesSafely() {
        // Arrange
        var listingWithInvalidGeohash = new Listing {
            ListingId = Guid.NewGuid(),
            Name = "Invalid Geohash Item",
            Description = "Test",
            Price = new Price { Currency = "USD", Amount = 100.00m },
            Category = Category.Electronics,
            Location = new Location { Country = "US", Municipality = "Unknown", Geohash = "invalid" }
        };

        var listings = new List<Listing> { listingWithInvalidGeohash, _testListings[1] };
        double latitude = 30.2672;
        double longitude = -97.7431;

        // Act
        IEnumerable<Listing> result = await _sortingService.ApplyAllSortingRulesAsync(listings, latitude, longitude);
        var sortedListings = result.ToList();

        // Assert
        sortedListings.Should().HaveCount(2);
        // Item with valid geohash should come first (closer)
        sortedListings[0].Location.Geohash.Should().Be("dr5regw");
        // Item with invalid geohash should come last (max distance)
        sortedListings[1].Location.Geohash.Should().Be("invalid");
    }

    [Test]
    public async Task ApplyAllSortingRulesAsync_WithEmptyList_ReturnsEmpty() {
        // Arrange
        var emptyList = new List<Listing>();

        // Act
        IEnumerable<Listing> result = await _sortingService.ApplyAllSortingRulesAsync(emptyList, 30.2672, -97.7431);

        // Assert
        result.Should().BeEmpty();
    }

    [Test]
    public async Task ApplyAllSortingRulesAsync_WithSamePriceAndDistance_MaintainsStableOrder() {
        // Arrange - Create items with identical price and location
        var identicalItems = new List<Listing> {
            new() {
                ListingId = Guid.NewGuid(),
                Name = "First Item",
                Description = "Test",
                Price = new Price { Currency = "USD", Amount = 100.00m },
                Category = Category.Electronics,
                Location = new Location { Country = "US", Municipality = "Austin", Geohash = "dr5regw" }
            },
            new() {
                ListingId = Guid.NewGuid(),
                Name = "Second Item",
                Description = "Test",
                Price = new Price { Currency = "USD", Amount = 100.00m },
                Category = Category.Electronics,
                Location = new Location { Country = "US", Municipality = "Austin", Geohash = "dr5regw" }
            }
        };

        // Act
        IEnumerable<Listing> result =
            await _sortingService.ApplyAllSortingRulesAsync(identicalItems, 30.2672, -97.7431);
        var sortedListings = result.ToList();

        // Assert
        sortedListings.Should().HaveCount(2);
        // Both items should be present and maintain their relative order
        sortedListings[0].Name.Should().Be("First Item");
        sortedListings[1].Name.Should().Be("Second Item");
    }
}
