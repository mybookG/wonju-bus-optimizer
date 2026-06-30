package com.wonju.bus.domain.route;

import com.wonju.bus.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "route_recommendations")
@Getter
@NoArgsConstructor
@SQLRestriction("deleted_at IS NULL")
public class RouteRecommendation extends BaseEntity {

    @Column(nullable = false)
    private String areaCode;

    @Column(nullable = false)
    private String blindSpotId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecommendType recommendType;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String routePath;

    @Column
    private Integer priorityScore;

    @Column
    private Long estimatedBeneficiaries;

    @Column
    private String status;

    @Builder
    public RouteRecommendation(String areaCode, String blindSpotId, RecommendType recommendType,
                                String description, String routePath,
                                Integer priorityScore, Long estimatedBeneficiaries) {
        this.areaCode = areaCode;
        this.blindSpotId = blindSpotId;
        this.recommendType = recommendType;
        this.description = description;
        this.routePath = routePath;
        this.priorityScore = priorityScore;
        this.estimatedBeneficiaries = estimatedBeneficiaries;
        this.status = "PENDING";
    }

    public void approve() {
        this.status = "APPROVED";
    }

    public enum RecommendType {
        NEW_ROUTE, EXTEND_ROUTE, INCREASE_FREQUENCY
    }
}
