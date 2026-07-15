package com.wonju.bus.infrastructure.gemini;

import com.wonju.bus.application.port.AiAnalysisPort;
import org.springframework.stereotype.Component;

@Component
public class GeminiPromptBuilder {

    public String buildDemandAnalysisPrompt(AiAnalysisPort.DemandAnalysisInput input) {
        return """
                다음 공공데이터를 분석해서 해당 지역의 버스 수요를 평가해주세요.

                지역: %s (%s)
                인구: %d명
                건축물 수: %d개
                복지시설 수: %d개
                현재 평균 배차 간격: %d분

                아래 JSON 형식으로만 응답하세요. 다른 텍스트는 포함하지 마세요.
                {
                  "demandScore": 0~100 사이 소수 (소수점 첫째 자리, 예: 72.5),
                  "supplyIndex": 0.0~1.0 사이 소수 (현재 공급 충족도),
                  "analysisReason": "분석 근거 (한국어, 100자 이내)"
                }
                """.formatted(
                input.areaName(), input.areaCode(),
                input.population(), input.buildingCount(),
                input.welfareFacilityCount(), input.averageIntervalMinutes()
        );
    }

    public String buildRouteRecommendPrompt(AiAnalysisPort.RouteRecommendInput input) {
        return """
                다음 버스 서비스 사각지대에 대한 노선 개선 방안을 제안해주세요.

                사각지대 ID: %s
                지역: %s (%s)
                수요 점수: %.1f / 100
                현황: 버스 서비스가 부족한 지역입니다.

                아래 JSON 형식으로만 응답하세요.
                {
                  "recommendType": "NEW_ROUTE|ROUTE_EXTENSION|FREQUENCY_INCREASE|DEMAND_RESPONSIVE 중 하나",
                  "description": "개선 방안 설명 (한국어, 200자 이내)",
                  "routePath": "제안 경로 (정류장명 → 정류장명 형식)",
                  "priorityScore": 0~100 사이 정수,
                  "estimatedBeneficiaries": 예상 수혜 인원 (정수)
                }
                """.formatted(
                input.blindSpotId(), input.areaName(), input.areaCode(), input.demandScore()
        );
    }

    public String buildComplaintClassifyPrompt(String complaintText) {
        return """
                다음 버스 불편 제보를 분류하고 불편 강도를 평가해주세요.

                제보 내용: %s

                아래 JSON 형식으로만 응답하세요.
                {
                  "category": "INSUFFICIENT_FREQUENCY|ROUTE_MISSING|STOP_MISSING|INCONVENIENT_TRANSFER|SAFETY_ISSUE|OTHER 중 하나",
                  "severityScore": 1~10 사이 정수 (1: 경미, 10: 매우 심각),
                  "summary": "분류 근거 요약 (한국어, 50자 이내)"
                }
                """.formatted(complaintText);
    }

    public static final String JSON_INSTRUCTION =
            "\n반드시 유효한 JSON만 반환하세요. 마크다운 코드 블록 없이 순수 JSON만 출력하세요.";
}
