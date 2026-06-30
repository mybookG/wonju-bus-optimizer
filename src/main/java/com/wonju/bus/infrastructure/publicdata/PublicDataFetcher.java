package com.wonju.bus.infrastructure.publicdata;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public interface PublicDataFetcher {

    /**
     * 지역 코드 기준으로 공공 데이터를 가져온다.
     */
    List<JsonNode> fetch(String areaCode);

    /**
     * 이 fetcher가 담당하는 데이터 소스 이름 (로깅·모니터링용)
     */
    String sourceName();
}
