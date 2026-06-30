package com.wonju.bus.common;

public class GeoUtils {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private GeoUtils() {}

    /**
     * EPSG:5179 (TM 좌표계, 단위: 미터) → WGS84 (위/경도) 근사 변환.
     * 정밀 변환이 필요하면 proj4j 라이브러리 사용 권장.
     */
    public static double[] tm5179ToWgs84(double tmX, double tmY) {
        double originLat = Math.toRadians(38.0);
        double originLon = Math.toRadians(127.0 + 3.0 / 60.0 + 14.8913 / 3600.0);

        double a = 6378137.0;
        double f = 1.0 / 298.257222101;
        double e2 = 2 * f - f * f;
        double k0 = 1.0;

        double x = tmX - 200000.0;
        double y = tmY - 600000.0;

        double M0 = a * ((1 - e2 / 4 - 3 * e2 * e2 / 64) * originLat
                - (3 * e2 / 8 + 3 * e2 * e2 / 32) * Math.sin(2 * originLat)
                + (15 * e2 * e2 / 256) * Math.sin(4 * originLat));

        double M = M0 + y / k0;
        double mu = M / (a * (1 - e2 / 4 - 3 * e2 * e2 / 64));

        double e1 = (1 - Math.sqrt(1 - e2)) / (1 + Math.sqrt(1 - e2));
        double phi1 = mu + (3 * e1 / 2 - 27 * e1 * e1 * e1 / 32) * Math.sin(2 * mu)
                + (21 * e1 * e1 / 16 - 55 * e1 * e1 * e1 * e1 / 32) * Math.sin(4 * mu)
                + (151 * e1 * e1 * e1 / 96) * Math.sin(6 * mu);

        double sinPhi1 = Math.sin(phi1);
        double N1 = a / Math.sqrt(1 - e2 * sinPhi1 * sinPhi1);
        double T1 = Math.tan(phi1) * Math.tan(phi1);
        double C1 = e2 / (1 - e2) * Math.cos(phi1) * Math.cos(phi1);
        double R1 = a * (1 - e2) / Math.pow(1 - e2 * sinPhi1 * sinPhi1, 1.5);
        double D = x / (N1 * k0);

        double lat = phi1 - (N1 * Math.tan(phi1) / R1) * (D * D / 2
                - (5 + 3 * T1 + 10 * C1 - 4 * C1 * C1 - 9 * e2 / (1 - e2)) * D * D * D * D / 24);
        double lon = originLon + (D - (1 + 2 * T1 + C1) * D * D * D / 6) / Math.cos(phi1);

        return new double[]{Math.toDegrees(lat), Math.toDegrees(lon)};
    }

    /**
     * 두 좌표(WGS84) 사이 거리 계산 (km, Haversine 공식).
     */
    public static double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return EARTH_RADIUS_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
