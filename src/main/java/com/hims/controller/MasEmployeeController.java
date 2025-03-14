package com.hims.controller;

import com.hims.entity.MasEmployee;
import com.hims.request.MasEmployeeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasEmployeeDTO;
import com.hims.service.MasEmployeeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "EmployeeRegistrationController", description = "Controller for Employee Registration Page")
@RequestMapping("/employees")
@RequiredArgsConstructor
public class MasEmployeeController {
    private static final Logger logger = LoggerFactory.getLogger(MasEmployeeController.class);

    private final MasEmployeeService masEmployeeService;

    @PostMapping(value = "/employee", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<MasEmployee>> createEmployee(@ModelAttribute MasEmployeeRequest masEmployeeRequest) {
        logger.info("Received request to create a new Employee: {}", masEmployeeRequest);
        return ResponseEntity.ok(masEmployeeService.createEmployee(masEmployeeRequest));
    }
    @GetMapping("/employee")
    public ResponseEntity<ApiResponse<List<MasEmployeeDTO>>> getAllHotels() {
        logger.info("Received request to fetch all Employees.");
        return ResponseEntity.ok(masEmployeeService.getAllEmployees());
    }
    @GetMapping("/employee/{id}")
    public ResponseEntity<ApiResponse<MasEmployee>> getEmployeeById(@PathVariable Long id) {
        logger.info("Received request to fetch Employee with ID: {}", id);
        return ResponseEntity.ok(masEmployeeService.getEmployeeById(id));
    }
    @PutMapping(value = "/employee/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<MasEmployee>> updateEmployee(@PathVariable Long id, @ModelAttribute MasEmployeeRequest masEmployeeRequest) {
        logger.info("Received request to update Employee with ID: {}", id);
        return ResponseEntity.ok(masEmployeeService.updateEmployee(id, masEmployeeRequest));
    }
    @PutMapping("/employee/status/{id}")
    public ResponseEntity<ApiResponse<MasEmployee>> updateEmplpoyeeStatus(@PathVariable Long id, @RequestParam String status) {
        logger.info("Received request to update Employee status  with ID: {}", id);
        return ResponseEntity.ok(masEmployeeService.updateEmployeeStatus(id,status));
    }

}