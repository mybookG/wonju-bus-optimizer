package com.wonju.bus.domain.route;

public enum RecommendType {
    NEW_ROUTE,           // 신규 노선 신설
    ROUTE_EXTENSION,     // 기존 노선 연장
    FREQUENCY_INCREASE,  // 배차 간격 단축
    DEMAND_RESPONSIVE    // 수요응답형 버스
}
