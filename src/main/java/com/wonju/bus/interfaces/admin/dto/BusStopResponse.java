package com.wonju.bus.interfaces.admin.dto;

import com.wonju.bus.domain.route.BusStop;

public record BusStopResponse(
        Long id,
        String stopId,
        String stopName,
        Double latitude,
        Double longitude,
        String areaCode
) {
    public static BusStopResponse from(BusStop stop) {
        return new BusStopResponse(
                stop.getId(),
                stop.getStopId(),
                stop.getStopName(),
                stop.getLatitude(),
                stop.getLongitude(),
                stop.getAreaCode()
        );
    }
}
