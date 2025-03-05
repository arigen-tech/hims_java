package com.hims.controller;

import com.hims.request.MasDepartmentTypeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasDepartmentTypeResponse;
import com.hims.service.MasDepartmentTypeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "MasDepartmentTypeController", description = "Controller for handling Department Type Master")
@RequestMapping("/department-type")
public class MasDepartmentTypeController {

    @Autowired
    private MasDepartmentTypeService masDepartmentTypeService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<MasDepartmentTypeResponse>> addDepartmentType(@RequestBody MasDepartmentTypeRequest request) {
        return ResponseEntity.ok(masDepartmentTypeService.addDepartmentType(request));
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<ApiResponse<String>> changeDepartmentTypeStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(masDepartmentTypeService.changeDepartmentTypeStatus(id, status));
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<ApiResponse<MasDepartmentTypeResponse>> editDepartmentType(@PathVariable Long id, @RequestBody MasDepartmentTypeRequest request) {
        return ResponseEntity.ok(masDepartmentTypeService.editDepartmentType(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MasDepartmentTypeResponse>> getDepartmentTypeById(@PathVariable Long id) {
        return ResponseEntity.ok(masDepartmentTypeService.getDepartmentTypeById(id));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<MasDepartmentTypeResponse>>> getAllDepartmentTypes() {
        return ResponseEntity.ok(masDepartmentTypeService.getAllDepartmentTypes());
    }
}
