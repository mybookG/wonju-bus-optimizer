package com.wonju.bus.application;

import com.wonju.bus.application.port.AiAnalysisPort;
import com.wonju.bus.common.BusServiceException;
import com.wonju.bus.common.ErrorCode;
import com.wonju.bus.domain.complaint.Complaint;
import com.wonju.bus.domain.complaint.ComplaintCategory;
import com.wonju.bus.domain.complaint.repository.ComplaintRepository;
import com.wonju.bus.domain.demand.repository.BlindSpotRepository;
import com.wonju.bus.interfaces.admin.dto.ComplaintResponse;
import com.wonju.bus.interfaces.citizen.dto.ComplaintCreateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComplaintService {

    private static final int DAILY_LIMIT = 3;
    private static final int NEARBY_COMPLAINT_THRESHOLD = 5;

    private final AiAnalysisPort aiAnalysisPort;
    private final ComplaintRepository complaintRepository;
    private final BlindSpotRepository blindSpotRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ComplaintCreateResponse submitComplaint(String phoneNumber, String content, double lat, double lon) {
        String maskedPhone = SmsVerificationService.maskPhone(phoneNumber);
        checkDailyLimit(maskedPhone);

        Complaint complaint = Complaint.builder()
                .maskedPhone(maskedPhone)
                .content(content)
                .latitude(lat)
                .longitude(lon)
                .build();
        complaintRepository.saveAndFlush(complaint);
        log.info("[ComplaintService] 제보 접수 - phone={}, lat={}, lon={}", maskedPhone, lat, lon);

        // 트랜잭션 커밋 후 AI 분류 실행 (트랜잭션 내 외부 API 호출 금지)
        eventPublisher.publishEvent(new ComplaintClassifyEvent(this, complaint.getId()));
        return ComplaintCreateResponse.from(complaint);
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    @TransactionalEventListener
    public void classifyComplaint(ComplaintClassifyEvent event) {
        Complaint complaint = complaintRepository.findById(event.complaintId())
                .orElseThrow(() -> new BusServiceException(ErrorCode.COMPLAINT_NOT_FOUND));

        try {
            AiAnalysisPort.ComplaintClassifyResult result = aiAnalysisPort.classifyComplaint(complaint.getContent());
            complaint.classify(
                    ComplaintCategory.valueOf(result.category()),
                    result.severityScore(),
                    result.rawResponse()
            );
            log.info("[ComplaintService] AI 분류 완료 - id={}, category={}", complaint.getId(), result.category());
        } catch (Exception e) {
            log.warn("[ComplaintService] AI 분류 실패 - id={}", complaint.getId(), e);
        }

        checkAndTriggerReanalysis(complaint.getLatitude(), complaint.getLongitude());
    }

    @Transactional(readOnly = true)
    public Page<ComplaintResponse> getComplaints(Pageable pageable) {
        return complaintRepository.findAll(pageable).map(ComplaintResponse::from);
    }

    private void checkDailyLimit(String maskedPhone) {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        long count = complaintRepository.countByMaskedPhoneAndCreatedAtAfter(maskedPhone, startOfDay);
        if (count >= DAILY_LIMIT) {
            throw new BusServiceException(ErrorCode.COMPLAINT_RATE_LIMIT_EXCEEDED);
        }
    }

    private void checkAndTriggerReanalysis(double lat, double lon) {
        long nearbyCount = complaintRepository.countNearbyComplaintsSince(lat, lon, LocalDateTime.now().minusDays(7));
        if (nearbyCount >= NEARBY_COMPLAINT_THRESHOLD) {
            blindSpotRepository.findUnresolvedWithinRadius(lat, lon, 200).stream()
                    .findFirst()
                    .ifPresent(spot -> {
                        log.info("[ComplaintService] 수요 재분석 트리거 - areaCode={}", spot.getAreaCode());
                        eventPublisher.publishEvent(new DemandAnalysisService.DemandReanalysisEvent(this, spot.getAreaCode()));
                    });
        }
    }

    public record ComplaintClassifyEvent(Object source, Long complaintId) {}
}
