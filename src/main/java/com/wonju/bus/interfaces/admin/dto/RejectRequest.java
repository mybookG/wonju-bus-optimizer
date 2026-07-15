package com.wonju.bus.interfaces.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record RejectRequest(
        @NotBlank String reason
) {}
