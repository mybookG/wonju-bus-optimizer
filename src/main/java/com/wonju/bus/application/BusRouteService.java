package com.wonju.bus.application;

import com.wonju.bus.common.BusServiceException;
import com.wonju.bus.common.ErrorCode;
import com.wonju.bus.domain.route.BusRoute;
import com.wonju.bus.domain.route.repository.BusRouteRepository;
import com.wonju.bus.domain.route.repository.BusStopRepository;
import com.wonju.bus.interfaces.admin.dto.BusRouteResponse;
import com.wonju.bus.interfaces.admin.dto.BusStopResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusRouteService {

    private final BusRouteRepository busRouteRepository;
    private final BusStopRepository busStopRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "cache:tago", key = "#routeId")
    public BusRouteResponse getRoute(String routeId) {
        log.info("[BusRouteService] 노선 조회 - routeId={}", routeId);
        return busRouteRepository.findByRouteId(routeId)
                .map(BusRouteResponse::from)
                .orElseThrow(() -> new BusServiceException(ErrorCode.BUS_ROUTE_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<BusRouteResponse> getRoutesByArea(String areaCode) {
        log.info("[BusRouteService] 지역 노선 목록 조회 - areaCode={}", areaCode);
        return busRouteRepository.findByAreaCode(areaCode).stream()
                .map(BusRouteResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BusStopResponse> getStopsNearby(double lat, double lon, double radiusMeters) {
        log.info("[BusRouteService] 반경 내 정류장 조회 - lat={}, lon={}, radius={}m", lat, lon, radiusMeters);
        return busStopRepository.findWithinRadius(lat, lon, radiusMeters).stream()
                .map(BusStopResponse::from)
                .toList();
    }

    @Transactional
    public BusRoute saveOrUpdateRoute(BusRoute route) {
        return busRouteRepository.findByRouteId(route.getRouteId())
                .map(existing -> {
                    existing.updateInterval(route.getIntervalMinutes());
                    log.info("[BusRouteService] 노선 갱신 - routeId={}", route.getRouteId());
                    return existing;
                })
                .orElseGet(() -> {
                    BusRoute saved = busRouteRepository.save(route);
                    log.info("[BusRouteService] 노선 저장 - routeId={}", saved.getRouteId());
                    return saved;
                });
    }
}
