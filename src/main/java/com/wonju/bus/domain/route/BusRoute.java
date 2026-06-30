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
@Table(name = "bus_routes")
@Getter
@NoArgsConstructor
@SQLRestriction("deleted_at IS NULL")
public class BusRoute extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String routeId;

    @Column(nullable = false)
    private String routeName;

    @Column(nullable = false)
    private String routeType;

    @Column
    private String startStop;

    @Column
    private String endStop;

    @Column
    private Integer intervalMinutes;

    @Column(nullable = false)
    private String areaCode;

    @OneToMany(mappedBy = "busRoute", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<RouteStop> routeStops = new ArrayList<>();

    @Builder
    public BusRoute(String routeId, String routeName, String routeType,
                    String startStop, String endStop, Integer intervalMinutes, String areaCode) {
        this.routeId = routeId;
        this.routeName = routeName;
        this.routeType = routeType;
        this.startStop = startStop;
        this.endStop = endStop;
        this.intervalMinutes = intervalMinutes;
        this.areaCode = areaCode;
    }

    public void updateInterval(int intervalMinutes) {
        this.intervalMinutes = intervalMinutes;
    }
}
