package com.hims.controller;

import com.hims.service.VisitService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Patient visit", description = "This controller is used for patient visit related task.")
@RequestMapping("/visit")
@Slf4j
@RequiredArgsConstructor
public class VisitController {

    private final VisitService visitService;

    @PutMapping("/status/{visitId}")
    public ResponseEntity<?> changeVisitStatus(
            @PathVariable Long visitId,
            @RequestParam(required = false,defaultValue = "n") String visitStatus
    ) {

        return ResponseEntity.ok(
                visitService.changeVisitStatus(visitId, visitStatus));
    }

    @GetMapping("/queue/{hospitalId}/{doctorId}/{departmentId}")
    public ResponseEntity<?> getPatientVisitQueue(
            @PathVariable Long hospitalId,
            @PathVariable Long doctorId,
            @PathVariable Long departmentId,
            @RequestParam(required = false,defaultValue = "0") int page,
            @RequestParam(required = false,defaultValue = "5") int size
    ) {

        return ResponseEntity.ok(
                visitService.getPatientVisitQueue(hospitalId,doctorId,departmentId,page,size));
    }

    @GetMapping("/{visitId}/past-history-and-notes")
    public ResponseEntity<?> getPreviousVisitPastHistoryAndClinicalNotes(
            @PathVariable Long visitId
    ) {

        return ResponseEntity.ok(
                visitService.getPreviousVisitPastHistoryAndClinicalNotes(visitId));
    }
}
