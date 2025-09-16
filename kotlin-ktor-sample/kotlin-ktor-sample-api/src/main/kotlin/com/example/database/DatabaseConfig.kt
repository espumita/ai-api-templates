package com.example.database

import org.postgresql.ds.PGSimpleDataSource
import javax.sql.DataSource

object DatabaseConfig {
    
    fun createDataSource(
        url: String = System.getenv("DATABASE_URL") ?: "jdbc:postgresql://localhost:5432/marketplace",
        username: String = System.getenv("DATABASE_USERNAME") ?: "postgres", 
        password: String = System.getenv("DATABASE_PASSWORD") ?: "postgres"
    ): DataSource {
        val dataSource = PGSimpleDataSource()
        dataSource.setURL(url)
        dataSource.user = username
        dataSource.password = password
        
        return dataSource
    }
}