package com.wonju.bus.application.port;

public interface SmsPort {

    /**
     * 인증 코드를 포함한 SMS를 전송.
     */
    void sendVerificationCode(String phoneNumber, String code);
}
