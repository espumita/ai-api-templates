using System.Data;
using csharp_aspnetcore_sample.Models;
using csharp_aspnetcore_sample.Services;
using Dapper;
using Npgsql;

namespace csharp_aspnetcore_sample.Repositories;

public class ListingRepository : IListingRepository {
    private readonly string _connectionString;
    private readonly ISortingService _sortingService;

    public ListingRepository(IConfiguration configuration, ISortingService sortingService) {
        _connectionString = configuration.GetConnectionString("DefaultConnection")
                            ?? throw new InvalidOperationException("Connection string 'DefaultConnection' not found.");
        _sortingService = sortingService;
    }

    public async Task<IEnumerable<Listing>> GetAllAsync(int page, int pageSize, double? latitude = null,
        double? longitude = null) {
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
            ORDER BY created_at DESC";

        using IDbConnection? connection = CreateConnection();

        IEnumerable<dynamic>? result = await connection.QueryAsync(sql);
        IEnumerable<Listing>? listings = result.Select(MapToListing);

        // Apply sorting rules
        IEnumerable<Listing>? sortedListings =
            await _sortingService.ApplyAllSortingRulesAsync(listings, latitude, longitude);

        // Apply pagination after sorting
        int offset = (page - 1) * pageSize;
        IEnumerable<Listing>? paginatedListings = sortedListings.Skip(offset).Take(pageSize);

        return paginatedListings;
    }

    public async Task<int> GetTotalCountAsync() {
        const string sql = "SELECT COUNT(*) FROM listings";

        using IDbConnection? connection = CreateConnection();
        return await connection.QuerySingleAsync<int>(sql);
    }

    public async Task<Listing?> GetByIdAsync(Guid id) {
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

        using IDbConnection? connection = CreateConnection();
        dynamic? result = await connection.QuerySingleOrDefaultAsync(sql, new { Id = id });

        return result != null ? MapToListing(result) : null;
    }

    public async Task<Listing> CreateAsync(Listing listing) {
        const string sql = @"
            INSERT INTO listings (
                listing_id, name, description, price_currency, price_amount, 
                category, location_country, location_municipality, location_geohash
            ) VALUES (
                @ListingId, @Name, @Description, @Currency, @Amount, 
                @Category, @Country, @Municipality, @Geohash
            )";

        listing.ListingId = Guid.NewGuid();

        using IDbConnection? connection = CreateConnection();
        await connection.ExecuteAsync(sql,
            new {
                listing.ListingId,
                listing.Name,
                listing.Description,
                listing.Price.Currency,
                listing.Price.Amount,
                Category = listing.Category.ToString(),
                listing.Location.Country,
                listing.Location.Municipality,
                listing.Location.Geohash
            });

        return listing;
    }

    public async Task<Listing?> UpdateAsync(Listing listing) {
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

        using IDbConnection? connection = CreateConnection();
        int rowsAffected = await connection.ExecuteAsync(sql,
            new {
                listing.ListingId,
                listing.Name,
                listing.Description,
                listing.Price.Currency,
                listing.Price.Amount,
                Category = listing.Category.ToString(),
                listing.Location.Country,
                listing.Location.Municipality,
                listing.Location.Geohash
            });

        return rowsAffected > 0 ? listing : null;
    }

    public async Task<bool> DeleteAsync(Guid id) {
        const string sql = "DELETE FROM listings WHERE listing_id = @Id";

        using IDbConnection? connection = CreateConnection();
        int rowsAffected = await connection.ExecuteAsync(sql, new { Id = id });

        return rowsAffected > 0;
    }

    public async Task<bool> ExistsAsync(Guid id) {
        const string sql = "SELECT COUNT(*) FROM listings WHERE listing_id = @Id";

        using IDbConnection? connection = CreateConnection();
        int count = await connection.QuerySingleAsync<int>(sql, new { Id = id });

        return count > 0;
    }

    public async Task<(IEnumerable<Listing> Items, int TotalCount)> SearchAsync(List<Filter> filters, int page,
        int pageSize, double? latitude = null, double? longitude = null) {
        string baseSql = @"
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
            FROM listings";

        string countSql = "SELECT COUNT(*) FROM listings";

        var whereConditions = new List<string>();
        var parameters = new DynamicParameters();

        BuildWhereClause(filters, whereConditions, parameters);

        if (whereConditions.Count > 0) {
            string whereClause = " WHERE " + string.Join(" AND ", whereConditions);
            baseSql += whereClause;
            countSql += whereClause;
        }

        baseSql += " ORDER BY created_at DESC";

        using IDbConnection connection = CreateConnection();

        IEnumerable<dynamic> itemsTask = await connection.QueryAsync(baseSql, parameters);
        int totalCount = await connection.QuerySingleAsync<int>(countSql, parameters);

        IEnumerable<Listing> listings = itemsTask.Select(MapToListing);

        // Apply sorting rules
        IEnumerable<Listing> sortedListings =
            await _sortingService.ApplyAllSortingRulesAsync(listings, latitude, longitude);

        // Apply pagination after sorting
        int offset = (page - 1) * pageSize;
        IEnumerable<Listing> paginatedListings = sortedListings.Skip(offset).Take(pageSize);

        return (paginatedListings, totalCount);
    }

    private IDbConnection CreateConnection() {
        return new NpgsqlConnection(_connectionString);
    }

    private static void BuildWhereClause(List<Filter> filters, List<string> whereConditions,
        DynamicParameters parameters) {
        for (int i = 0; i < filters.Count; i++) {
            Filter? filter = filters[i];
            string? paramName = $"param{i}";

            switch (filter.Field.ToLowerInvariant()) {
                case "name":
                    if (filter.Operator.ToLowerInvariant() == "contains") {
                        whereConditions.Add($"LOWER(name) LIKE LOWER(@{paramName})");
                        parameters.Add($"@{paramName}", $"%{filter.Value}%");
                    }

                    break;

                case "description":
                    if (filter.Operator.ToLowerInvariant() == "contains") {
                        whereConditions.Add($"LOWER(description) LIKE LOWER(@{paramName})");
                        parameters.Add($"@{paramName}", $"%{filter.Value}%");
                    }

                    break;

                case "category":
                    if (filter.Operator.ToLowerInvariant() == "equals") {
                        whereConditions.Add($"category = @{paramName}");
                        parameters.Add($"@{paramName}", filter.Value.ToString());
                    }

                    break;

                case "location.country":
                    if (filter.Operator.ToLowerInvariant() == "contains") {
                        whereConditions.Add($"LOWER(location_country) LIKE LOWER(@{paramName})");
                        parameters.Add($"@{paramName}", $"%{filter.Value}%");
                    }

                    break;

                case "location.municipality":
                    if (filter.Operator.ToLowerInvariant() == "contains") {
                        whereConditions.Add($"LOWER(location_municipality) LIKE LOWER(@{paramName})");
                        parameters.Add($"@{paramName}", $"%{filter.Value}%");
                    }

                    break;
            }
        }
    }

    private static Listing MapToListing(dynamic row) {
        string? categoryString = (string)row.category;
        Category category =
            Enum.Parse<Category>(categoryString.Replace(" & ", "And").Replace(" ", "").Replace(",", ""));

        return new Listing {
            ListingId = (Guid)row.listingid,
            Name = (string)row.name,
            Description = (string)row.description,
            Price = new Price { Currency = (string)row.currency, Amount = (decimal)row.amount },
            Category = category,
            Location = new Location {
                Country = (string)row.country, Municipality = (string)row.municipality, Geohash = (string)row.geohash
            }
        };
    }
}
