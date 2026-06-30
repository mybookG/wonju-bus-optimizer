package com.wonju.bus.interfaces.citizen.dto;

import com.wonju.bus.domain.complaint.Complaint;

import java.time.LocalDateTime;

public record ComplaintCreateResponse(
        Long id,
        String status,
        LocalDateTime createdAt
) {
    public static ComplaintCreateResponse from(Complaint complaint) {
        return new ComplaintCreateResponse(
                complaint.getId(),
                complaint.getStatus().name(),
                complaint.getCreatedAt()
        );
    }
}
