package com.wonju.bus.domain.route;

import com.wonju.bus.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "route_stops",
        uniqueConstraints = @UniqueConstraint(columnNames = {"bus_route_id", "stop_sequence"}))
@Getter
@NoArgsConstructor
public class RouteStop extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bus_route_id", nullable = false)
    private BusRoute busRoute;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bus_stop_id", nullable = false)
    private BusStop busStop;

    @Column(nullable = false)
    private Integer stopSequence;

    @Column(nullable = false)
    private String direction;

    @Builder
    public RouteStop(BusRoute busRoute, BusStop busStop, Integer stopSequence, String direction) {
        this.busRoute = busRoute;
        this.busStop = busStop;
        this.stopSequence = stopSequence;
        this.direction = direction;
    }
}
