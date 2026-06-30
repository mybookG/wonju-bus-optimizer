package com.wonju.bus.infrastructure.publicdata;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "public-api")
public class PublicApiProperties {

    private ApiConfig tago = new ApiConfig();
    private ApiConfig kosis = new ApiConfig();

    @Getter
    @Setter
    public static class ApiConfig {
        private String key;
        private String baseUrl;
    }
}
