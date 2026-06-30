package com.wonju.bus.domain.complaint;

import com.wonju.bus.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "complaints",
        indexes = @Index(name = "idx_complaint_phone_date", columnList = "masked_phone, created_at"))
@Getter
@NoArgsConstructor
@SQLRestriction("deleted_at IS NULL")
public class Complaint extends BaseEntity {

    @Column(nullable = false)
    private String maskedPhone;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Enumerated(EnumType.STRING)
    @Column
    private ComplaintCategory category;

    @Column
    private Integer severityScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ComplaintStatus status;

    @Column(columnDefinition = "TEXT")
    private String geminiClassification;

    @Builder
    public Complaint(String maskedPhone, String content, Double latitude, Double longitude) {
        this.maskedPhone = maskedPhone;
        this.content = content;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = ComplaintStatus.RECEIVED;
    }

    public void classify(ComplaintCategory category, Integer severityScore, String geminiClassification) {
        this.category = category;
        this.severityScore = severityScore;
        this.geminiClassification = geminiClassification;
    }

    public void updateStatus(ComplaintStatus status) {
        this.status = status;
    }
}
