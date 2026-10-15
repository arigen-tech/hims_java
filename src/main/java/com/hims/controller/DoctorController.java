package com.hims.controller;

import com.hims.service.DoctorService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Doctor", description = "This controller is used for any Doctor Related task.")
@RequestMapping("/doctor")
@Slf4j
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping("/summary")
    public ResponseEntity<?> getVisitSummary(
            @RequestParam(required = false) Long hospitalId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) Long departmentId
          ) {

        return ResponseEntity.ok(
                doctorService.getTodayVisitSummary(hospitalId, doctorId, departmentId));
    }

}
