package com.wonju.bus.interfaces.admin;

import com.wonju.bus.application.RouteRecommendService;
import com.wonju.bus.common.ApiResponse;
import com.wonju.bus.interfaces.admin.dto.RouteRecommendResponse;
import com.wonju.bus.interfaces.admin.dto.RejectRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/recommendations")
@RequiredArgsConstructor
public class RouteController {

    private final RouteRecommendService routeRecommendService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RouteRecommendResponse>>> getPending() {
        log.info("[RouteController] 대기 중 노선 추천 목록 조회");
        return ResponseEntity.ok(ApiResponse.success(routeRecommendService.getPendingRecommendations()));
    }

    @PostMapping("/blind-spots/{blindSpotId}/generate")
    public ResponseEntity<ApiResponse<RouteRecommendResponse>> generate(@PathVariable Long blindSpotId) {
        log.info("[RouteController] 노선 추천 생성 - blindSpotId={}", blindSpotId);
        return ResponseEntity.ok(ApiResponse.success(routeRecommendService.generateRecommendation(blindSpotId)));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<RouteRecommendResponse>> approve(@PathVariable Long id) {
        log.info("[RouteController] 노선 추천 승인 - id={}", id);
        return ResponseEntity.ok(ApiResponse.success(routeRecommendService.approveRecommendation(id)));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<RouteRecommendResponse>> reject(
            @PathVariable Long id,
            @Valid @RequestBody RejectRequest request) {
        log.info("[RouteController] 노선 추천 반려 - id={}", id);
        return ResponseEntity.ok(ApiResponse.success(routeRecommendService.rejectRecommendation(id, request.reason())));
    }
}
