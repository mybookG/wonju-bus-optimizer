package com.wonju.bus.domain.complaint;

public enum ComplaintStatus {
    RECEIVED,   // 접수
    CLASSIFIED, // AI 분류 완료
    PROCESSING, // 처리 중
    RESOLVED    // 해결됨
}
