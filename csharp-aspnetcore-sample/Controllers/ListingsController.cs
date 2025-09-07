using Microsoft.AspNetCore.Mvc;
using csharp_aspnetcore_sample.Models;
using Microsoft.AspNetCore.Http;

namespace csharp_aspnetcore_sample.Controllers;

[ApiController]
[Route("api/[controller]")]
public class ListingsController : ControllerBase
{
    private static readonly List<Listing> _listings = new();

    // GET: api/listings
    [HttpGet]
    [ProducesResponseType(StatusCodes.Status200OK, Type = typeof(PaginatedListingsResponse))]
    [ProducesResponseType(StatusCodes.Status400BadRequest)]
    public ActionResult<PaginatedListingsResponse> GetListings([FromQuery] int page = 1, [FromQuery] int pageSize = 10)
    {
        if (page < 1 || pageSize < 1)
        {
            return BadRequest("Page and page size must be positive numbers");
        }

        var listings = _listings
            .Skip((page - 1) * pageSize)
            .Take(pageSize)
            .ToList();

        var response = new PaginatedListingsResponse
        {
            Items = listings,
            TotalItems = _listings.Count,
            Page = page,
            PageSize = pageSize
        };

        return Ok(response);
    }

    // GET: api/listings/{guid}
    [HttpGet("{id}")]
    [ProducesResponseType(StatusCodes.Status200OK, Type = typeof(Listing))]
    [ProducesResponseType(StatusCodes.Status404NotFound)]
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
    [ProducesResponseType(StatusCodes.Status201Created, Type = typeof(Listing))]
    [ProducesResponseType(StatusCodes.Status400BadRequest)]
    public ActionResult<Listing> CreateListing(Listing listing)
    {
        if (!ModelState.IsValid)
        {
            return BadRequest(ModelState);
        }

        listing.ListingId = Guid.NewGuid();
        _listings.Add(listing);

        return CreatedAtAction(
            nameof(GetListing),
            new { id = listing.ListingId },
            listing);
    }

    // PUT: api/listings/{guid}
    [HttpPut("{id}")]
    [ProducesResponseType(StatusCodes.Status200OK, Type = typeof(Listing))]
    [ProducesResponseType(StatusCodes.Status400BadRequest)]
    [ProducesResponseType(StatusCodes.Status404NotFound)]
    public ActionResult<Listing> UpdateListing(Guid id, Listing listing)
    {
        if (!ModelState.IsValid)
        {
            return BadRequest(ModelState);
        }

        if (id != listing.ListingId)
        {
            return BadRequest("The ID in the URL must match the ID in the request body");
        }

        var existingListing = _listings.FirstOrDefault(p => p.ListingId == id);
        if (existingListing == null)
        {
            return NotFound();
        }

        var index = _listings.IndexOf(existingListing);
        _listings[index] = listing;

        return Ok(listing);
    }

    // DELETE: api/listings/{guid}
    [HttpDelete("{id}")]
    [ProducesResponseType(StatusCodes.Status204NoContent)]
    [ProducesResponseType(StatusCodes.Status404NotFound)]
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
