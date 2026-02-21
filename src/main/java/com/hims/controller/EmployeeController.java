package com.hims.controller;

import com.hims.entity.MasEmployee;
import com.hims.entity.repository.MasEmployeeRepository;
import com.hims.request.MasEmployeeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasEmployeeDTO;
import com.hims.service.EmployeeService;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Controller for employee management operations
 */
@RestController
@RequestMapping("/employee")
@RequiredArgsConstructor
@Slf4j
public class EmployeeController {

    private final EmployeeService employeeService;

    private final MasEmployeeRepository masEmployeeRepository;

    /**
     * Get all employees
     */
    @GetMapping("/getAllEmployees")
    public ResponseEntity<ApiResponse<List<MasEmployeeDTO>>> getAllEmployees() {
        log.info("GET /employee/getAllEmployees called");
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    /**
     * Get employees by status
     */
    @GetMapping("/getEmployeesByStatus/{status}")
    public ResponseEntity<ApiResponse<List<MasEmployeeDTO>>> getEmployeesByStatus(
            @PathVariable @Parameter(description = "Employee status") String status) {
        log.info("GET /employee/getEmployeesByStatus/{} called", status);
        return ResponseEntity.ok(employeeService.getEmployeesByStatus(status));
    }

    /**
     * Get employee by ID
     */
    @GetMapping("/getEmployeeById/{id}")
    public ResponseEntity<ApiResponse<MasEmployeeDTO>> getEmployeeById(
            @PathVariable @Parameter(description = "Employee ID") Long id) {
        log.info("GET /employee/getEmployeeById/{} called", id);
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    /**
     * Create new employee
     */
    @PostMapping(value = "/createEmployee", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createEmployee(@ModelAttribute MasEmployeeRequest request) {
        log.info("POST /employee/createEmployee called");
        return ResponseEntity.ok(employeeService.createEmployee(request));
    }

    /**
     * Update existing employee
     */
    @PutMapping(value = "/updateEmployee/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<MasEmployee>> updateEmployee(
            @PathVariable @Parameter(description = "Employee ID") Long id,
            @ModelAttribute MasEmployeeRequest request) {
        log.info("PUT /employee/updateEmployee/{} called", id);
        return ResponseEntity.ok(employeeService.updateEmployee(id, request));
    }

    /**
     * Approve employee and create user
     */
    @PutMapping("/approveEmployee/{id}/{deptId}")
    public ResponseEntity<ApiResponse<MasEmployee>> approveEmployee(
            @PathVariable @Parameter(description = "Employee ID") Long id,
            @PathVariable @Parameter(description = "Department ID") Long deptId) {
        log.info("PUT /employee/approveEmployee/{}/{} called", id, deptId);
        return ResponseEntity.ok(employeeService.updateEmployeeApprovalStatus(id, deptId));
    }

    /**
     * Create employee and approve immediately
     */
    @PostMapping(value = "/createAndApproveEmployee", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<MasEmployee>> createAndApproveEmployee(
            @RequestBody MasEmployeeRequest request) {
        log.info("POST /employee/createAndApproveEmployee called");
        return ResponseEntity.ok(employeeService.createAndApproveEmployee(request));
    }

    /**
     * Get employee profile image
     */
    @GetMapping("/getEmployeeProfileImage/{empId}")
    public ResponseEntity<?> getEmployeeProfileImage(
            @PathVariable @Parameter(description = "Employee ID") Long empId) {
        log.info("GET /employee/getEmployeeProfileImage/{} called", empId);
        Optional<MasEmployee> optionalEmployee = masEmployeeRepository.findById(empId);

        if (optionalEmployee.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Employee not found");
        }

        String imagePath = optionalEmployee.get().getProfilePicName();
        if (imagePath == null || imagePath.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Profile image path not set");
        }

        File imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Image not found");
        }

        try {
            Path path = imageFile.toPath();
            byte[] imageBytes = Files.readAllBytes(path);
            String contentType = Files.probeContentType(path);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(imageBytes);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error reading image: " + e.getMessage());
        }
    }

    /**
     * View employee document
     */
    @GetMapping("/viewEmployeeDocument")
    public ResponseEntity<?> viewEmployeeDocument(
            @RequestParam @Parameter(description = "Document file path") String filePath) {
        log.info("GET /employee/viewEmployeeDocument called");
        File file = new File(filePath);
        if (!file.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found");
        }
        try {
            Path path = file.toPath();
            byte[] data = Files.readAllBytes(path);
            String contentType = Files.probeContentType(path);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(data);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to read file: " + e.getMessage());
        }
    }
}



