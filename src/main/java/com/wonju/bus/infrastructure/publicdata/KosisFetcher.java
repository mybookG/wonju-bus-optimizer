package com.wonju.bus.infrastructure.publicdata;

import com.fasterxml.jackson.databind.JsonNode;
import com.wonju.bus.common.BusServiceException;
import com.wonju.bus.common.ErrorCode;
import com.wonju.bus.common.HttpClientSupport;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class KosisFetcher extends HttpClientSupport implements PublicDataFetcher {

    private static final String WONJU_ORG_ID = "101";
    private static final String POPULATION_TABLE_ID = "DT_1B040A3";

    private final PublicApiProperties properties;
    private WebClient webClient;

    @PostConstruct
    public void init() {
        this.webClient = buildWebClient(properties.getKosis().getBaseUrl());
    }

    @Override
    public List<JsonNode> fetch(String areaCode) {
        log.info("[KosisFetcher] 인구 통계 수집 - areaCode={}", areaCode);
        try {
            JsonNode response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/statisticsData/getStatisticsList")
                            .queryParam("method", "getList")
                            .queryParam("apiKey", properties.getKosis().getKey())
                            .queryParam("orgId", WONJU_ORG_ID)
                            .queryParam("tblId", POPULATION_TABLE_ID)
                            .queryParam("objL1", areaCode)
                            .queryParam("format", "json")
                            .build())
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            List<JsonNode> items = new ArrayList<>();
            if (response != null && response.isArray()) {
                response.forEach(items::add);
            }
            log.info("[KosisFetcher] 수집 완료 - areaCode={}, count={}", areaCode, items.size());
            return items;
        } catch (Exception e) {
            log.warn("[KosisFetcher] KOSIS API 호출 실패 - areaCode={}", areaCode, e);
            throw new BusServiceException(ErrorCode.PUBLIC_API_UNAVAILABLE, e);
        }
    }

    @Override
    public String sourceName() {
        return "KOSIS";
    }
}
