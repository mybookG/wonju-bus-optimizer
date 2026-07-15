package com.wonju.bus.interfaces.admin.dto;

import com.wonju.bus.domain.route.BusRoute;

public record BusRouteResponse(
        Long id,
        String routeId,
        String routeName,
        String routeType,
        String startStop,
        String endStop,
        Integer intervalMinutes,
        String areaCode
) {
    public static BusRouteResponse from(BusRoute route) {
        return new BusRouteResponse(
                route.getId(),
                route.getRouteId(),
                route.getRouteName(),
                route.getRouteType(),
                route.getStartStop(),
                route.getEndStop(),
                route.getIntervalMinutes(),
                route.getAreaCode()
        );
    }
}
