package com.wonju.bus.application.port;

import java.util.Map;

public interface AiAnalysisPort {

    /**
     * 읍·면·동 공공데이터를 입력받아 수요 점수(0~100)와 분석 근거를 반환.
     */
    DemandAnalysisResult analyzeDemand(DemandAnalysisInput input);

    /**
     * 사각지대 목록을 기반으로 노선 개선 방안 3가지를 반환.
     */
    RouteRecommendResult recommendRoute(RouteRecommendInput input);

    /**
     * 시민 제보 텍스트를 분류하고 불편 강도 점수를 반환.
     */
    ComplaintClassifyResult classifyComplaint(String complaintText);

    record DemandAnalysisInput(
            String areaCode,
            String areaName,
            long population,
            long buildingCount,
            long welfareFacilityCount,
            int averageIntervalMinutes
    ) {}

    record DemandAnalysisResult(
            double demandScore,
            double supplyIndex,
            String analysisReason,
            String rawResponse
    ) {}

    record RouteRecommendInput(
            String blindSpotId,
            String areaCode,
            String areaName,
            double demandScore,
            Map<String, Object> nearbyRoutes
    ) {}

    record RouteRecommendResult(
            String recommendType,
            String description,
            String routePath,
            double priorityScore,
            long estimatedBeneficiaries
    ) {}

    record ComplaintClassifyResult(
            String category,
            int severityScore,
            String rawResponse
    ) {}
}
