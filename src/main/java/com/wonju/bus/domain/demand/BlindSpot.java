package com.wonju.bus.domain.demand;

import com.wonju.bus.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;

@Entity
@Table(name = "blind_spots")
@Getter
@NoArgsConstructor
@SQLRestriction("deleted_at IS NULL")
public class BlindSpot extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String blindSpotId;

    @Column(nullable = false)
    private String areaCode;

    @Column(nullable = false)
    private String areaName;

    @Column(nullable = false)
    private Integer demandScore;

    @Column(nullable = false)
    private Double supplyIndex;

    @Column(nullable = false)
    private Double centerLatitude;

    @Column(nullable = false)
    private Double centerLongitude;

    @Column(nullable = false)
    private LocalDate detectedDate;

    @Column(nullable = false)
    private Boolean resolved = false;

    @Builder
    public BlindSpot(String blindSpotId, String areaCode, String areaName,
                     Integer demandScore, Double supplyIndex,
                     Double centerLatitude, Double centerLongitude, LocalDate detectedDate) {
        this.blindSpotId = blindSpotId;
        this.areaCode = areaCode;
        this.areaName = areaName;
        this.demandScore = demandScore;
        this.supplyIndex = supplyIndex;
        this.centerLatitude = centerLatitude;
        this.centerLongitude = centerLongitude;
        this.detectedDate = detectedDate;
        this.resolved = false;
    }

    public void markResolved() {
        this.resolved = true;
    }
}
