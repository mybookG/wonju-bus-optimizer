package com.wonju.bus.domain.demand;

import com.wonju.bus.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "demand_scores",
        indexes = @Index(name = "idx_demand_area_date", columnList = "area_code, analyzed_date"))
@Getter
@NoArgsConstructor
public class DemandScore extends BaseEntity {

    @Column(nullable = false)
    private String areaCode;

    @Column(nullable = false)
    private String areaName;

    @Column(nullable = false)
    private Integer demandScore;

    @Column(nullable = false)
    private Double supplyIndex;

    @Column(nullable = false)
    private Long population;

    @Column
    private Long buildingCount;

    @Column
    private Long welfareFacilityCount;

    @Column(nullable = false)
    private LocalDate analyzedDate;

    @Column(columnDefinition = "TEXT")
    private String geminiRawResponse;

    @Builder
    public DemandScore(String areaCode, String areaName, Integer demandScore, Double supplyIndex,
                       Long population, Long buildingCount, Long welfareFacilityCount,
                       LocalDate analyzedDate, String geminiRawResponse) {
        this.areaCode = areaCode;
        this.areaName = areaName;
        this.demandScore = demandScore;
        this.supplyIndex = supplyIndex;
        this.population = population;
        this.buildingCount = buildingCount;
        this.welfareFacilityCount = welfareFacilityCount;
        this.analyzedDate = analyzedDate;
        this.geminiRawResponse = geminiRawResponse;
    }
}
