package com.wonju.bus.infrastructure.publicdata;

import com.fasterxml.jackson.databind.JsonNode;
import com.wonju.bus.common.BusServiceException;
import com.wonju.bus.common.ErrorCode;
import com.wonju.bus.common.HttpClientSupport;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TagoFetcher extends HttpClientSupport implements PublicDataFetcher {

    private final PublicApiProperties properties;
    private WebClient webClient;

    @PostConstruct
    public void init() {
        this.webClient = buildWebClient(properties.getTago().getBaseUrl());
    }

    @Override
    @Cacheable(value = "cache:tago", key = "#areaCode")
    public List<JsonNode> fetch(String areaCode) {
        log.info("[TagoFetcher] 버스 노선 데이터 수집 - areaCode={}", areaCode);
        try {
            JsonNode response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/BusRouteInfoInqireService/getRouteNoList")
                            .queryParam("serviceKey", properties.getTago().getKey())
                            .queryParam("cityCode", areaCode)
                            .queryParam("numOfRows", 100)
                            .queryParam("pageNo", 1)
                            .queryParam("_type", "json")
                            .build())
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            List<JsonNode> items = new ArrayList<>();
            if (response != null) {
                JsonNode itemList = response.path("response").path("body").path("items").path("item");
                if (itemList.isArray()) {
                    itemList.forEach(items::add);
                }
            }
            log.info("[TagoFetcher] 수집 완료 - areaCode={}, count={}", areaCode, items.size());
            return items;
        } catch (Exception e) {
            log.warn("[TagoFetcher] TAGO API 호출 실패 - areaCode={}", areaCode, e);
            throw new BusServiceException(ErrorCode.PUBLIC_API_UNAVAILABLE, e);
        }
    }

    @Override
    public String sourceName() {
        return "TAGO";
    }
}
