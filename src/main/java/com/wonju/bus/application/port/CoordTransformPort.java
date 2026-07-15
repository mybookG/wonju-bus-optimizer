package com.wonju.bus.application.port;

public interface CoordTransformPort {

    /**
     * EPSG:5179 (TM 좌표계, 단위: 미터) → WGS84 (위도, 경도) 변환.
     *
     * @return double[]{latitude, longitude}
     */
    double[] tm5179ToWgs84(double tmX, double tmY);
}
