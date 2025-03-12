package com.hims.controller;

import com.hims.entity.MasUserDepartment;
import com.hims.request.MasDepartmentRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasDepartmentResponse;
import com.hims.response.MasUserDepartmentResponse;
import com.hims.service.MasDepartmentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "MasDepartmentController", description = "Controller for handling Department Master")
@RequestMapping("/department")
public class MasDepartmentController {

    @Autowired
    private MasDepartmentService masDepartmentService;

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<MasDepartmentResponse>> addDepartment(@RequestBody MasDepartmentRequest request) {
        return ResponseEntity.ok(masDepartmentService.addDepartment(request));
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<ApiResponse<MasDepartmentResponse>> editDepartment(@PathVariable Long id, @RequestBody MasDepartmentRequest request) {
        return ResponseEntity.ok(masDepartmentService.editDepartment(id, request));
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<ApiResponse<String>> changeDepartmentStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(masDepartmentService.changeDepartmentStatus(id, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MasDepartmentResponse>> getDepartmentById(@PathVariable Long id) {
        return ResponseEntity.ok(masDepartmentService.getDepartmentById(id));
    }

    @GetMapping("/getAllDepartments/{flag}")
    public ApiResponse<List<MasDepartmentResponse>> getAllDepartments(@PathVariable int flag) {
        return masDepartmentService.getAllDepartments(flag);
    }

    @GetMapping("/allUserDepartment")
    public ResponseEntity<ApiResponse<List<MasUserDepartmentResponse>>> getAllUserDepartments() {
        return ResponseEntity.ok(masDepartmentService.getAllMasUserDepartments());
    }
    @GetMapping("/userDepartments/{departmentId}")
    public ResponseEntity<ApiResponse<List<MasUserDepartmentResponse>>> getUserDepartmentsByDepartmentId(
            @PathVariable Long departmentId) {
        return ResponseEntity.ok(masDepartmentService.getMasUserDepartmentsByDepartmentId(departmentId));
    }


}
