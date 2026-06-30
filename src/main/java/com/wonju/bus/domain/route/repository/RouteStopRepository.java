package com.wonju.bus.domain.route.repository;

import com.wonju.bus.domain.route.RouteStop;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RouteStopRepository extends JpaRepository<RouteStop, Long> {

    @EntityGraph(attributePaths = {"busStop"})
    List<RouteStop> findByBusRouteIdOrderByStopSequenceAsc(Long busRouteId);
}
