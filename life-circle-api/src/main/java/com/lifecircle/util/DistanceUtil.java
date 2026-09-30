package com.lifecircle.util;

public class DistanceUtil {

    private static final double EARTH_RADIUS = 6371000; // 地球半径，单位：米

    /**
     * 使用 Haversine 公式计算两点之间的距离
     * @param lng1 经度 1
     * @param lat1 纬度 1
     * @param lng2 经度 2
     * @param lat2 纬度 2
     * @return 距离，单位：米
     */
    public static double calcDistance(double lng1, double lat1, double lng2, double lat2) {
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }
}
