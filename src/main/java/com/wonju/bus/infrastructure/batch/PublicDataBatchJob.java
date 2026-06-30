package com.wonju.bus.infrastructure.batch;

import com.fasterxml.jackson.databind.JsonNode;
import com.wonju.bus.application.DemandAnalysisService;
import com.wonju.bus.domain.route.BusRoute;
import com.wonju.bus.domain.route.repository.BusRouteRepository;
import com.wonju.bus.infrastructure.publicdata.KosisFetcher;
import com.wonju.bus.infrastructure.publicdata.TagoFetcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class PublicDataBatchJob {

    private static final String WONJU_AREA_CODE = "42220";

    private final TagoFetcher tagoFetcher;
    private final KosisFetcher kosisFetcher;
    private final BusRouteRepository busRouteRepository;
    private final DemandAnalysisService demandAnalysisService;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job publicDataRefreshJob() {
        return new JobBuilder("publicDataRefreshJob", jobRepository)
                .start(collectBusRouteStep())
                .next(analyzeDemandStep())
                .build();
    }

    @Bean
    public Step collectBusRouteStep() {
        return new StepBuilder("collectBusRouteStep", jobRepository)
                .tasklet(collectBusRouteTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Step analyzeDemandStep() {
        return new StepBuilder("analyzeDemandStep", jobRepository)
                .tasklet(analyzeDemandTasklet(), transactionManager)
                .build();
    }

    private Tasklet collectBusRouteTasklet() {
        return (contribution, chunkContext) -> {
            log.info("[PublicDataBatchJob] 버스 노선 데이터 수집 시작 - areaCode={}", WONJU_AREA_CODE);
            try {
                List<JsonNode> routes = tagoFetcher.fetch(WONJU_AREA_CODE);
                routes.forEach(node -> {
                    String routeId = node.path("routeId").asText();
                    String routeName = node.path("routeNm").asText();
                    if (!routeId.isBlank() && !busRouteRepository.existsByRouteId(routeId)) {
                        BusRoute route = BusRoute.builder()
                                .routeId(routeId)
                                .routeName(routeName)
                                .routeType(node.path("routeTp").asText("일반"))
                                .areaCode(WONJU_AREA_CODE)
                                .intervalMinutes(node.path("headwaySe").asInt(30))
                                .build();
                        busRouteRepository.save(route);
                    }
                });
                log.info("[PublicDataBatchJob] 버스 노선 수집 완료 - count={}", routes.size());
            } catch (Exception e) {
                log.error("[PublicDataBatchJob] 버스 노선 수집 실패", e);
            }
            return RepeatStatus.FINISHED;
        };
    }

    private Tasklet analyzeDemandTasklet() {
        return (contribution, chunkContext) -> {
            log.info("[PublicDataBatchJob] 수요 분석 시작 - areaCode={}", WONJU_AREA_CODE);
            try {
                List<JsonNode> popData = kosisFetcher.fetch(WONJU_AREA_CODE);
                long population = popData.stream()
                        .mapToLong(n -> n.path("DT").asLong(0))
                        .sum();

                long avgInterval = (long) busRouteRepository.findByAreaCode(WONJU_AREA_CODE).stream()
                        .mapToInt(r -> r.getIntervalMinutes() != null ? r.getIntervalMinutes() : 30)
                        .average()
                        .orElse(30.0);

                demandAnalysisService.analyze(WONJU_AREA_CODE, "원주시", population, 0L, 0L, (int) avgInterval);
                log.info("[PublicDataBatchJob] 수요 분석 완료 - population={}", population);
            } catch (Exception e) {
                log.error("[PublicDataBatchJob] 수요 분석 실패", e);
            }
            return RepeatStatus.FINISHED;
        };
    }
}
