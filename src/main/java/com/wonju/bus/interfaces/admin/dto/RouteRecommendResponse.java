package com.wonju.bus.interfaces.admin.dto;

import com.wonju.bus.domain.route.RouteRecommendation;

public record RouteRecommendResponse(
        Long id,
        String areaCode,
        Long blindSpotId,
        String recommendType,
        String description,
        String routePath,
        Double priorityScore,
        Long estimatedBeneficiaries,
        String status
) {
    public static RouteRecommendResponse from(RouteRecommendation rec) {
        return new RouteRecommendResponse(
                rec.getId(),
                rec.getAreaCode(),
                rec.getBlindSpot().getId(),
                rec.getRecommendType().name(),
                rec.getDescription(),
                rec.getRoutePath(),
                rec.getPriorityScore(),
                rec.getEstimatedBeneficiaries(),
                rec.getStatus().name()
        );
    }
}
