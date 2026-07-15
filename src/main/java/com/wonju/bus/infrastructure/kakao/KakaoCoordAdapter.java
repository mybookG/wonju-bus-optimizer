package com.wonju.bus.infrastructure.kakao;

import com.wonju.bus.application.port.CoordTransformPort;
import com.wonju.bus.common.BusServiceException;
import com.wonju.bus.common.ErrorCode;
import com.wonju.bus.common.HttpClientSupport;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoCoordAdapter extends HttpClientSupport implements CoordTransformPort {

    private final KakaoCoordProperties properties;

    private WebClient webClient;

    @PostConstruct
    public void init() {
        this.webClient = buildWebClient(properties.getBaseUrl());
    }

    @Override
    public double[] tm5179ToWgs84(double tmX, double tmY) {
        log.info("[KakaoCoordAdapter] TM→WGS84 변환 - tmX={}, tmY={}", tmX, tmY);

        try {
            KakaoTransCoordResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v2/local/geo/transcoord.json")
                            .queryParam("x", tmX)
                            .queryParam("y", tmY)
                            .queryParam("input_coord", "TM")
                            .queryParam("output_coord", "WGS84")
                            .build())
                    .header("Authorization", "KakaoAK " + properties.getRestApiKey())
                    .retrieve()
                    .bodyToMono(KakaoTransCoordResponse.class)
                    .block();

            if (response == null || response.documents() == null || response.documents().isEmpty()) {
                log.warn("[KakaoCoordAdapter] 변환 결과 없음 - tmX={}, tmY={}", tmX, tmY);
                throw new BusServiceException(ErrorCode.COORD_TRANSFORM_FAILED);
            }

            Document doc = response.documents().get(0);
            return new double[]{doc.y(), doc.x()};
        } catch (BusServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("[KakaoCoordAdapter] 좌표 변환 실패 - tmX={}, tmY={}", tmX, tmY, e);
            throw new BusServiceException(ErrorCode.COORD_TRANSFORM_FAILED, e);
        }
    }

    private record KakaoTransCoordResponse(List<Document> documents) {}

    private record Document(double x, double y) {}
}
