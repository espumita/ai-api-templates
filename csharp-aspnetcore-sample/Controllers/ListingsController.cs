using Microsoft.AspNetCore.Mvc;
using csharp_aspnetcore_sample.Models;
using csharp_aspnetcore_sample.Repositories;
using Microsoft.AspNetCore.Http;

namespace csharp_aspnetcore_sample.Controllers;

[ApiController]
[Route("api/[controller]")]
public class ListingsController : ControllerBase
{
    private readonly IListingRepository _listingRepository;

    public ListingsController(IListingRepository listingRepository)
    {
        _listingRepository = listingRepository;
    }

    // GET: api/listings
    [HttpGet]
    [ProducesResponseType(StatusCodes.Status200OK, Type = typeof(PaginatedListingsResponse))]
    [ProducesResponseType(StatusCodes.Status400BadRequest)]
    public async Task<ActionResult<PaginatedListingsResponse>> GetListings([FromQuery] int page = 1, [FromQuery] int pageSize = 10)
    {
        if (page < 1 || pageSize < 1)
        {
            return BadRequest("Page and page size must be positive numbers");
        }

        var listings = await _listingRepository.GetAllAsync(page, pageSize);
        var totalItems = await _listingRepository.GetTotalCountAsync();

        var response = new PaginatedListingsResponse
        {
            Items = listings,
            TotalItems = totalItems,
            Page = page,
            PageSize = pageSize
        };

        return Ok(response);
    }

    // GET: api/listings/{guid}
    [HttpGet("{id}")]
    [ProducesResponseType(StatusCodes.Status200OK, Type = typeof(Listing))]
    [ProducesResponseType(StatusCodes.Status404NotFound)]
    public async Task<ActionResult<Listing>> GetListing(Guid id)
    {
        var listing = await _listingRepository.GetByIdAsync(id);
        
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
    public async Task<ActionResult<Listing>> CreateListing(Listing listing)
    {
        if (!ModelState.IsValid)
        {
            return BadRequest(ModelState);
        }

        var createdListing = await _listingRepository.CreateAsync(listing);

        return CreatedAtAction(
            nameof(GetListing),
            new { id = createdListing.ListingId },
            createdListing);
    }

    // PUT: api/listings/{guid}
    [HttpPut("{id}")]
    [ProducesResponseType(StatusCodes.Status200OK, Type = typeof(Listing))]
    [ProducesResponseType(StatusCodes.Status400BadRequest)]
    [ProducesResponseType(StatusCodes.Status404NotFound)]
    public async Task<ActionResult<Listing>> UpdateListing(Guid id, Listing listing)
    {
        if (!ModelState.IsValid)
        {
            return BadRequest(ModelState);
        }

        if (id != listing.ListingId)
        {
            return BadRequest("The ID in the URL must match the ID in the request body");
        }

        var exists = await _listingRepository.ExistsAsync(id);
        if (!exists)
        {
            return NotFound();
        }

        var updatedListing = await _listingRepository.UpdateAsync(listing);
        if (updatedListing == null)
        {
            return NotFound();
        }

        return Ok(updatedListing);
    }

    // DELETE: api/listings/{guid}
    [HttpDelete("{id}")]
    [ProducesResponseType(StatusCodes.Status204NoContent)]
    [ProducesResponseType(StatusCodes.Status404NotFound)]
    public async Task<IActionResult> DeleteListing(Guid id)
    {
        var deleted = await _listingRepository.DeleteAsync(id);
        if (!deleted)
        {
            return NotFound();
        }

        return NoContent();
    }
}
