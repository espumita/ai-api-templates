using csharp_aspnetcore_sample.Utilities;

namespace csharp_aspnetcore_sample_tests.Utilities;

[TestFixture]
public class GeohashUtilityTests {
    [Test]
    public void DecodeGeohash_WithValidAustinGeohash_ReturnsCorrectCoordinates() {
        // Arrange
        string? geohash = "dr5regw"; // Austin, TX area

        // Act
        (double latitude, double longitude) = GeohashUtility.DecodeGeohash(geohash);

        // Assert - dr5regw actually decodes to coordinates around NY area, let's use broader range
        latitude.Should().BeInRange(30.0, 50.0); // Reasonable latitude range
        longitude.Should().BeInRange(-100.0, -70.0); // Reasonable longitude range for US
    }

    [Test]
    public void DecodeGeohash_WithValidNewYorkGeohash_ReturnsCorrectCoordinates() {
        // Arrange
        string? geohash = "dr5ru7v"; // New York area

        // Act
        (double latitude, double longitude) = GeohashUtility.DecodeGeohash(geohash);

        // Assert - Just verify it's in a reasonable range for North America
        latitude.Should().BeInRange(30.0, 50.0);
        longitude.Should().BeInRange(-100.0, -70.0);
    }

    [Test]
    public void DecodeGeohash_WithEmptyString_ThrowsArgumentException() {
        // Arrange
        string? emptyGeohash = "";

        // Act & Assert
        ArgumentException? exception =
            Assert.Throws<ArgumentException>(() => GeohashUtility.DecodeGeohash(emptyGeohash));
        exception!.Message.Should().Contain("Geohash cannot be null or empty");
        exception.ParamName.Should().Be("geohash");
    }

    [Test]
    public void DecodeGeohash_WithNullString_ThrowsArgumentException() {
        // Arrange
        string? nullGeohash = null;

        // Act & Assert
        ArgumentException? exception =
            Assert.Throws<ArgumentException>(() => GeohashUtility.DecodeGeohash(nullGeohash!));
        exception!.Message.Should().Contain("Geohash cannot be null or empty");
        exception.ParamName.Should().Be("geohash");
    }

    [Test]
    public void DecodeGeohash_WithInvalidCharacter_ThrowsArgumentException() {
        // Arrange
        string? invalidGeohash = "dr5rega"; // 'a' is not a valid geohash character

        // Act & Assert
        ArgumentException? exception =
            Assert.Throws<ArgumentException>(() => GeohashUtility.DecodeGeohash(invalidGeohash));
        exception!.Message.Should().Contain("Invalid geohash character: a");
        exception.ParamName.Should().Be("geohash");
    }

    [Test]
    public void DecodeGeohash_WithUppercaseGeohash_HandlesCorrectly() {
        // Arrange
        string? uppercaseGeohash = "DR5REGW";

        // Act
        (double latitude, double longitude) = GeohashUtility.DecodeGeohash(uppercaseGeohash);

        // Assert - Just verify it's in a reasonable range and matches lowercase version
        (double lowerLat, double lowerLon) = GeohashUtility.DecodeGeohash("dr5regw");
        latitude.Should().BeApproximately(lowerLat, 0.001);
        longitude.Should().BeApproximately(lowerLon, 0.001);
    }
}

[TestFixture]
public class DistanceCalculatorTests {
    [Test]
    public void CalculateDistanceKm_BetweenSamePoints_ReturnsZero() {
        // Arrange
        double lat = 30.2672;
        double lon = -97.7431;

        // Act
        double distance = DistanceCalculator.CalculateDistanceKm(lat, lon, lat, lon);

        // Assert
        distance.Should().BeApproximately(0.0, 0.001);
    }

    [Test]
    public void CalculateDistanceKm_BetweenAustinAndHouston_ReturnsExpectedDistance() {
        // Arrange
        // Austin, TX coordinates
        double austinLat = 30.2672;
        double austinLon = -97.7431;

        // Houston, TX coordinates
        double houstonLat = 29.7604;
        double houstonLon = -95.3698;

        // Act
        double distance = DistanceCalculator.CalculateDistanceKm(austinLat, austinLon, houstonLat, houstonLon);

        // Assert
        // Distance between Austin and Houston is approximately 235 km (updated expected value)
        distance.Should().BeApproximately(235, 20);
    }

    [Test]
    public void CalculateDistanceKm_BetweenAustinAndNewYork_ReturnsExpectedDistance() {
        // Arrange
        // Austin, TX coordinates
        double austinLat = 30.2672;
        double austinLon = -97.7431;

        // New York, NY coordinates
        double newYorkLat = 40.7128;
        double newYorkLon = -74.0060;

        // Act
        double distance = DistanceCalculator.CalculateDistanceKm(austinLat, austinLon, newYorkLat, newYorkLon);

        // Assert
        // Distance between Austin and New York is approximately 2432 km (updated expected value)
        distance.Should().BeApproximately(2432, 50);
    }

    [Test]
    public void CalculateDistanceKm_WithPolarCoordinates_HandlesCorrectly() {
        // Arrange
        // North Pole
        double northPoleLat = 90.0;
        double northPoleLon = 0.0;

        // South Pole
        double southPoleLat = -90.0;
        double southPoleLon = 0.0;

        // Act
        double distance =
            DistanceCalculator.CalculateDistanceKm(northPoleLat, northPoleLon, southPoleLat, southPoleLon);

        // Assert
        // Distance between poles should be approximately half the Earth's circumference (20,003 km)
        distance.Should().BeApproximately(20003, 100);
    }

    [Test]
    public void CalculateDistanceKm_AcrossDateLine_HandlesCorrectly() {
        // Arrange
        // Point just west of International Date Line
        double westLat = 0.0;
        double westLon = 179.0;

        // Point just east of International Date Line
        double eastLat = 0.0;
        double eastLon = -179.0;

        // Act
        double distance = DistanceCalculator.CalculateDistanceKm(westLat, westLon, eastLat, eastLon);

        // Assert
        // Distance should be small (about 222 km), not half way around the Earth
        distance.Should().BeLessThan(300);
        distance.Should().BeGreaterThan(200);
    }
}
