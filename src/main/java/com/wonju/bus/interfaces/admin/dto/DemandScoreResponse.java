package com.wonju.bus.interfaces.admin.dto;

import com.wonju.bus.domain.demand.DemandScore;

import java.time.LocalDate;

public record DemandScoreResponse(
        Long id,
        String areaCode,
        String areaName,
        Integer demandScore,
        Double supplyIndex,
        Long population,
        LocalDate analyzedDate
) {
    public static DemandScoreResponse from(DemandScore score) {
        return new DemandScoreResponse(
                score.getId(),
                score.getAreaCode(),
                score.getAreaName(),
                score.getDemandScore(),
                score.getSupplyIndex(),
                score.getPopulation(),
                score.getAnalyzedDate()
        );
    }
}
