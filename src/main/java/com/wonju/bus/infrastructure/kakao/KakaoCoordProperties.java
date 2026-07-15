package com.wonju.bus.infrastructure.kakao;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "kakao")
public class KakaoCoordProperties {
    private String restApiKey;
    private String baseUrl;
}
