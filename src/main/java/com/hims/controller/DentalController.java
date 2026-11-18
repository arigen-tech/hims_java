package com.hims.controller;


import com.hims.service.DentalService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "OPD Dental", description = "This controller is used for any OPD Dental Related task.")
@RequestMapping("/opd/dental")
@Slf4j
@RequiredArgsConstructor
public class DentalController {

    private final DentalService dentalService;

    @GetMapping("/previous-details/{patientId}")
    public ResponseEntity<?> getPreviousDentalDetailsForPatient(@PathVariable Long patientId,
                                                                @RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(dentalService.getPreviousDentalDetails(patientId,page,size));
    }

    @GetMapping("/procedure-history/{patientId}")
    public ResponseEntity<?> getDentalProcedureHistory(@PathVariable Long patientId,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(dentalService.getDentalProcedureHistory(patientId,page,size));
    }

    @GetMapping("/session-history/{procedureDtId}")
    public ResponseEntity<?> getProcedureSessionHistory(@PathVariable Long procedureDtId) {
        return ResponseEntity.ok(dentalService.getProcedureSessionHistory(procedureDtId));
    }
}
