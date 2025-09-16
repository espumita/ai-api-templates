using System.ComponentModel.DataAnnotations;
using csharp_aspnetcore_sample.Models;

namespace csharp_aspnetcore_sample_tests.Models;

[TestFixture]
public class ListingValidationTests {
    [Test]
    public void Listing_WithValidData_PassesValidation() {
        // Arrange
        var listing = new Listing {
            ListingId = Guid.NewGuid(),
            Name = "Valid Listing",
            Description = "This is a valid listing description",
            Price = new Price { Currency = "USD", Amount = 99.99m },
            Category = Category.Electronics,
            Location = new Location { Country = "US", Municipality = "Austin", Geohash = "dr5regw" }
        };

        // Act
        IList<ValidationResult> validationResults = ValidateModel(listing);

        // Assert
        validationResults.Should().BeEmpty();
    }

    [Test]
    public void Listing_WithEmptyName_FailsValidation() {
        // Arrange
        var listing = new Listing {
            ListingId = Guid.NewGuid(),
            Name = "", // Invalid: empty name
            Description = "Valid description",
            Price = new Price { Currency = "USD", Amount = 99.99m },
            Category = Category.Electronics,
            Location = new Location { Country = "US", Municipality = "Austin", Geohash = "dr5regw" }
        };

        // Act
        IList<ValidationResult> validationResults = ValidateModel(listing);

        // Assert
        validationResults.Should().NotBeEmpty();
        validationResults.Should().Contain(r => r.MemberNames.Contains("Name"));
    }

    [Test]
    public void Listing_WithNameTooLong_FailsValidation() {
        // Arrange
        var listing = new Listing {
            ListingId = Guid.NewGuid(),
            Name = new string('a', 101), // Invalid: exceeds 100 character limit
            Description = "Valid description",
            Price = new Price { Currency = "USD", Amount = 99.99m },
            Category = Category.Electronics,
            Location = new Location { Country = "US", Municipality = "Austin", Geohash = "dr5regw" }
        };

        // Act
        IList<ValidationResult> validationResults = ValidateModel(listing);

        // Assert
        validationResults.Should().NotBeEmpty();
        validationResults.Should().Contain(r => r.MemberNames.Contains("Name"));
    }

    [Test]
    public void Price_WithZeroAmount_FailsValidation() {
        // Arrange
        var price = new Price {
            Currency = "USD", Amount = 0 // Invalid: amount must be greater than 0
        };

        // Act
        IList<ValidationResult> validationResults = ValidateModel(price);

        // Assert
        validationResults.Should().NotBeEmpty();
        validationResults.Should().Contain(r => r.MemberNames.Contains("Amount"));
    }

    [Test]
    public void Price_WithNegativeAmount_FailsValidation() {
        // Arrange
        var price = new Price {
            Currency = "USD", Amount = -10.50m // Invalid: negative amount
        };

        // Act
        IList<ValidationResult> validationResults = ValidateModel(price);

        // Assert
        validationResults.Should().NotBeEmpty();
        validationResults.Should().Contain(r => r.MemberNames.Contains("Amount"));
    }

    [Test]
    public void Price_WithInvalidCurrencyLength_FailsValidation() {
        // Arrange
        var price = new Price {
            Currency = "USDT", // Invalid: currency must be exactly 3 characters
            Amount = 99.99m
        };

        // Act
        IList<ValidationResult> validationResults = ValidateModel(price);

        // Assert
        validationResults.Should().NotBeEmpty();
        validationResults.Should().Contain(r => r.MemberNames.Contains("Currency"));
    }

    [Test]
    public void Location_WithInvalidCountryCode_FailsValidation() {
        // Arrange
        var location = new Location {
            Country = "USA", // Invalid: should be 2-character ISO code
            Municipality = "Austin",
            Geohash = "dr5regw"
        };

        // Act
        IList<ValidationResult> validationResults = ValidateModel(location);

        // Assert
        validationResults.Should().NotBeEmpty();
        validationResults.Should().Contain(r => r.MemberNames.Contains("Country"));
    }

    [Test]
    public void Location_WithInvalidGeohashLength_FailsValidation() {
        // Arrange
        var location = new Location {
            Country = "US", Municipality = "Austin", Geohash = "dr5reg" // Invalid: should be exactly 7 characters
        };

        // Act
        IList<ValidationResult> validationResults = ValidateModel(location);

        // Assert
        validationResults.Should().NotBeEmpty();
        validationResults.Should().Contain(r => r.MemberNames.Contains("Geohash"));
    }

    [Test]
    public void Filter_WithValidData_PassesValidation() {
        // Arrange
        var filter = new Filter { Field = "name", Operator = "contains", Value = "test" };

        // Act
        IList<ValidationResult> validationResults = ValidateModel(filter);

        // Assert
        validationResults.Should().BeEmpty();
    }

    [Test]
    public void Filter_WithEmptyField_FailsValidation() {
        // Arrange
        var filter = new Filter {
            Field = "", // Invalid: empty field
            Operator = "contains",
            Value = "test"
        };

        // Act
        IList<ValidationResult> validationResults = ValidateModel(filter);

        // Assert
        validationResults.Should().NotBeEmpty();
        validationResults.Should().Contain(r => r.MemberNames.Contains("Field"));
    }

    [Test]
    public void FilterRequest_WithValidData_PassesValidation() {
        // Arrange
        var filterRequest = new FilterRequest {
            Filters = new List<Filter> { new() { Field = "name", Operator = "contains", Value = "test" } },
            Page = 1,
            PageSize = 10
        };

        // Act
        IList<ValidationResult> validationResults = ValidateModel(filterRequest);

        // Assert
        validationResults.Should().BeEmpty();
    }

    private static IList<ValidationResult> ValidateModel(object model) {
        var validationResults = new List<ValidationResult>();
        var validationContext = new ValidationContext(model);
        Validator.TryValidateObject(model, validationContext, validationResults, true);
        return validationResults;
    }
}
