package com.wonju.bus.infrastructure.sens;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "sens")
public class SmsProperties {
    private String accessKey;
    private String secretKey;
    private String serviceId;
    private String sender;
}
