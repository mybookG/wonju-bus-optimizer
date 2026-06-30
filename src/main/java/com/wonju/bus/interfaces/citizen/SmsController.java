package com.wonju.bus.interfaces.citizen;

import com.wonju.bus.application.SmsVerificationService;
import com.wonju.bus.common.ApiResponse;
import com.wonju.bus.interfaces.citizen.dto.SmsSendRequest;
import com.wonju.bus.interfaces.citizen.dto.SmsVerifyRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/sms")
@RequiredArgsConstructor
public class SmsController {

    private final SmsVerificationService smsVerificationService;

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<Void>> sendCode(@Valid @RequestBody SmsSendRequest request) {
        log.info("[SmsController] 인증코드 요청 - phone={}****",
                request.phoneNumber().substring(0, request.phoneNumber().length() - 4));
        smsVerificationService.sendCode(request.phoneNumber());
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Void>> verifyCode(@Valid @RequestBody SmsVerifyRequest request) {
        smsVerificationService.verifyCode(request.phoneNumber(), request.code());
        return ResponseEntity.ok(ApiResponse.success());
    }
}
