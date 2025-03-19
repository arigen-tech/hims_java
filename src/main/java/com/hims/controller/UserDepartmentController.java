package com.hims.controller;

import com.hims.request.UserDepartmentRequest;
import com.hims.response.ApiResponse;
import com.hims.response.UserDepartmentResponse;
import com.hims.service.UserDepartmentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "UserDepartmentController", description = "Controller for handling User Departments")
@RequestMapping("/user-departments")
public class UserDepartmentController {

    @Autowired
    private UserDepartmentService userDepartmentService;

    @GetMapping("/getAllUserDepartments")
    public ApiResponse<List<UserDepartmentResponse>> getAllUserDepartments() {
        return userDepartmentService.getAllUserDepartments();
    }

    @PostMapping("/create")
    public ApiResponse<UserDepartmentResponse> addUserDepartment(@RequestBody UserDepartmentRequest request) {
        return userDepartmentService.addUserDepartment(request);
    }

    @PutMapping("/edit/{id}")
    public ApiResponse<UserDepartmentResponse> updateUserDepartment(@PathVariable Long id, @RequestBody UserDepartmentResponse details) {
        return userDepartmentService.updateUserDepartment(id, details);
    }

    @GetMapping("/{id}")
    public ApiResponse<UserDepartmentResponse> findById(@PathVariable Long id) {
        return userDepartmentService.findById(id);
    }
}
