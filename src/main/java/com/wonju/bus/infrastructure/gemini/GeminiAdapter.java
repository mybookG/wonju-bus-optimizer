package com.wonju.bus.infrastructure.gemini;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wonju.bus.application.port.AiAnalysisPort;
import com.wonju.bus.common.BusServiceException;
import com.wonju.bus.common.ErrorCode;
import com.wonju.bus.common.HttpClientSupport;
import com.wonju.bus.infrastructure.gemini.dto.GeminiRequest;
import com.wonju.bus.infrastructure.gemini.dto.GeminiResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiAdapter extends HttpClientSupport implements AiAnalysisPort {

    private final GeminiProperties properties;
    private final GeminiPromptBuilder promptBuilder;
    private final ObjectMapper objectMapper;

    private WebClient webClient;

    @PostConstruct
    public void init() {
        this.webClient = buildWebClient(properties.getBaseUrl());
    }

    @Override
    public DemandAnalysisResult analyzeDemand(DemandAnalysisInput input) {
        String prompt = promptBuilder.buildDemandAnalysisPrompt(input) + GeminiPromptBuilder.JSON_INSTRUCTION;
        String json = callGemini(prompt);
        log.info("[GeminiAdapter] 수요 분석 응답 - areaCode={}", input.areaCode());
        log.debug("[GeminiAdapter] raw response - {}", json);

        try {
            JsonNode node = objectMapper.readTree(json);
            return new DemandAnalysisResult(
                    node.path("demandScore").asInt(),
                    node.path("supplyIndex").asDouble(),
                    node.path("analysisReason").asText(),
                    json
            );
        } catch (Exception e) {
            log.error("[GeminiAdapter] 수요 분석 응답 파싱 실패", e);
            throw new BusServiceException(ErrorCode.AI_ANALYSIS_FAILED, e);
        }
    }

    @Override
    public RouteRecommendResult recommendRoute(RouteRecommendInput input) {
        String prompt = promptBuilder.buildRouteRecommendPrompt(input) + GeminiPromptBuilder.JSON_INSTRUCTION;
        String json = callGemini(prompt);
        log.info("[GeminiAdapter] 노선 추천 응답 - blindSpotId={}", input.blindSpotId());

        try {
            JsonNode node = objectMapper.readTree(json);
            return new RouteRecommendResult(
                    node.path("recommendType").asText(),
                    node.path("description").asText(),
                    node.path("routePath").asText(),
                    node.path("priorityScore").asInt(),
                    node.path("estimatedBeneficiaries").asLong()
            );
        } catch (Exception e) {
            log.error("[GeminiAdapter] 노선 추천 응답 파싱 실패", e);
            throw new BusServiceException(ErrorCode.AI_ANALYSIS_FAILED, e);
        }
    }

    @Override
    public ComplaintClassifyResult classifyComplaint(String complaintText) {
        String prompt = promptBuilder.buildComplaintClassifyPrompt(complaintText) + GeminiPromptBuilder.JSON_INSTRUCTION;
        String json = callGemini(prompt);
        log.info("[GeminiAdapter] 민원 분류 완료");

        try {
            JsonNode node = objectMapper.readTree(json);
            return new ComplaintClassifyResult(
                    node.path("category").asText(),
                    node.path("severityScore").asInt(),
                    json
            );
        } catch (Exception e) {
            log.error("[GeminiAdapter] 민원 분류 응답 파싱 실패", e);
            throw new BusServiceException(ErrorCode.AI_ANALYSIS_FAILED, e);
        }
    }

    private String callGemini(String prompt) {
        String url = "/v1beta/models/%s:generateContent?key=%s"
                .formatted(properties.getModel(), properties.getApiKey());

        GeminiResponse response = webClient.post()
                .uri(url)
                .bodyValue(GeminiRequest.of(prompt))
                .retrieve()
                .bodyToMono(GeminiResponse.class)
                .block();

        if (response == null) {
            throw new BusServiceException(ErrorCode.AI_ANALYSIS_FAILED);
        }
        return response.extractText();
    }
}
