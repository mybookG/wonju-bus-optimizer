package com.wonju.bus.interfaces.citizen.dto;

import jakarta.validation.constraints.*;

public record ComplaintCreateRequest(
        @NotBlank
        @Pattern(regexp = "^01[0-9]{8,9}$", message = "올바른 휴대폰 번호를 입력하세요.")
        String phoneNumber,

        @NotBlank
        @Size(min = 10, max = 1000, message = "제보 내용은 10자 이상 1000자 이하로 입력하세요.")
        String content,

        @NotNull
        @DecimalMin(value = "35.0") @DecimalMax(value = "39.0")
        Double latitude,

        @NotNull
        @DecimalMin(value = "127.0") @DecimalMax(value = "131.0")
        Double longitude
) {}
