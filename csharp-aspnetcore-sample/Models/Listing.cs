using System.ComponentModel.DataAnnotations;
using System.Runtime.Serialization;

namespace csharp_aspnetcore_sample.Models;

public enum Category
{
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

public class Listing
{
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
}

public class Price
{
    [Required]
    [StringLength(3, MinimumLength = 3)]
    public string Currency { get; set; } = string.Empty;

    [Required]
    [Range(0.01, double.MaxValue, ErrorMessage = "Amount must be greater than 0")]
    public decimal Amount { get; set; }
}
