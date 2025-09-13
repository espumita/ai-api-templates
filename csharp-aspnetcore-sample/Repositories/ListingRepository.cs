using System.Data;
using csharp_aspnetcore_sample.Models;
using Dapper;
using Npgsql;

namespace csharp_aspnetcore_sample.Repositories;

public class ListingRepository : IListingRepository
{
    private readonly string _connectionString;

    public ListingRepository(IConfiguration configuration)
    {
        _connectionString = configuration.GetConnectionString("DefaultConnection") 
            ?? throw new InvalidOperationException("Connection string 'DefaultConnection' not found.");
    }

    private IDbConnection CreateConnection() => new NpgsqlConnection(_connectionString);

    public async Task<IEnumerable<Listing>> GetAllAsync(int page, int pageSize)
    {
        const string sql = @"
            SELECT 
                listing_id as ListingId,
                name as Name,
                description as Description,
                price_currency as Currency,
                price_amount as Amount,
                category as Category,
                location_country as Country,
                location_municipality as Municipality,
                location_geohash as Geohash
            FROM listings 
            ORDER BY created_at DESC
            LIMIT @PageSize OFFSET @Offset";

        using var connection = CreateConnection();
        var offset = (page - 1) * pageSize;
        
        var result = await connection.QueryAsync(sql, new { PageSize = pageSize, Offset = offset });
        
        return result.Select(MapToListing);
    }

    public async Task<int> GetTotalCountAsync()
    {
        const string sql = "SELECT COUNT(*) FROM listings";
        
        using var connection = CreateConnection();
        return await connection.QuerySingleAsync<int>(sql);
    }

    public async Task<Listing?> GetByIdAsync(Guid id)
    {
        const string sql = @"
            SELECT 
                listing_id as ListingId,
                name as Name,
                description as Description,
                price_currency as Currency,
                price_amount as Amount,
                category as Category,
                location_country as Country,
                location_municipality as Municipality,
                location_geohash as Geohash
            FROM listings 
            WHERE listing_id = @Id";

        using var connection = CreateConnection();
        var result = await connection.QuerySingleOrDefaultAsync(sql, new { Id = id });
        
        return result != null ? MapToListing(result) : null;
    }

    public async Task<Listing> CreateAsync(Listing listing)
    {
        const string sql = @"
            INSERT INTO listings (
                listing_id, name, description, price_currency, price_amount, 
                category, location_country, location_municipality, location_geohash
            ) VALUES (
                @ListingId, @Name, @Description, @Currency, @Amount, 
                @Category, @Country, @Municipality, @Geohash
            )";

        listing.ListingId = Guid.NewGuid();

        using var connection = CreateConnection();
        await connection.ExecuteAsync(sql, new
        {
            ListingId = listing.ListingId,
            Name = listing.Name,
            Description = listing.Description,
            Currency = listing.Price.Currency,
            Amount = listing.Price.Amount,
            Category = listing.Category.ToString(),
            Country = listing.Location.Country,
            Municipality = listing.Location.Municipality,
            Geohash = listing.Location.Geohash
        });

        return listing;
    }

    public async Task<Listing?> UpdateAsync(Listing listing)
    {
        const string sql = @"
            UPDATE listings SET 
                name = @Name,
                description = @Description,
                price_currency = @Currency,
                price_amount = @Amount,
                category = @Category,
                location_country = @Country,
                location_municipality = @Municipality,
                location_geohash = @Geohash,
                updated_at = CURRENT_TIMESTAMP
            WHERE listing_id = @ListingId";

        using var connection = CreateConnection();
        var rowsAffected = await connection.ExecuteAsync(sql, new
        {
            ListingId = listing.ListingId,
            Name = listing.Name,
            Description = listing.Description,
            Currency = listing.Price.Currency,
            Amount = listing.Price.Amount,
            Category = listing.Category.ToString(),
            Country = listing.Location.Country,
            Municipality = listing.Location.Municipality,
            Geohash = listing.Location.Geohash
        });

        return rowsAffected > 0 ? listing : null;
    }

    public async Task<bool> DeleteAsync(Guid id)
    {
        const string sql = "DELETE FROM listings WHERE listing_id = @Id";
        
        using var connection = CreateConnection();
        var rowsAffected = await connection.ExecuteAsync(sql, new { Id = id });
        
        return rowsAffected > 0;
    }

    public async Task<bool> ExistsAsync(Guid id)
    {
        const string sql = "SELECT COUNT(*) FROM listings WHERE listing_id = @Id";
        
        using var connection = CreateConnection();
        var count = await connection.QuerySingleAsync<int>(sql, new { Id = id });
        
        return count > 0;
    }

    private static Listing MapToListing(dynamic row)
    {
        var categoryString = (string)row.Category;
        var category = Enum.Parse<Category>(categoryString.Replace(" & ", "And").Replace(" ", "").Replace(",", ""));

        return new Listing
        {
            ListingId = (Guid)row.ListingId,
            Name = (string)row.Name,
            Description = (string)row.Description,
            Price = new Price
            {
                Currency = (string)row.Currency,
                Amount = (decimal)row.Amount
            },
            Category = category,
            Location = new Location
            {
                Country = (string)row.Country,
                Municipality = (string)row.Municipality,
                Geohash = (string)row.Geohash
            }
        };
    }
}