package com.wonju.bus.domain.route;

import com.wonju.bus.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bus_stops")
@Getter
@NoArgsConstructor
@SQLRestriction("deleted_at IS NULL")
public class BusStop extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String stopId;

    @Column(nullable = false)
    private String stopName;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column
    private String areaCode;

    @OneToMany(mappedBy = "busStop", fetch = FetchType.LAZY)
    private List<RouteStop> routeStops = new ArrayList<>();

    @Builder
    public BusStop(String stopId, String stopName, Double latitude, Double longitude, String areaCode) {
        this.stopId = stopId;
        this.stopName = stopName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.areaCode = areaCode;
    }
}
