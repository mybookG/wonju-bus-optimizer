package com.wonju.bus.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "잘못된 입력값입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),

    // 인증
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),

    // 버스 노선
    BUS_ROUTE_NOT_FOUND(HttpStatus.NOT_FOUND, "버스 노선을 찾을 수 없습니다."),
    BUS_STOP_NOT_FOUND(HttpStatus.NOT_FOUND, "버스 정류장을 찾을 수 없습니다."),

    // 수요 분석
    DEMAND_ANALYSIS_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "수요 분석에 실패했습니다."),
    BLIND_SPOT_NOT_FOUND(HttpStatus.NOT_FOUND, "사각지대 정보를 찾을 수 없습니다."),

    // 노선 추천
    ROUTE_RECOMMEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "노선 추천 생성에 실패했습니다."),
    RECOMMENDATION_NOT_FOUND(HttpStatus.NOT_FOUND, "노선 추천 정보를 찾을 수 없습니다."),

    // 시민 제보
    COMPLAINT_NOT_FOUND(HttpStatus.NOT_FOUND, "제보를 찾을 수 없습니다."),
    COMPLAINT_RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "오늘 제보 한도(3회)를 초과했습니다."),

    // SMS 인증
    SMS_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SMS 발송에 실패했습니다."),
    SMS_CODE_NOT_FOUND(HttpStatus.BAD_REQUEST, "인증 코드가 존재하지 않거나 만료됐습니다."),
    SMS_CODE_INVALID(HttpStatus.BAD_REQUEST, "인증 코드가 일치하지 않습니다."),

    // AI 분석
    AI_ANALYSIS_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AI 분석에 실패했습니다."),

    // 외부 API
    PUBLIC_API_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "공공 API가 응답하지 않습니다."),
    COORD_TRANSFORM_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "좌표 변환에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
