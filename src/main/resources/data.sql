CREATE FUNCTION IF NOT EXISTS haversine_distance(lat1 FLOAT, lon1 FLOAT, lat2 FLOAT, lon2 FLOAT)
RETURNS FLOAT
DETERMINISTIC
RETURN 6371 * acos(
    cos(radians(lat2)) * cos(radians(lat1))
    * cos(radians(lon1) - radians(lon2))
    + sin(radians(lat2)) * sin(radians(lat1))
);