package com.wonju.bus.interfaces.admin;

import com.wonju.bus.application.DemandAnalysisService;
import com.wonju.bus.common.ApiResponse;
import com.wonju.bus.interfaces.admin.dto.BlindSpotResponse;
import com.wonju.bus.interfaces.admin.dto.DemandScoreResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/demand")
@RequiredArgsConstructor
public class DemandController {

    private final DemandAnalysisService demandAnalysisService;

    @GetMapping("/scores")
    public ResponseEntity<ApiResponse<List<DemandScoreResponse>>> getLatestScores() {
        log.info("[DemandController] 수요 점수 조회");
        List<DemandScoreResponse> responses = demandAnalysisService.getLatestScores().stream()
                .map(DemandScoreResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/blind-spots")
    public ResponseEntity<ApiResponse<List<BlindSpotResponse>>> getBlindSpots() {
        log.info("[DemandController] 사각지대 목록 조회");
        List<BlindSpotResponse> responses = demandAnalysisService.getUnresolvedBlindSpots().stream()
                .map(BlindSpotResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
