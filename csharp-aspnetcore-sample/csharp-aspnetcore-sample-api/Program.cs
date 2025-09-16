using System.Text.Json.Serialization;
using csharp_aspnetcore_sample.Repositories;
using csharp_aspnetcore_sample.Services;
using Microsoft.OpenApi.Models;

WebApplicationBuilder builder = WebApplication.CreateBuilder(args);

// Add services to the container.
builder.Services.AddControllers()
    .AddJsonOptions(options => {
        options.JsonSerializerOptions.Converters.Add(new JsonStringEnumConverter());
    });

// Register repository
builder.Services.AddScoped<IListingRepository, ListingRepository>();

// Register sorting services
builder.Services.AddScoped<ISortingService, SortingService>();

builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen(c => {
    c.SwaggerDoc("v1",
        new OpenApiInfo {
            Title = "Marketplace API",
            Version = "v1",
            Description = "A simple marketplace API for managing product listings"
        });
});

WebApplication app = builder.Build();

// Configure the HTTP request pipeline.
app.UseSwagger();
app.UseSwaggerUI();

app.UseHttpsRedirection();
app.UseAuthorization();
app.MapControllers();

app.Run();

// Make Program class accessible for testing
public partial class Program {
}
