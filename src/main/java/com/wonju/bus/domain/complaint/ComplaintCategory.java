package com.wonju.bus.domain.complaint;

public enum ComplaintCategory {
    INSUFFICIENT_FREQUENCY, // 배차 간격 부족
    ROUTE_MISSING,          // 노선 미운행 지역
    STOP_MISSING,           // 정류장 없음
    INCONVENIENT_TRANSFER,  // 환승 불편
    SAFETY_ISSUE,           // 안전 문제
    OTHER                   // 기타
}
