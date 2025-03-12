package com.hims.controller;

import com.hims.request.MasIdentificationTypeRequest;
import com.hims.response.MasIdentificationTypeResponse;
import com.hims.response.ApiResponse;
import com.hims.service.MasIdentificationTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/identification-types")
public class MasIdentificationTypeController {

    @Autowired
    private MasIdentificationTypeService masIdentificationTypeService;

    @PostMapping("/create")
    public ApiResponse<MasIdentificationTypeResponse> addIdentificationType(@RequestBody MasIdentificationTypeRequest request) {
        return masIdentificationTypeService.addIdentificationType(request);
    }

    @PutMapping("/status/{id}")
    public ApiResponse<String> changeIdentificationStatus(@PathVariable Long id, @RequestParam String status) {
        return masIdentificationTypeService.changeIdentificationStatus(id, status);
    }

    @PutMapping("/edit/{id}")
    public ApiResponse<MasIdentificationTypeResponse> editIdentificationType(@PathVariable Long id, @RequestBody MasIdentificationTypeRequest request) {
        return masIdentificationTypeService.editIdentificationType(id, request);
    }

    @GetMapping("/{id}")
    public ApiResponse<MasIdentificationTypeResponse> getIdentificationTypeById(@PathVariable Long id) {
        return masIdentificationTypeService.getIdentificationTypeById(id);
    }

    @GetMapping("/getAllIdentificationTypes/{flag}")
    public ApiResponse<List<MasIdentificationTypeResponse>> getAllIdentificationTypes(@PathVariable int flag) {
        return masIdentificationTypeService.getAllIdentificationTypes(flag);
    }
}
