package com.wonju.bus.application;

import com.wonju.bus.application.port.AiAnalysisPort;
import com.wonju.bus.common.BusServiceException;
import com.wonju.bus.common.ErrorCode;
import com.wonju.bus.domain.demand.BlindSpot;
import com.wonju.bus.domain.demand.DemandScore;
import com.wonju.bus.domain.demand.repository.BlindSpotRepository;
import com.wonju.bus.domain.demand.repository.DemandScoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DemandAnalysisService {

    private static final int BLIND_SPOT_DEMAND_THRESHOLD = 70;
    private static final double BLIND_SPOT_SUPPLY_THRESHOLD = 0.3;

    private final AiAnalysisPort aiAnalysisPort;
    private final DemandScoreRepository demandScoreRepository;
    private final BlindSpotRepository blindSpotRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public DemandScore analyze(String areaCode, String areaName,
                               long population, long buildingCount,
                               long welfareFacilityCount, int averageIntervalMinutes) {
        log.info("[DemandAnalysisService] 수요 분석 시작 - areaCode={}, areaName={}", areaCode, areaName);

        AiAnalysisPort.DemandAnalysisInput input = new AiAnalysisPort.DemandAnalysisInput(
                areaCode, areaName, population, buildingCount, welfareFacilityCount, averageIntervalMinutes);

        AiAnalysisPort.DemandAnalysisResult result;
        try {
            result = aiAnalysisPort.analyzeDemand(input);
        } catch (Exception e) {
            log.warn("[DemandAnalysisService] AI 분석 실패, 규칙 기반 fallback 적용 - areaCode={}", areaCode, e);
            result = fallbackDemandAnalysis(population, averageIntervalMinutes);
        }

        DemandScore score = DemandScore.builder()
                .areaCode(areaCode)
                .areaName(areaName)
                .demandScore(result.demandScore())
                .supplyIndex(result.supplyIndex())
                .population(population)
                .buildingCount(buildingCount)
                .welfareFacilityCount(welfareFacilityCount)
                .analyzedDate(LocalDate.now())
                .geminiRawResponse(result.rawResponse())
                .build();

        demandScoreRepository.save(score);
        log.info("[DemandAnalysisService] 수요 분석 완료 - areaCode={}, score={}", areaCode, result.demandScore());

        detectAndSaveBlindSpot(score, areaName);
        return score;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "cache:gemini:demand", key = "'today'")
    public List<DemandScore> getLatestScores() {
        return demandScoreRepository.findByAnalyzedDateOrderByDemandScoreDesc(LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<BlindSpot> getUnresolvedBlindSpots() {
        return blindSpotRepository.findByResolvedFalseOrderByDemandScoreDesc();
    }

    private void detectAndSaveBlindSpot(DemandScore score, String areaName) {
        boolean isBlindSpot = score.getDemandScore() >= BLIND_SPOT_DEMAND_THRESHOLD
                && score.getSupplyIndex() <= BLIND_SPOT_SUPPLY_THRESHOLD;

        if (isBlindSpot) {
            BlindSpot blindSpot = BlindSpot.builder()
                    .blindSpotId(UUID.randomUUID().toString())
                    .areaCode(score.getAreaCode())
                    .areaName(areaName)
                    .demandScore(score.getDemandScore())
                    .supplyIndex(score.getSupplyIndex())
                    .centerLatitude(0.0)
                    .centerLongitude(0.0)
                    .detectedDate(LocalDate.now())
                    .build();
            blindSpotRepository.save(blindSpot);
            log.info("[DemandAnalysisService] 사각지대 감지 - areaCode={}, score={}", score.getAreaCode(), score.getDemandScore());
        }
    }

    private AiAnalysisPort.DemandAnalysisResult fallbackDemandAnalysis(long population, int intervalMinutes) {
        double score = Math.min(100.0, population / 1000.0 * 10);
        double supplyIndex = intervalMinutes > 0 ? Math.min(1.0, 10.0 / intervalMinutes) : 0.0;
        return new AiAnalysisPort.DemandAnalysisResult(score, supplyIndex, "규칙 기반 fallback", null);
    }

    public void triggerReanalysis(String areaCode) {
        log.info("[DemandAnalysisService] 수요 재분석 트리거 - areaCode={}", areaCode);
        eventPublisher.publishEvent(new DemandReanalysisEvent(this, areaCode));
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    @TransactionalEventListener(fallbackExecution = true)
    public void onReanalysisRequested(DemandReanalysisEvent event) {
        log.info("[DemandAnalysisService] 재분석 이벤트 수신 - areaCode={}", event.areaCode());
        List<DemandScore> history = demandScoreRepository.findByAreaCodeOrderByAnalyzedDateDesc(event.areaCode());
        if (history.isEmpty()) {
            log.warn("[DemandAnalysisService] 재분석 대상 없음 - areaCode={}", event.areaCode());
            return;
        }
        DemandScore last = history.get(0);
        int inferredInterval = last.getSupplyIndex() > 0
                ? (int) Math.min(120, 10.0 / last.getSupplyIndex())
                : 60;
        analyze(last.getAreaCode(), last.getAreaName(), last.getPopulation(),
                last.getBuildingCount() != null ? last.getBuildingCount() : 0L,
                last.getWelfareFacilityCount() != null ? last.getWelfareFacilityCount() : 0L,
                inferredInterval);
    }

    public record DemandReanalysisEvent(Object source, String areaCode) {}
}
