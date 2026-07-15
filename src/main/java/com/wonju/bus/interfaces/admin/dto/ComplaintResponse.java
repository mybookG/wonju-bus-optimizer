package com.wonju.bus.interfaces.admin.dto;

import com.wonju.bus.domain.complaint.Complaint;

import java.time.LocalDateTime;

public record ComplaintResponse(
        Long id,
        String maskedPhone,
        String content,
        Double latitude,
        Double longitude,
        String category,
        Integer severityScore,
        String status,
        LocalDateTime createdAt
) {
    public static ComplaintResponse from(Complaint complaint) {
        return new ComplaintResponse(
                complaint.getId(),
                complaint.getMaskedPhone(),
                complaint.getContent(),
                complaint.getLatitude(),
                complaint.getLongitude(),
                complaint.getCategory() != null ? complaint.getCategory().name() : null,
                complaint.getSeverityScore(),
                complaint.getStatus().name(),
                complaint.getCreatedAt()
        );
    }
}
