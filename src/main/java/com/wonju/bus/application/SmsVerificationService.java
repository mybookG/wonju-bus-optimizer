package com.wonju.bus.application;

import com.wonju.bus.application.port.SmsPort;
import com.wonju.bus.common.BusServiceException;
import com.wonju.bus.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsVerificationService {

    private static final String KEY_PREFIX = "sms:verify:";
    private static final Duration TTL = Duration.ofMinutes(3);
    private static final int CODE_LENGTH = 6;

    private final SmsPort smsPort;
    private final StringRedisTemplate redisTemplate;

    public void sendCode(String phoneNumber) {
        String code = generateCode();
        String maskedPhone = maskPhone(phoneNumber);
        redisTemplate.opsForValue().set(KEY_PREFIX + phoneNumber, code, TTL);

        smsPort.sendVerificationCode(phoneNumber, code);
        log.info("[SmsVerificationService] 인증코드 발송 - phone={}", maskedPhone);
    }

    public void verifyCode(String phoneNumber, String inputCode) {
        String key = KEY_PREFIX + phoneNumber;
        String savedCode = redisTemplate.opsForValue().get(key);

        if (savedCode == null) {
            throw new BusServiceException(ErrorCode.SMS_CODE_NOT_FOUND);
        }
        if (!savedCode.equals(inputCode)) {
            throw new BusServiceException(ErrorCode.SMS_CODE_INVALID);
        }

        redisTemplate.delete(key);
        log.info("[SmsVerificationService] 인증 성공 - phone={}", maskPhone(phoneNumber));
    }

    private String generateCode() {
        return String.format("%06d", new SecureRandom().nextInt(1_000_000));
    }

    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) return "****";
        return phone.substring(0, phone.length() - 4) + "****";
    }
}
