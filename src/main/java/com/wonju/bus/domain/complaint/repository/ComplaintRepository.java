package com.wonju.bus.domain.complaint.repository;

import com.wonju.bus.domain.complaint.Complaint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    Page<Complaint> findAll(Pageable pageable);

    /**
     * 하루 동안 동일 번호 제보 횟수 (Rate Limiting)
     */
    @Query("""
            SELECT COUNT(c) FROM Complaint c
            WHERE c.maskedPhone = :maskedPhone
              AND c.createdAt >= :from
            """)
    long countByMaskedPhoneAndCreatedAtAfter(@Param("maskedPhone") String maskedPhone,
                                              @Param("from") LocalDateTime from);

    /**
     * 반경 200m 내 제보 집계 (사각지대 환류 트리거용)
     */
    @Query(value = """
            SELECT COUNT(*) FROM complaints
            WHERE deleted_at IS NULL
              AND ST_DWithin(
                ST_SetSRID(ST_MakePoint(longitude, latitude), 4326)::geography,
                ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography,
                200
              )
              AND created_at >= :since
            """, nativeQuery = true)
    long countNearbyComplaintsSince(@Param("lat") double lat,
                                    @Param("lon") double lon,
                                    @Param("since") LocalDateTime since);

    List<Complaint> findTop10ByOrderByCreatedAtDesc();
}
