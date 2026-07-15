package com.wonju.bus.config;

import com.wonju.bus.infrastructure.gemini.GeminiProperties;
import com.wonju.bus.infrastructure.kakao.KakaoCoordProperties;
import com.wonju.bus.infrastructure.publicdata.PublicApiProperties;
import com.wonju.bus.infrastructure.sens.SmsProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        GeminiProperties.class,
        KakaoCoordProperties.class,
        PublicApiProperties.class,
        SmsProperties.class
})
public class AppConfig {
}
