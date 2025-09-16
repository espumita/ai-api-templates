using csharp_aspnetcore_sample.Models;

namespace csharp_aspnetcore_sample.Services;

public interface ISortingService {
    Task<IEnumerable<Listing>> ApplyAllSortingRulesAsync(IEnumerable<Listing> listings, double? latitude, double? longitude);
}