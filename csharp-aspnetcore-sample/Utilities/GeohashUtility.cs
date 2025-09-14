namespace csharp_aspnetcore_sample.Utilities;

public static class GeohashUtility {
    private static readonly char[] Base32 = "0123456789bcdefghjkmnpqrstuvwxyz".ToCharArray();
    private static readonly Dictionary<char, int> Base32Map = new();

    static GeohashUtility() {
        for (int i = 0; i < Base32.Length; i++) {
            Base32Map[Base32[i]] = i;
        }
    }

    public static (double Latitude, double Longitude) DecodeGeohash(string geohash) {
        if (string.IsNullOrEmpty(geohash)) {
            throw new ArgumentException("Geohash cannot be null or empty", nameof(geohash));
        }

        double[] latRange = { -90.0, 90.0 };
        double[] lonRange = { -180.0, 180.0 };
        bool isEvenBit = true;

        foreach (char c in geohash.ToLower()) {
            if (!Base32Map.TryGetValue(c, out int cd)) {
                throw new ArgumentException($"Invalid geohash character: {c}", nameof(geohash));
            }

            for (int i = 4; i >= 0; i--) {
                int bit = (cd >> i) & 1;
                if (isEvenBit) {
                    // longitude
                    double mid = (lonRange[0] + lonRange[1]) / 2;
                    if (bit == 1) {
                        lonRange[0] = mid;
                    } else {
                        lonRange[1] = mid;
                    }
                } else {
                    // latitude
                    double mid = (latRange[0] + latRange[1]) / 2;
                    if (bit == 1) {
                        latRange[0] = mid;
                    } else {
                        latRange[1] = mid;
                    }
                }
                isEvenBit = !isEvenBit;
            }
        }

        double lat = (latRange[0] + latRange[1]) / 2;
        double lon = (lonRange[0] + lonRange[1]) / 2;

        return (lat, lon);
    }
}

public static class DistanceCalculator {
    private const double EarthRadiusKm = 6371.0;

    /// <summary>
    /// Calculate the distance between two points on Earth using the Haversine formula
    /// </summary>
    /// <param name="lat1">Latitude of first point in degrees</param>
    /// <param name="lon1">Longitude of first point in degrees</param>
    /// <param name="lat2">Latitude of second point in degrees</param>
    /// <param name="lon2">Longitude of second point in degrees</param>
    /// <returns>Distance in kilometers</returns>
    public static double CalculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        var lat1Rad = DegreesToRadians(lat1);
        var lat2Rad = DegreesToRadians(lat2);
        var deltaLatRad = DegreesToRadians(lat2 - lat1);
        var deltaLonRad = DegreesToRadians(lon2 - lon1);

        var a = Math.Sin(deltaLatRad / 2) * Math.Sin(deltaLatRad / 2) +
                Math.Cos(lat1Rad) * Math.Cos(lat2Rad) *
                Math.Sin(deltaLonRad / 2) * Math.Sin(deltaLonRad / 2);

        var c = 2 * Math.Atan2(Math.Sqrt(a), Math.Sqrt(1 - a));

        return EarthRadiusKm * c;
    }

    private static double DegreesToRadians(double degrees) {
        return degrees * Math.PI / 180.0;
    }
}