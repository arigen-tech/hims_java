package com.hims.controller;

import com.hims.response.ApiResponse;
import com.hims.response.DoctorRosterDTO;
import com.hims.response.ModalityDetailsByDepartmentResponse;
import com.hims.service.GeneralService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@Tag(name = "GeneralController")
@RequestMapping("/general")
@Slf4j
public class GeneralController {
    @Autowired
    private GeneralService generalService;

    @GetMapping("/getModalityDetailsByDepartment")
    public ResponseEntity<ApiResponse<List<ModalityDetailsByDepartmentResponse>>> getModalityDetailsByDepartment(@RequestParam String code) {
        ApiResponse<List<ModalityDetailsByDepartmentResponse>> response =generalService.getModalityDetailsByDepartment(code);

        return ResponseEntity.ok(response);
    }




}
