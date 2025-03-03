package com.hims.controller;

import com.hims.response.ApiResponse;
import com.hims.response.MasRelationResponse;
import com.hims.service.MasRelationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "MasRelationController", description = "Controller for handling Relation Master")
@RequestMapping("/relation")
public class MasRelationController {

    @Autowired
    private MasRelationService masRelationService;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<MasRelationResponse>>> getAllRelations() {
        ApiResponse<List<MasRelationResponse>> response = masRelationService.getAllRelations();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
