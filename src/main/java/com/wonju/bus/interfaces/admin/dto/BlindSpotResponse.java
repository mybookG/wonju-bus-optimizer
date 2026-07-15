package com.wonju.bus.interfaces.admin.dto;

import com.wonju.bus.domain.demand.BlindSpot;

import java.time.LocalDate;

public record BlindSpotResponse(
        Long id,
        String blindSpotId,
        String areaCode,
        String areaName,
        Double demandScore,
        Double supplyIndex,
        Double centerLatitude,
        Double centerLongitude,
        LocalDate detectedDate,
        Boolean resolved
) {
    public static BlindSpotResponse from(BlindSpot blindSpot) {
        return new BlindSpotResponse(
                blindSpot.getId(),
                blindSpot.getBlindSpotId(),
                blindSpot.getAreaCode(),
                blindSpot.getAreaName(),
                blindSpot.getDemandScore(),
                blindSpot.getSupplyIndex(),
                blindSpot.getCenterLatitude(),
                blindSpot.getCenterLongitude(),
                blindSpot.getDetectedDate(),
                blindSpot.getResolved()
        );
    }
}
