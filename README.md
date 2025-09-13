# AI-Generated API Templates

This repository contains AI-generated example implementations of a marketplace API in multiple programming languages.

Each implementation follows the same API specification while adhering to language-specific best practices and sharing common database infrastructure.

## Available Implementations

- **C# ASP.NET Core** (`csharp-aspnetcore-sample/`) 
- **Kotlin Ktor** (`kotlin-ktor-sample/`)

## Shared Infrastructure

To avoid duplication, the following components are shared across all implementations:

- **Database Schema** (`database/schema.sql`) - PostgreSQL schema definition
- **Docker Compose** (`docker-compose.yml`) - PostgreSQL service configuration

## Quick Start

1. **Start the shared PostgreSQL database:**
   ```bash
   docker-compose up -d
   ```

2. **Choose and run your preferred implementation:**
   - For C#: See `csharp-aspnetcore-sample/README.md`
   - For Kotlin: See `kotlin-ktor-sample/README.md`

3. **Stop the database when done:**
   ```bash
   docker-compose down
   ```

## Documentation

For detailed API specifications, data models, and implementation guidelines, see [copilot-instructions.md](.github/copilot-instructions.md).

## Docker Support

Both implementations include Docker support for easy deployment, and share a common PostgreSQL database service.

## Note

This project was generated with AI assistance to demonstrate consistent API implementation patterns across different programming languages and frameworks. Feel free to contribute additional language implementations that maintain feature parity with existing templates.
