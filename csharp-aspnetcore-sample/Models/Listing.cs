using System.ComponentModel.DataAnnotations;
using System.Runtime.Serialization;

namespace csharp_aspnetcore_sample.Models;

public enum Category {
    [EnumMember(Value = "Electronics")]
    Electronics,
    [EnumMember(Value = "Fashion")]
    Fashion,
    [EnumMember(Value = "Home & Garden")]
    HomeAndGarden,
    [EnumMember(Value = "Motors")]
    Motors,
    [EnumMember(Value = "Collectibles & Art")]
    CollectiblesAndArt,
    [EnumMember(Value = "Sporting Goods")]
    SportingGoods,
    [EnumMember(Value = "Toys & Hobbies")]
    ToysAndHobbies,
    [EnumMember(Value = "Business & Industrial")]
    BusinessAndIndustrial,
    [EnumMember(Value = "Music")]
    Music,
    [EnumMember(Value = "Health & Beauty")]
    HealthAndBeauty,
    [EnumMember(Value = "Books")]
    Books,
    [EnumMember(Value = "Cameras & Photo")]
    CamerasAndPhoto,
    [EnumMember(Value = "Computers, Tablets & Networking")]
    ComputersTabletsAndNetworking,
    [EnumMember(Value = "Cell Phones & Accessories")]
    CellPhonesAndAccessories,
    [EnumMember(Value = "Video Games & Consoles")]
    VideoGamesAndConsoles
}

public class Listing {
    public Guid ListingId { get; set; }

    [Required]
    [StringLength(100)]
    public string Name { get; set; } = string.Empty;

    [Required]
    public string Description { get; set; } = string.Empty;

    [Required]
    public Price Price { get; set; } = new();

    [Required]
    public Category Category { get; set; }

    [Required]
    public Location Location { get; set; } = new();
}

public class Price {
    [Required]
    [StringLength(3, MinimumLength = 3)]
    public string Currency { get; set; } = string.Empty;

    [Required]
    [Range(0.01, double.MaxValue, ErrorMessage = "Amount must be greater than 0")]
    public decimal Amount { get; set; }
}

public class Location {
    [Required]
    [StringLength(2, MinimumLength = 2, ErrorMessage = "Country must be a valid ISO 3166 2-character code")]
    public string Country { get; set; } = string.Empty;

    [Required]
    public string Municipality { get; set; } = string.Empty;

    [Required]
    [StringLength(7, MinimumLength = 7, ErrorMessage = "Geohash must be exactly 7 characters")]
    public string Geohash { get; set; } = string.Empty;
}

public class PaginatedListingsResponse {
    public IEnumerable<Listing> Items { get; set; } = new List<Listing>();
    public int TotalItems { get; set; }
    public int Page { get; set; }
    public int PageSize { get; set; }
}

public class FilterRequest {
    public List<Filter> Filters { get; set; } = new();
    public int Page { get; set; } = 1;
    public int PageSize { get; set; } = 10;
}

public class Filter {
    [Required]
    public string Field { get; set; } = string.Empty;
    
    [Required]
    public string Operator { get; set; } = string.Empty;
    
    [Required]
    public object Value { get; set; } = string.Empty;
}

public class SearchResponse {
    public IEnumerable<Listing> Items { get; set; } = new List<Listing>();
    public int TotalItems { get; set; }
    public int Page { get; set; }
    public int PageSize { get; set; }
    public List<Filter> AppliedFilters { get; set; } = new();
}
