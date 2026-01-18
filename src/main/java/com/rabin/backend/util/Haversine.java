package com.rabin.backend.util;

public class Haversine {
    private static final double EARTH_RADIUS_KM = 6371.0;

    public static double distance(double userLat, double userLong, double eventLat, double eventLong){

        double latDistance = Math.toRadians(eventLat-userLat);
        double longDistance = Math.toRadians(eventLong-userLong);

        double a = Math.sin(latDistance/2)*Math.sin(latDistance/2)
                + Math.cos(Math.toRadians(userLat))
                * Math.cos(Math.toRadians(eventLat))
                * Math.sin(longDistance/2)*Math.sin(longDistance/2);

        double c = 2*Math.atan2(Math.sqrt(a),Math.sqrt(1-a));

        return EARTH_RADIUS_KM*c;
    }
}
