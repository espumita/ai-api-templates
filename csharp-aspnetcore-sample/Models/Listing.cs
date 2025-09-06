using System.ComponentModel.DataAnnotations;

namespace csharp_aspnetcore_sample.Models;

public class Listing
{
    public int ListingId { get; set; }

    [Required]
    [StringLength(100)]
    public string Name { get; set; } = string.Empty;

    [Required]
    public string Description { get; set; } = string.Empty;

    [Required]
    public Price Price { get; set; } = new();

    [Required]
    public string Category { get; set; } = string.Empty;
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
