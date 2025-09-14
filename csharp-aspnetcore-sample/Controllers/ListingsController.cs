using Microsoft.AspNetCore.Mvc;
using csharp_aspnetcore_sample.Models;
using csharp_aspnetcore_sample.Repositories;
using Microsoft.AspNetCore.Http;

namespace csharp_aspnetcore_sample.Controllers;

[ApiController]
[Route("api/[controller]")]
public class ListingsController : ControllerBase {
    private readonly IListingRepository _listingRepository;

    public ListingsController(IListingRepository listingRepository) {
        _listingRepository = listingRepository;
    }

    // GET: api/listings
    [HttpGet]
    [ProducesResponseType(StatusCodes.Status200OK, Type = typeof(PaginatedListingsResponse))]
    [ProducesResponseType(StatusCodes.Status400BadRequest)]
    public async Task<ActionResult<PaginatedListingsResponse>> GetListings(
        [FromQuery] int page = 1, 
        [FromQuery] int pageSize = 10,
        [FromQuery] double? latitude = null,
        [FromQuery] double? longitude = null) {

        if (pageSize > 50) {
            return BadRequest("Page size cannot exceed 50 items");
        }

        // Validate latitude and longitude if provided
        if (latitude.HasValue && (latitude.Value < -90 || latitude.Value > 90)) {
            return BadRequest("Latitude must be between -90 and 90 degrees");
        }

        if (longitude.HasValue && (longitude.Value < -180 || longitude.Value > 180)) {
            return BadRequest("Longitude must be between -180 and 180 degrees");
        }

        // Both or neither latitude and longitude must be provided
        if ((latitude.HasValue && !longitude.HasValue) || (!latitude.HasValue && longitude.HasValue)) {
            return BadRequest("Both latitude and longitude must be provided together for proximity sorting");
        }

        var listings = await _listingRepository.GetAllAsync(page, pageSize, latitude, longitude);
        var totalItems = await _listingRepository.GetTotalCountAsync();

        var response = new PaginatedListingsResponse {
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
    public async Task<ActionResult<Listing>> GetListing(Guid id) {
        var listing = await _listingRepository.GetByIdAsync(id);
        
        if (listing == null) {
            return NotFound();
        }

        return Ok(listing);
    }

    // POST: api/listings
    [HttpPost]
    [ProducesResponseType(StatusCodes.Status201Created, Type = typeof(Listing))]
    [ProducesResponseType(StatusCodes.Status400BadRequest)]
    public async Task<ActionResult<Listing>> CreateListing(Listing listing) {
        if (!ModelState.IsValid) {
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
    public async Task<ActionResult<Listing>> UpdateListing(Guid id, Listing listing) {
        if (!ModelState.IsValid) {
            return BadRequest(ModelState);
        }

        if (id != listing.ListingId) {
            return BadRequest("The ID in the URL must match the ID in the request body");
        }

        var exists = await _listingRepository.ExistsAsync(id);
        if (!exists) {
            return NotFound();
        }

        var updatedListing = await _listingRepository.UpdateAsync(listing);
        if (updatedListing == null) {
            return NotFound();
        }

        return Ok(updatedListing);
    }

    // DELETE: api/listings/{guid}
    [HttpDelete("{id}")]
    [ProducesResponseType(StatusCodes.Status204NoContent)]
    [ProducesResponseType(StatusCodes.Status404NotFound)]
    public async Task<IActionResult> DeleteListing(Guid id) {
        var deleted = await _listingRepository.DeleteAsync(id);
        if (!deleted) {
            return NotFound();
        }

        return NoContent();
    }

    // POST: api/listings/search
    [HttpPost("search")]
    [ProducesResponseType(StatusCodes.Status200OK, Type = typeof(SearchResponse))]
    [ProducesResponseType(StatusCodes.Status400BadRequest)]
    public async Task<ActionResult<SearchResponse>> SearchListings([FromBody] FilterRequest filterRequest) {
        if (!ModelState.IsValid) {
            return BadRequest(ModelState);
        }

        if (filterRequest.PageSize > 50) {
            return BadRequest("Page size cannot exceed 50 items");
        }

        // Validate latitude and longitude if provided
        if (filterRequest.Latitude.HasValue && (filterRequest.Latitude.Value < -90 || filterRequest.Latitude.Value > 90)) {
            return BadRequest("Latitude must be between -90 and 90 degrees");
        }

        if (filterRequest.Longitude.HasValue && (filterRequest.Longitude.Value < -180 || filterRequest.Longitude.Value > 180)) {
            return BadRequest("Longitude must be between -180 and 180 degrees");
        }

        // Both or neither latitude and longitude must be provided
        if ((filterRequest.Latitude.HasValue && !filterRequest.Longitude.HasValue) || 
            (!filterRequest.Latitude.HasValue && filterRequest.Longitude.HasValue)) {
            return BadRequest("Both latitude and longitude must be provided together for proximity sorting");
        }

        // Validate filter operators and fields
        var validFields = new[] { "name", "description", "category", "location.country", "location.municipality" };
        var validOperators = new[] { "contains", "equals" };

        foreach (var filter in filterRequest.Filters) {
            if (!validFields.Contains(filter.Field.ToLowerInvariant())) {
                return BadRequest($"Invalid filter field: {filter.Field}. Valid fields are: {string.Join(", ", validFields)}");
            }

            if (!validOperators.Contains(filter.Operator.ToLowerInvariant())) {
                return BadRequest($"Invalid filter operator: {filter.Operator}. Valid operators are: {string.Join(", ", validOperators)}");
            }

            // Special validation for category field - must use equals operator
            if (filter.Field.ToLowerInvariant() == "category" && filter.Operator.ToLowerInvariant() != "equals") {
                return BadRequest("Category field only supports 'equals' operator");
            }
        }

        var (items, totalCount) = await _listingRepository.SearchAsync(filterRequest.Filters, filterRequest.Page, filterRequest.PageSize, filterRequest.Latitude, filterRequest.Longitude);

        var response = new SearchResponse {
            Items = items,
            TotalItems = totalCount,
            Page = filterRequest.Page,
            PageSize = filterRequest.PageSize,
            AppliedFilters = filterRequest.Filters
        };

        return Ok(response);
    }
}
