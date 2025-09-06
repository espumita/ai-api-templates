using Microsoft.AspNetCore.Mvc;
using csharp_aspnetcore_sample.Models;

namespace csharp_aspnetcore_sample.Controllers;

[ApiController]
[Route("api/[controller]")]
public class ListingsController : ControllerBase
{
    private static readonly List<Listing> _listings = new();

    // GET: api/listings
    [HttpGet]
    public ActionResult<IEnumerable<Listing>> GetListings([FromQuery] int page = 1, [FromQuery] int pageSize = 10)
    {
        var listings = _listings
            .Skip((page - 1) * pageSize)
            .Take(pageSize)
            .ToList();

        return Ok(listings);
    }

    // GET: api/listings/{guid}
    [HttpGet("{id}")]
    public ActionResult<Listing> GetListing(Guid id)
    {
        var listing = _listings.FirstOrDefault(p => p.ListingId == id);
        
        if (listing == null)
        {
            return NotFound();
        }

        return Ok(listing);
    }

    // POST: api/listings
    [HttpPost]
    public ActionResult<Listing> CreateListing(Listing listing)
    {
        listing.ListingId = Guid.NewGuid();
        _listings.Add(listing);

        return CreatedAtAction(
            nameof(GetListing),
            new { id = listing.ListingId },
            listing);
    }

    // PUT: api/listings/{guid}
    [HttpPut("{id}")]
    public IActionResult UpdateListing(Guid id, Listing listing)
    {
        if (id != listing.ListingId)
        {
            return BadRequest();
        }

        var existingListing = _listings.FirstOrDefault(p => p.ListingId == id);
        if (existingListing == null)
        {
            return NotFound();
        }

        var index = _listings.IndexOf(existingListing);
        _listings[index] = listing;

        return NoContent();
    }

    // DELETE: api/listings/{guid}
    [HttpDelete("{id}")]
    public IActionResult DeleteListing(Guid id)
    {
        var listing = _listings.FirstOrDefault(p => p.ListingId == id);
        if (listing == null)
        {
            return NotFound();
        }

        _listings.Remove(listing);
        return NoContent();
    }
}
