package com.hims.controller;

import com.hims.response.ApiResponse;
import com.hims.response.MasDepartmentResponse;
import com.hims.service.MasDepartmentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "MasDepartmentController", description = "Controller for handling Department Master")
@RequestMapping("/department")
public class MasDepartmentController {

    @Autowired
    private MasDepartmentService masDepartmentService;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<MasDepartmentResponse>>> getAllDepartments() {
        ApiResponse<List<MasDepartmentResponse>> response = masDepartmentService.getAllDepartments();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
