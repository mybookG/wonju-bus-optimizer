package com.wonju.bus.interfaces.citizen;

import com.wonju.bus.application.ComplaintService;
import com.wonju.bus.common.ApiResponse;
import com.wonju.bus.domain.complaint.Complaint;
import com.wonju.bus.interfaces.citizen.dto.ComplaintCreateRequest;
import com.wonju.bus.interfaces.citizen.dto.ComplaintCreateResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/complaints")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService complaintService;

    @PostMapping
    public ResponseEntity<ApiResponse<ComplaintCreateResponse>> submit(
            @Valid @RequestBody ComplaintCreateRequest request) {
        log.info("[ComplaintController] 제보 접수 요청");
        Complaint complaint = complaintService.submitComplaint(
                request.phoneNumber(),
                request.content(),
                request.latitude(),
                request.longitude()
        );
        return ResponseEntity.ok(ApiResponse.success(ComplaintCreateResponse.from(complaint)));
    }
}
