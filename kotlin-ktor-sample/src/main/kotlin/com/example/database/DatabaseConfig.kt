package com.example.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource

object DatabaseConfig {
    
    fun createDataSource(
        url: String = System.getenv("DATABASE_URL") ?: "jdbc:postgresql://localhost:5432/marketplace",
        username: String = System.getenv("DATABASE_USERNAME") ?: "postgres", 
        password: String = System.getenv("DATABASE_PASSWORD") ?: "postgres"
    ): DataSource {
        val config = HikariConfig().apply {
            jdbcUrl = url
            this.username = username
            this.password = password
            driverClassName = "org.postgresql.Driver"
            
            // Connection pool settings
            maximumPoolSize = 10
            minimumIdle = 2
            connectionTimeout = 30000
            idleTimeout = 600000
            maxLifetime = 1800000
            
            // Connection validation
            connectionTestQuery = "SELECT 1"
            isAutoCommit = true
        }
        
        return HikariDataSource(config)
    }
}