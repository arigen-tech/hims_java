package com.hims.controller;

import com.hims.request.MasRelationRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasRelationResponse;
import com.hims.service.MasRelationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "MasRelationController", description = "Controller for handling Relation Master")
@RequestMapping("/relation")
public class MasRelationController {

    @Autowired
    private MasRelationService masRelationService;

    @GetMapping("/getAllRelations/{flag}")
    public ApiResponse<List<MasRelationResponse>> getAllRelations(@PathVariable int flag) {
        return masRelationService.getAllRelations(flag);
    }


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MasRelationResponse>> getRelationById(@PathVariable Long id) {
        ApiResponse<MasRelationResponse> response = masRelationService.findById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<MasRelationResponse>> addRelation(@RequestBody MasRelationRequest relationRequest) {
        ApiResponse<MasRelationResponse> response = masRelationService.addRelation(relationRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<MasRelationResponse>> updateRelation(
            @PathVariable Long id,
            @RequestBody MasRelationRequest relationRequest) {
        ApiResponse<MasRelationResponse> response = masRelationService.updateRelation(id, relationRequest);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<ApiResponse<MasRelationResponse>> changeStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        ApiResponse<MasRelationResponse> response = masRelationService.changeStatus(id, status);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
