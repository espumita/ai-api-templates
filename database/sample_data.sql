-- Sample Data for Marketplace API
-- Insert 10 sample listings to populate the database

-- Clear existing data (optional - uncomment if needed)
-- TRUNCATE TABLE listings;

-- Insert sample listings
INSERT INTO listings (
    listing_id,
    name,
    description,
    price_currency,
    price_amount,
    category,
    location_country,
    location_municipality,
    location_geohash,
    created_at
) VALUES 
-- 1. Electronics - iPhone
(
    '550e8400-e29b-41d4-a716-446655440001',
    'iPhone 15 Pro Max 256GB',
    'Brand new iPhone 15 Pro Max in Space Black with 256GB storage. Includes original box, charger, and one year warranty.',
    'USD',
    1199.99,
    'Electronics',
    'US',
    'San Francisco',
    'dr5regw',
    NOW() - INTERVAL '5 days'
),

-- 2. Fashion - Designer Jacket
(
    '550e8400-e29b-41d4-a716-446655440002',
    'Vintage Leather Jacket',
    'Authentic 1980s leather jacket in excellent condition. Made from genuine cowhide leather with original tags.',
    'EUR',
    350.00,
    'Fashion',
    'DE',
    'Berlin',
    'u33dbf5',
    NOW() - INTERVAL '3 days'
),

-- 3. Home & Garden - Coffee Table
(
    '550e8400-e29b-41d4-a716-446655440003',
    'Mid-Century Modern Coffee Table',
    'Beautiful walnut coffee table with clean lines and tapered legs. Perfect for modern living rooms.',
    'USD',
    485.00,
    'Home & Garden',
    'US',
    'Austin',
    'dr5regw',
    NOW() - INTERVAL '7 days'
),

-- 4. Motors - Motorcycle
(
    '550e8400-e29b-41d4-a716-446655440004',
    '2019 Yamaha R6',
    'Excellent condition 2019 Yamaha R6 with only 8,500 miles. Recently serviced with new tires and chain.',
    'USD',
    12500.00,
    'Motors',
    'US',
    'Miami',
    'dhwjd0j',
    NOW() - INTERVAL '10 days'
),

-- 5. Music - Guitar
(
    '550e8400-e29b-41d4-a716-446655440005',
    '1970s Fender Stratocaster',
    'Vintage 1975 Fender Stratocaster in sunburst finish. Original pickups and hardware. Includes hard case.',
    'USD',
    2800.00,
    'Music',
    'US',
    'Nashville',
    'dn5h0b8',
    NOW() - INTERVAL '2 days'
),

-- 6. Books - Rare Book
(
    '550e8400-e29b-41d4-a716-446655440006',
    'First Edition Lord of the Rings Set',
    'Complete first edition set of The Lord of the Rings trilogy by J.R.R. Tolkien. Excellent condition with dust jackets.',
    'GBP',
    1500.00,
    'Books',
    'GB',
    'London',
    'gcpuvpk',
    NOW() - INTERVAL '1 day'
),

-- 7. Sporting Goods - Mountain Bike
(
    '550e8400-e29b-41d4-a716-446655440007',
    'Trek Mountain Bike Full Suspension',
    'Trek Fuel EX 9.8 full suspension mountain bike. Size large, excellent condition with recent tune-up.',
    'CAD',
    3200.00,
    'Sporting Goods',
    'CA',
    'Vancouver',
    'c2b2r2j',
    NOW() - INTERVAL '6 days'
),

-- 8. Collectibles & Art - Painting
(
    '550e8400-e29b-41d4-a716-446655440008',
    'Original Abstract Oil Painting',
    'Contemporary abstract oil painting by local artist. 24x36 inches, vibrant colors, comes with certificate of authenticity.',
    'USD',
    750.00,
    'Collectibles & Art',
    'US',
    'New York',
    'dr5ru7h',
    NOW() - INTERVAL '4 days'
),

-- 9. Video Games & Consoles - Gaming Setup
(
    '550e8400-e29b-41d4-a716-446655440009',
    'PlayStation 5 Console Bundle',
    'PlayStation 5 console with extra controller, charging station, and 5 popular games including Spider-Man 2.',
    'USD',
    650.00,
    'Video Games & Consoles',
    'US',
    'Los Angeles',
    'dr72gm4',
    NOW() - INTERVAL '8 days'
),

-- 10. Health & Beauty - Skincare Set
(
    '550e8400-e29b-41d4-a716-446655440010',
    'Premium Skincare Gift Set',
    'Luxury skincare set including cleanser, serum, moisturizer, and eye cream. All products are organic and cruelty-free.',
    'USD',
    125.00,
    'Health & Beauty',
    'US',
    'Seattle',
    'dr72gm4',
    NOW() - INTERVAL '12 days'
);

-- Verify the data was inserted
SELECT COUNT(*) as total_listings FROM listings;
SELECT category, COUNT(*) as count_per_category FROM listings GROUP BY category ORDER BY category;