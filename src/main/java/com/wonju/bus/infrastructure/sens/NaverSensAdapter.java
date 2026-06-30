package com.wonju.bus.infrastructure.sens;

import com.wonju.bus.application.port.SmsPort;
import com.wonju.bus.common.BusServiceException;
import com.wonju.bus.common.ErrorCode;
import com.wonju.bus.common.HttpClientSupport;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class NaverSensAdapter extends HttpClientSupport implements SmsPort {

    private static final String BASE_URL = "https://sens.apigw.ntruss.com";

    private final SmsProperties properties;
    private WebClient webClient;

    @PostConstruct
    public void init() {
        this.webClient = buildWebClient(BASE_URL);
    }

    @Override
    public void sendVerificationCode(String phoneNumber, String code) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String url = "/sms/v2/services/%s/messages".formatted(properties.getServiceId());
        String signature = makeSignature(timestamp, url);

        Map<String, Object> body = Map.of(
                "type", "SMS",
                "contentType", "COMM",
                "countryCode", "82",
                "from", properties.getSender(),
                "content", "[원주시 버스] 인증번호: " + code,
                "messages", List.of(Map.of("to", phoneNumber))
        );

        try {
            webClient.post()
                    .uri(url)
                    .header("x-ncp-apigw-timestamp", timestamp)
                    .header("x-ncp-iam-access-key", properties.getAccessKey())
                    .header("x-ncp-apigw-signature-v2", signature)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            log.info("[NaverSensAdapter] SMS 발송 완료 - to={}****", phoneNumber.substring(0, phoneNumber.length() - 4));
        } catch (Exception e) {
            log.error("[NaverSensAdapter] SMS 발송 실패", e);
            throw new BusServiceException(ErrorCode.SMS_SEND_FAILED, e);
        }
    }

    private String makeSignature(String timestamp, String url) {
        try {
            String message = "POST %s\n%s\n%s".formatted(url, timestamp, properties.getAccessKey());
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(properties.getSecretKey().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getEncoder().encodeToString(mac.doFinal(message.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new BusServiceException(ErrorCode.SMS_SEND_FAILED, e);
        }
    }
}
