-- Marketplace API Database Schema
-- PostgreSQL Schema for Listings

-- Create extension for UUID generation
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create the listings table
CREATE TABLE IF NOT EXISTS listings (
    listing_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    price_currency VARCHAR(3) NOT NULL,
    price_amount NUMERIC(19, 2) NOT NULL CHECK (price_amount > 0),
    category VARCHAR(50) NOT NULL CHECK (category IN (
        'Electronics', 
        'Fashion', 
        'Home & Garden', 
        'Motors', 
        'Collectibles & Art', 
        'Sporting Goods', 
        'Toys & Hobbies', 
        'Business & Industrial', 
        'Music', 
        'Health & Beauty', 
        'Books', 
        'Cameras & Photo', 
        'Computers, Tablets & Networking', 
        'Cell Phones & Accessories', 
        'Video Games & Consoles'
    )),
    location_country VARCHAR(2) NOT NULL,
    location_municipality VARCHAR(255) NOT NULL,
    location_geohash VARCHAR(7) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_listings_category ON listings(category);
CREATE INDEX IF NOT EXISTS idx_listings_location_country ON listings(location_country);
CREATE INDEX IF NOT EXISTS idx_listings_price_amount ON listings(price_amount);
CREATE INDEX IF NOT EXISTS idx_listings_created_at ON listings(created_at);

-- Create a trigger to update the updated_at column
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_listings_updated_at
    BEFORE UPDATE ON listings
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();