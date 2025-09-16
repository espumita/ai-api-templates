# Build stage
FROM mcr.microsoft.com/dotnet/sdk:9.0 AS build
WORKDIR /src
COPY csharp-aspnetcore-sample-api/ ./csharp-aspnetcore-sample-api/
RUN dotnet restore ./csharp-aspnetcore-sample-api/csharp-aspnetcore-sample.csproj
RUN dotnet build ./csharp-aspnetcore-sample-api/csharp-aspnetcore-sample.csproj -c Release --no-restore -o /app/build
RUN dotnet publish ./csharp-aspnetcore-sample-api/csharp-aspnetcore-sample.csproj -c Release --no-build -o /app/publish /p:UseAppHost=false

# Runtime stage
FROM mcr.microsoft.com/dotnet/aspnet:9.0 AS final
WORKDIR /app
COPY --from=build /app/publish .
ENV ASPNETCORE_URLS=http://+:80
EXPOSE 80
ENTRYPOINT ["dotnet", "csharp-aspnetcore-sample.dll"]
