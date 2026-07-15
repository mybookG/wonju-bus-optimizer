package com.wonju.bus.domain.route;

import com.wonju.bus.domain.common.BaseEntity;
import com.wonju.bus.domain.demand.BlindSpot;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "route_recommendations",
        indexes = @Index(name = "idx_rec_blind_spot_status", columnList = "blind_spot_id, status"))
@Getter
@NoArgsConstructor
@SQLRestriction("deleted_at IS NULL")
public class RouteRecommendation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blind_spot_id", nullable = false)
    private BlindSpot blindSpot;

    @Column(nullable = false)
    private String areaCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecommendType recommendType;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String routePath;

    @Column
    private Double priorityScore;

    @Column
    private Long estimatedBeneficiaries;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecommendationStatus status;

    @Column(columnDefinition = "TEXT")
    private String rejectReason;

    @Builder
    public RouteRecommendation(BlindSpot blindSpot, String areaCode, RecommendType recommendType,
                                String description, String routePath,
                                Double priorityScore, Long estimatedBeneficiaries) {
        this.blindSpot = blindSpot;
        this.areaCode = areaCode;
        this.recommendType = recommendType;
        this.description = description;
        this.routePath = routePath;
        this.priorityScore = priorityScore;
        this.estimatedBeneficiaries = estimatedBeneficiaries;
        this.status = RecommendationStatus.PENDING;
    }

    public void approve() {
        this.status = RecommendationStatus.APPROVED;
    }

    public void reject(String reason) {
        this.status = RecommendationStatus.REJECTED;
        this.rejectReason = reason;
    }
}
