package com.wonju.bus.interfaces.admin.dto;

import com.wonju.bus.domain.route.RouteRecommendation;

public record RouteRecommendResponse(
        Long id,
        String areaCode,
        String blindSpotId,
        String recommendType,
        String description,
        String routePath,
        Integer priorityScore,
        Long estimatedBeneficiaries,
        String status
) {
    public static RouteRecommendResponse from(RouteRecommendation rec) {
        return new RouteRecommendResponse(
                rec.getId(),
                rec.getAreaCode(),
                rec.getBlindSpotId(),
                rec.getRecommendType().name(),
                rec.getDescription(),
                rec.getRoutePath(),
                rec.getPriorityScore(),
                rec.getEstimatedBeneficiaries(),
                rec.getStatus()
        );
    }
}
