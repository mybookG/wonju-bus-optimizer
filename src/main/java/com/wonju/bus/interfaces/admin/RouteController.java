package com.wonju.bus.interfaces.admin;

import com.wonju.bus.application.RouteRecommendService;
import com.wonju.bus.common.ApiResponse;
import com.wonju.bus.interfaces.admin.dto.RouteRecommendResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
public class RouteController {

    private final RouteRecommendService routeRecommendService;

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<RouteRecommendResponse>>> getPending() {
        log.info("[RouteController] 대기 중 노선 추천 목록 조회");
        List<RouteRecommendResponse> responses = routeRecommendService.getPendingRecommendations().stream()
                .map(RouteRecommendResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PostMapping("/blind-spots/{blindSpotId}/generate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RouteRecommendResponse>> generate(@PathVariable String blindSpotId) {
        log.info("[RouteController] 노선 추천 생성 - blindSpotId={}", blindSpotId);
        RouteRecommendResponse response = RouteRecommendResponse.from(
                routeRecommendService.generateRecommendation(blindSpotId));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RouteRecommendResponse>> approve(@PathVariable Long id) {
        log.info("[RouteController] 노선 추천 승인 - id={}", id);
        RouteRecommendResponse response = RouteRecommendResponse.from(
                routeRecommendService.approveRecommendation(id));
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
