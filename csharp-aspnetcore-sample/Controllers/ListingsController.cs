using Microsoft.AspNetCore.Mvc;
using csharp_aspnetcore_sample.Models;

namespace csharp_aspnetcore_sample.Controllers;

[ApiController]
[Route("api/[controller]")]
public class ListingsController : ControllerBase
{
    private static readonly List<Listing> _listings = new();
    private static int _nextId = 1;

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

    // GET: api/listings/5
    [HttpGet("{id}")]
    public ActionResult<Listing> GetListing(int id)
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
        listing.ListingId = _nextId++;
        _listings.Add(listing);

        return CreatedAtAction(
            nameof(GetListing),
            new { id = listing.ListingId },
            listing);
    }

    // PUT: api/listings/5
    [HttpPut("{id}")]
    public IActionResult UpdateListing(int id, Listing listing)
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

    // DELETE: api/listings/5
    [HttpDelete("{id}")]
    public IActionResult DeleteListing(int id)
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
