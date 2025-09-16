using csharp_aspnetcore_sample.Models;
using csharp_aspnetcore_sample.Utilities;

namespace csharp_aspnetcore_sample.Services;

public class SortingService : ISortingService {
    public async Task<IEnumerable<Listing>> ApplyAllSortingRulesAsync(IEnumerable<Listing> listings, double? latitude,
        double? longitude) {
        // Convert to list to avoid multiple enumeration
        var listingsList = listings.ToList();

        // Create a list of tuples with calculated values for sorting
        var listingsWithSortData = new List<(Listing Listing, double Distance, decimal Price)>();

        foreach (Listing listing in listingsList) {
            double distance = double.MaxValue; // Default value when no location provided

            // Calculate distance if client location is provided
            if (latitude.HasValue && longitude.HasValue) {
                try {
                    (double listingLat, double listingLon) = GeohashUtility.DecodeGeohash(listing.Location.Geohash);
                    distance = DistanceCalculator.CalculateDistanceKm(
                        latitude.Value, longitude.Value,
                        listingLat, listingLon);
                }
                catch (ArgumentException) {
                    // If geohash is invalid, keep max distance
                    distance = double.MaxValue;
                }
            }

            listingsWithSortData.Add((listing, distance, listing.Price.Amount));
        }

        // Apply compound sorting:
        // Rule 1: Sort by distance (ascending - closest first)
        // Rule 2: Then by price (ascending - cheaper first) 
        // Rule 3: Placeholder for future sorting (currently no additional sorting)
        IEnumerable<Listing> sortedListings = listingsWithSortData
            .OrderBy(x => x.Distance) // Rule 1: Distance
            .ThenBy(x => x.Price) // Rule 2: Price
            // Rule 3: Placeholder - can add .ThenBy() for future sorting criteria
            .Select(x => x.Listing);

        return await Task.FromResult(sortedListings);
    }
}
