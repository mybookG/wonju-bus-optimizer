package com.wonju.bus.interfaces.citizen.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SmsVerifyRequest(
        @NotBlank
        @Pattern(regexp = "^01[0-9]{8,9}$", message = "올바른 휴대폰 번호를 입력하세요.")
        String phoneNumber,

        @NotBlank
        @Size(min = 6, max = 6, message = "인증 코드는 6자리입니다.")
        String code
) {}
