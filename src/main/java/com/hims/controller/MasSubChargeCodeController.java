package com.hims.controller;

import com.hims.request.MasSubChargeCodeReq;
import com.hims.response.ApiResponse;
import com.hims.response.MasSubChargeCodeDTO;
import com.hims.service.MasSubChargeCodeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Mas-Sub-Charge-Code-Controller", description = "Controller for Sub Charge Code")
@RequestMapping("/sub-charge-code")
public class MasSubChargeCodeController {

    @Autowired
    MasSubChargeCodeService subService;

    @PostMapping("/add")
    ResponseEntity<ApiResponse<MasSubChargeCodeDTO>> createSubCharge(@RequestBody MasSubChargeCodeReq codeReq){
        return new ResponseEntity<>(subService.createSubCharge(codeReq), HttpStatus.CREATED);
    }

    @PutMapping("/update/{subId}")
    ResponseEntity<ApiResponse<MasSubChargeCodeDTO>> updateSubCharge(
            @PathVariable Long subId,
            @RequestBody MasSubChargeCodeReq codeReq){
        return new ResponseEntity<>(subService.updateSubCharge(subId, codeReq), HttpStatus.ACCEPTED);
    }

    @PutMapping("/status/{subId}")
    ResponseEntity<ApiResponse<MasSubChargeCodeDTO>> changeStatus(
            @PathVariable Long subId,
            @RequestParam String status){
        return new ResponseEntity<>(subService.changeStatus(subId, status), HttpStatus.ACCEPTED);
    }

    @GetMapping("/{subId}")
    ResponseEntity<ApiResponse<MasSubChargeCodeDTO>> getBySubId(@PathVariable Long subId){
        return new ResponseEntity<>(subService.getBySubId(subId), HttpStatus.OK);
    }

    @GetMapping("/getAllSubCharge/{flag}")
    ResponseEntity<ApiResponse<List<MasSubChargeCodeDTO>>> getAllSubCharge(@PathVariable int flag){
        return new ResponseEntity<>(subService.getAllSubCharge(flag), HttpStatus.OK);
    }
}
