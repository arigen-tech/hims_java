package com.hims.controller;

import com.hims.response.ApiResponse;
import com.hims.response.MasStoreItemResponse;
import com.hims.response.UserApplicationResponse;
import com.hims.response.UserResponse;
import com.hims.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "UserController", description = "This controller is used for any User Related task.")
@RequestMapping("/users")
@Slf4j
public class UserController {
    @Autowired
    UserService userService;

    @GetMapping("/doctorBySpeciality/{speciality}")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllApplications(@PathVariable Long speciality) {
        return new ResponseEntity<>(userService.getAllDoctorsBySpeciality(speciality), HttpStatus.OK);
    }

    @GetMapping("/allDoctor/list")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllDoctors() {
        return ResponseEntity.ok(userService.getAllDoctors());
    }


    @GetMapping("/getAll/{flag}")
    @Operation(summary = "Get all users", description = "Get all users with filter by status (0: all users, 1: active users only)")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers(@PathVariable int flag) {
        return new ResponseEntity<>(userService.getAllUsers(flag), HttpStatus.OK);
    }
    @GetMapping("/getByUserName/{user}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable String user) {
        ApiResponse<UserResponse> response =userService.findByUser(user);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
