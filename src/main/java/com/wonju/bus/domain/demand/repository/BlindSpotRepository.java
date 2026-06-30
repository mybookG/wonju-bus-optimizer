package com.wonju.bus.domain.demand.repository;

import com.wonju.bus.domain.demand.BlindSpot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BlindSpotRepository extends JpaRepository<BlindSpot, Long> {

    Optional<BlindSpot> findByBlindSpotId(String blindSpotId);

    List<BlindSpot> findByResolvedFalseOrderByDemandScoreDesc();

    List<BlindSpot> findByAreaCode(String areaCode);

    /**
     * 반경 200m 내 사각지대 집계 (시민 제보 환류용)
     */
    @Query(value = """
            SELECT * FROM blind_spots
            WHERE deleted_at IS NULL AND resolved = false
              AND ST_DWithin(
                ST_SetSRID(ST_MakePoint(center_longitude, center_latitude), 4326)::geography,
                ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography,
                :radiusMeters
              )
            """, nativeQuery = true)
    List<BlindSpot> findUnresolvedWithinRadius(@Param("lat") double lat,
                                               @Param("lon") double lon,
                                               @Param("radiusMeters") double radiusMeters);
}
