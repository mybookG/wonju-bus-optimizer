package com.wonju.bus.application;

import com.wonju.bus.application.port.AiAnalysisPort;
import com.wonju.bus.common.BusServiceException;
import com.wonju.bus.common.ErrorCode;
import com.wonju.bus.domain.demand.BlindSpot;
import com.wonju.bus.domain.demand.repository.BlindSpotRepository;
import com.wonju.bus.domain.route.RecommendType;
import com.wonju.bus.domain.route.RecommendationStatus;
import com.wonju.bus.domain.route.RouteRecommendation;
import com.wonju.bus.domain.route.repository.RouteRecommendationRepository;
import com.wonju.bus.interfaces.admin.dto.RouteRecommendResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouteRecommendService {

    private final AiAnalysisPort aiAnalysisPort;
    private final BlindSpotRepository blindSpotRepository;
    private final RouteRecommendationRepository recommendationRepository;

    @Transactional
    public RouteRecommendResponse generateRecommendation(Long blindSpotPk) {
        log.info("[RouteRecommendService] 노선 추천 생성 시작 - blindSpotId={}", blindSpotPk);

        BlindSpot blindSpot = blindSpotRepository.findById(blindSpotPk)
                .orElseThrow(() -> new BusServiceException(ErrorCode.BLIND_SPOT_NOT_FOUND));

        AiAnalysisPort.RouteRecommendInput input = new AiAnalysisPort.RouteRecommendInput(
                blindSpot.getBlindSpotId(),
                blindSpot.getAreaCode(),
                blindSpot.getAreaName(),
                blindSpot.getDemandScore(),
                Collections.emptyMap()
        );

        AiAnalysisPort.RouteRecommendResult result;
        try {
            result = aiAnalysisPort.recommendRoute(input);
        } catch (Exception e) {
            log.warn("[RouteRecommendService] AI 추천 실패 - blindSpotId={}", blindSpotPk, e);
            throw new BusServiceException(ErrorCode.ROUTE_RECOMMEND_FAILED, e);
        }

        RouteRecommendation recommendation = RouteRecommendation.builder()
                .blindSpot(blindSpot)
                .areaCode(blindSpot.getAreaCode())
                .recommendType(RecommendType.valueOf(result.recommendType()))
                .description(result.description())
                .routePath(result.routePath())
                .priorityScore(result.priorityScore())
                .estimatedBeneficiaries(result.estimatedBeneficiaries())
                .build();

        recommendationRepository.save(recommendation);
        log.info("[RouteRecommendService] 노선 추천 생성 완료 - blindSpotId={}, type={}", blindSpotPk, result.recommendType());
        return new RouteRecommendResponse(
                recommendation.getId(),
                recommendation.getAreaCode(),
                blindSpotPk,
                recommendation.getRecommendType().name(),
                recommendation.getDescription(),
                recommendation.getRoutePath(),
                recommendation.getPriorityScore(),
                recommendation.getEstimatedBeneficiaries(),
                recommendation.getStatus().name()
        );
    }

    @Transactional(readOnly = true)
    public Page<RouteRecommendResponse> getRecommendationsByArea(String areaCode, Pageable pageable) {
        return recommendationRepository.findByAreaCode(areaCode, pageable)
                .map(RouteRecommendResponse::from);
    }

    @Transactional(readOnly = true)
    public List<RouteRecommendResponse> getPendingRecommendations() {
        return recommendationRepository.findByStatusOrderByPriorityScoreDesc(RecommendationStatus.PENDING)
                .stream()
                .map(RouteRecommendResponse::from)
                .toList();
    }

    @Transactional
    public RouteRecommendResponse approveRecommendation(Long id) {
        RouteRecommendation recommendation = recommendationRepository.findById(id)
                .orElseThrow(() -> new BusServiceException(ErrorCode.RECOMMENDATION_NOT_FOUND));
        recommendation.approve();
        log.info("[RouteRecommendService] 노선 추천 승인 - id={}", id);
        return RouteRecommendResponse.from(recommendation);
    }

    @Transactional
    public RouteRecommendResponse rejectRecommendation(Long id, String reason) {
        RouteRecommendation recommendation = recommendationRepository.findById(id)
                .orElseThrow(() -> new BusServiceException(ErrorCode.RECOMMENDATION_NOT_FOUND));
        recommendation.reject(reason);
        log.info("[RouteRecommendService] 노선 추천 반려 - id={}, reason={}", id, reason);
        return RouteRecommendResponse.from(recommendation);
    }
}
