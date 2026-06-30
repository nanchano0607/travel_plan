package com.min.edu.plan.place;

import java.math.BigDecimal;

public class GeoDistanceCalculator {

    private static final double EARTH_RADIUS_METERS = 6371000.0;

    private GeoDistanceCalculator() {
    }

    public static double distanceMeters(
            BigDecimal latitude1,
            BigDecimal longitude1,
            BigDecimal latitude2,
            BigDecimal longitude2
    ) {
        double lat1 = Math.toRadians(latitude1.doubleValue());
        double lat2 = Math.toRadians(latitude2.doubleValue());
        double deltaLat = Math.toRadians(latitude2.subtract(latitude1).doubleValue());
        double deltaLon = Math.toRadians(longitude2.subtract(longitude1).doubleValue());

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_METERS * c;
    }
}
