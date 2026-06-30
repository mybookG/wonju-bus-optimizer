package com.wonju.bus.domain.demand.repository;

import com.wonju.bus.domain.demand.DemandScore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DemandScoreRepository extends JpaRepository<DemandScore, Long> {

    Optional<DemandScore> findByAreaCodeAndAnalyzedDate(String areaCode, LocalDate analyzedDate);

    List<DemandScore> findByAnalyzedDateOrderByDemandScoreDesc(LocalDate analyzedDate);

    List<DemandScore> findByAreaCodeOrderByAnalyzedDateDesc(String areaCode);
}
