using csharp_aspnetcore_sample.Models;

namespace csharp_aspnetcore_sample.Repositories;

public interface IListingRepository
{
    Task<IEnumerable<Listing>> GetAllAsync(int page, int pageSize);
    Task<int> GetTotalCountAsync();
    Task<Listing?> GetByIdAsync(Guid id);
    Task<Listing> CreateAsync(Listing listing);
    Task<Listing?> UpdateAsync(Listing listing);
    Task<bool> DeleteAsync(Guid id);
    Task<bool> ExistsAsync(Guid id);
}