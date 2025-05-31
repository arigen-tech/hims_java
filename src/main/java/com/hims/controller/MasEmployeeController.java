package com.hims.controller;

import com.hims.entity.MasEmployee;
import com.hims.entity.repository.MasEmployeeRepository;
import com.hims.request.EmployeeDocumentReq;
import com.hims.request.EmployeeQualificationReq;
import com.hims.request.MasEmployeeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasEmployeeDTO;
import com.hims.service.MasEmployeeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@RestController
@Tag(name = "EmployeeRegistrationController", description = "Controller for Employee Registration Page")
@RequestMapping("/api/employee")
@RequiredArgsConstructor
public class MasEmployeeController {
    private static final Logger logger = LoggerFactory.getLogger(MasEmployeeController.class);

    private final MasEmployeeService masEmployeeService;

    @Autowired
    private MasEmployeeRepository masEmployeeRepository;


    @GetMapping("/getAllEmployee")
    public ResponseEntity<ApiResponse<List<MasEmployeeDTO>>> getAllEmployees() {
        logger.info("Received request to fetch all Employees.");
        return ResponseEntity.ok(masEmployeeService.getAllEmployees());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<MasEmployeeDTO>>> getEmployeesByStatus(@PathVariable String status) {
        logger.info("Received request to fetch Employees with status: {}", status);
        return ResponseEntity.ok(masEmployeeService.getEmployeesByStatus(status));
    }

    @GetMapping("/employee/{id}")
    public ResponseEntity<ApiResponse<MasEmployeeDTO>> getEmployeeById(@PathVariable Long id) {
        logger.info("Received request to fetch Employee with ID: {}", id);
        return ResponseEntity.ok(masEmployeeService.getEmployeeById(id));
    }

    @PutMapping(value = "/employee/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<MasEmployee>> updateEmployee(@PathVariable Long id, @ModelAttribute MasEmployeeRequest masEmployeeRequest) {
        logger.info("Received request to update Employee with ID: {}", id);
        return ResponseEntity.ok(masEmployeeService.updateEmployee(id, masEmployeeRequest));
    }

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createEmployee(@ModelAttribute MasEmployeeRequest masEmployeeRequest) {
        logger.info("📥 Received Employee Request: {}", masEmployeeRequest);
        return ResponseEntity.ok(masEmployeeService.createEmployee(masEmployeeRequest));
    }

    @PutMapping("/approve/{id}/{deptId}")
    public ResponseEntity<ApiResponse<MasEmployee>> approveEmpAndCreateUser(@PathVariable Long id, @PathVariable Long deptId) {
        logger.info("Received request to approve Employee and create user with ID: {}", id);
        logger.info("Department ID for employee assignment: {}", deptId);
        return ResponseEntity.ok(masEmployeeService.updateEmployeeApprovalStatus(id, deptId));
    }

    @PostMapping(value = "/create-and-approve", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<MasEmployee>> createEmpAndUser(@ModelAttribute MasEmployeeRequest masEmployeeRequest) {
        logger.info("Received request to create and approve a new Employee: {}", masEmployeeRequest);
        return ResponseEntity.ok(masEmployeeService.createAndApproveEmployee(masEmployeeRequest));
    }

    @GetMapping("/getProfileImageSrcInEmployee/{empId}")
    public ResponseEntity<?> getProfileImageSrc(@PathVariable Long empId) {
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
                contentType = "application/octet-stream"; // Fallback
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(imageBytes);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error reading image: " + e.getMessage());
        }
    }

    @GetMapping("/viewDocument")
    public ResponseEntity<?> viewDocument(@RequestParam String filePath) {
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to read file");
        }
    }


}