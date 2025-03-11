package com.hims.controller;

import com.hims.response.ApiResponse;
import com.hims.response.UserApplicationResponse;
import com.hims.response.UserResponse;
import com.hims.service.UserService;
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
    @GetMapping("/all/{speciality}")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllApplications(@PathVariable Long speciality) {
        return new ResponseEntity<>(userService.getAllDoctorsBySpeciality(speciality), HttpStatus.OK);
    }

}
