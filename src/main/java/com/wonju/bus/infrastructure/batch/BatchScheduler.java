package com.wonju.bus.infrastructure.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job publicDataRefreshJob;

    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    public void runPublicDataRefreshJob() {
        log.info("[BatchScheduler] 공공데이터 수집 배치 시작");
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("run.id", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(publicDataRefreshJob, params);
            log.info("[BatchScheduler] 공공데이터 수집 배치 완료");
        } catch (Exception e) {
            log.error("[BatchScheduler] 공공데이터 수집 배치 실패", e);
        }
    }
}
