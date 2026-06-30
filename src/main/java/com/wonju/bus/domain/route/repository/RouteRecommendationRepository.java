package com.wonju.bus.domain.route.repository;

import com.wonju.bus.domain.route.RouteRecommendation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RouteRecommendationRepository extends JpaRepository<RouteRecommendation, Long> {

    Page<RouteRecommendation> findByAreaCode(String areaCode, Pageable pageable);

    List<RouteRecommendation> findByBlindSpotId(String blindSpotId);

    List<RouteRecommendation> findByStatusOrderByPriorityScoreDesc(String status);
}
