package com.wonju.bus.application;

import com.wonju.bus.common.BusServiceException;
import com.wonju.bus.common.ErrorCode;
import com.wonju.bus.domain.route.BusRoute;
import com.wonju.bus.domain.route.BusStop;
import com.wonju.bus.domain.route.repository.BusRouteRepository;
import com.wonju.bus.domain.route.repository.BusStopRepository;
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
    public BusRoute getRoute(String routeId) {
        log.info("[BusRouteService] 노선 조회 - routeId={}", routeId);
        return busRouteRepository.findByRouteId(routeId)
                .orElseThrow(() -> new BusServiceException(ErrorCode.BUS_ROUTE_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<BusRoute> getRoutesByArea(String areaCode) {
        log.info("[BusRouteService] 지역 노선 목록 조회 - areaCode={}", areaCode);
        return busRouteRepository.findByAreaCode(areaCode);
    }

    @Transactional(readOnly = true)
    public List<BusStop> getStopsNearby(double lat, double lon, double radiusMeters) {
        log.info("[BusRouteService] 반경 내 정류장 조회 - lat={}, lon={}, radius={}m", lat, lon, radiusMeters);
        return busStopRepository.findWithinRadius(lat, lon, radiusMeters);
    }

    @Transactional
    public BusRoute saveOrUpdateRoute(BusRoute route) {
        if (busRouteRepository.existsByRouteId(route.getRouteId())) {
            BusRoute existing = busRouteRepository.findByRouteId(route.getRouteId()).get();
            existing.updateInterval(route.getIntervalMinutes());
            log.info("[BusRouteService] 노선 갱신 - routeId={}", route.getRouteId());
            return existing;
        }
        BusRoute saved = busRouteRepository.save(route);
        log.info("[BusRouteService] 노선 저장 - routeId={}", saved.getRouteId());
        return saved;
    }
}
