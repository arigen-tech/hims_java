package com.hims.controller;

import com.hims.request.MasTemplateRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasTemplateResponse;
import com.hims.service.MasTemplateService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "MasTemplateController", description = "Controller for handling Mas Templates")
@RequestMapping("/mas-templates")
public class MasTemplateController {

    @Autowired
    private MasTemplateService masTemplateService;

    @GetMapping("/getAllTemplates/{flag}")
    public ApiResponse<List<MasTemplateResponse>> getAllTemplates(@PathVariable int flag) {
        return masTemplateService.getAllTemplates(flag);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MasTemplateResponse>> getTemplateById(@PathVariable Long id) {
        return new ResponseEntity<>(masTemplateService.getTemplateById(id), HttpStatus.OK);
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<MasTemplateResponse>> createTemplate(@RequestBody MasTemplateRequest request) {
        return new ResponseEntity<>(masTemplateService.createTemplate(request), HttpStatus.CREATED);
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<ApiResponse<MasTemplateResponse>> updateTemplate(@PathVariable Long id, @RequestBody MasTemplateRequest request) {
        return new ResponseEntity<>(masTemplateService.updateTemplate(id, request), HttpStatus.OK);
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<ApiResponse<String>> changeTemplateStatus(@PathVariable Long id, @RequestParam String status) {
        return new ResponseEntity<>(masTemplateService.changeTemplateStatus(id, status), HttpStatus.OK);
    }
}
