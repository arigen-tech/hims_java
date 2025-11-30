package com.hims.controller;

import com.hims.entity.Visit;
import com.hims.response.ApiResponse;
import com.hims.response.MasItemClassResponse;
import com.hims.response.VisitResponse;
import com.hims.service.ClinicalHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class clinicalHistoryController {

    @Autowired
    private ClinicalHistoryService clinicalHistoryService;

    @GetMapping("/getAllVisitByPatient/{patientId}")
    public ApiResponse<List<VisitResponse>> getAllVisitByPatient(@PathVariable int patientId) {
        return clinicalHistoryService.getPreviousVisits(patientId);
    }
}
