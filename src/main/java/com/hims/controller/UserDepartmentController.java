package com.hims.controller;

import com.hims.request.UserDepartmentRequest;
import com.hims.request.UserDepartmentRequestOne;
import com.hims.response.ApiResponse;
import com.hims.response.UserDepartmentResponse;
import com.hims.service.UserDepartmentService;
import com.hims.service.impl.UserDepartmentServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "UserDepartmentController", description = "Controller for handling User Departments")
@RequestMapping("/user-departments")
public class UserDepartmentController {

    @Autowired
    private UserDepartmentService userDepartmentService;

    @Autowired
    private UserDepartmentServiceImpl userDepartmentServiceImpl;


    @GetMapping("/getAllUserDepartments")
    public ApiResponse<List<UserDepartmentResponse>> getAllUserDepartments() {
        return userDepartmentServiceImpl.getAllUserDepartments();
    }

    @PostMapping("/create")
    public ApiResponse<UserDepartmentResponse> addUserDepartment(@RequestBody UserDepartmentRequest request) {
        return userDepartmentServiceImpl.addUserDepartment(request);
    }

    @PutMapping("/edit/{id}")
    public ApiResponse<UserDepartmentResponse> updateUserDepartment(@PathVariable Long id, @RequestBody UserDepartmentResponse details) {
        return userDepartmentServiceImpl.updateUserDepartment(id, details);
    }

    @GetMapping("/{id}")
    public ApiResponse<UserDepartmentResponse> findById(@PathVariable Long id) {
        return userDepartmentServiceImpl.findById(id);
    }

    @GetMapping("/getByUserId{id}")
    public ApiResponse<List<UserDepartmentResponse>> getAllUserDepartmentsByUserId(@PathVariable Long id) {
        return userDepartmentServiceImpl.getAllUserDepartmentsByUserId(id);
    }

    @PutMapping("/addOrUpdateUserDept")
    public ResponseEntity<ApiResponse<String>> addOrUpdateUserDepartment(@RequestBody UserDepartmentRequestOne request) {
        ApiResponse<String> response = userDepartmentServiceImpl.addOrUpdateUserDept(request);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getStatus()));
    }
}
