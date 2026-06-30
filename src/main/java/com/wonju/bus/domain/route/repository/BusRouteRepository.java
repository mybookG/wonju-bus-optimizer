package com.wonju.bus.domain.route.repository;

import com.wonju.bus.domain.route.BusRoute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BusRouteRepository extends JpaRepository<BusRoute, Long> {

    Optional<BusRoute> findByRouteId(String routeId);

    List<BusRoute> findByAreaCode(String areaCode);

    boolean existsByRouteId(String routeId);
}
