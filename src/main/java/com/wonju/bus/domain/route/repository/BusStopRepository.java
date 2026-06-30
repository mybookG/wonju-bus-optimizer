package com.wonju.bus.domain.route.repository;

import com.wonju.bus.domain.route.BusStop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BusStopRepository extends JpaRepository<BusStop, Long> {

    Optional<BusStop> findByStopId(String stopId);

    boolean existsByStopId(String stopId);

    /**
     * 반경 내 정류장 조회 (PostGIS ST_DWithin, WGS84 기준)
     */
    @Query(value = """
            SELECT * FROM bus_stops
            WHERE deleted_at IS NULL
              AND ST_DWithin(
                ST_SetSRID(ST_MakePoint(longitude, latitude), 4326)::geography,
                ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography,
                :radiusMeters
              )
            """, nativeQuery = true)
    List<BusStop> findWithinRadius(@Param("lat") double lat,
                                   @Param("lon") double lon,
                                   @Param("radiusMeters") double radiusMeters);
}
