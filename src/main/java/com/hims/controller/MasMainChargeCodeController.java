package com.hims.controller;

import com.hims.request.MasMainChargeCodeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasMainChargeCodeDTO;
import com.hims.service.MasMainChargeCodeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "MasMainChargeCodeController", description = "Controller for Main Charge Code")
@RequestMapping("/main-charge-code")
public class MasMainChargeCodeController {
    @Autowired
    private MasMainChargeCodeService masMainChargeCodeService;

    @GetMapping("/getAllChargeCode/{flag}")
    public ApiResponse<List<MasMainChargeCodeDTO>> getAllChargeCode (@PathVariable int flag){
        return masMainChargeCodeService.getAllChargeCode(flag);
    }

    @GetMapping("/{chargecodeId}")
    public ResponseEntity<ApiResponse<MasMainChargeCodeDTO>> getChargeCodeById (@PathVariable Long chargecodeId){
        return new ResponseEntity<> (masMainChargeCodeService.getChargeCodeById(chargecodeId), HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<MasMainChargeCodeDTO>> createChargeCode (@RequestBody MasMainChargeCodeRequest codeRequest){
        return new ResponseEntity<> (masMainChargeCodeService.createChargeCode(codeRequest), HttpStatus.CREATED);
    }

    @PutMapping ("/update/{chargecodeId}")
    public ResponseEntity<ApiResponse<MasMainChargeCodeDTO>>updateChargeCode(
            @PathVariable Long chargecodeId,
            @RequestBody MasMainChargeCodeRequest codeRequest){
        return new ResponseEntity<> (masMainChargeCodeService.updateChargeCode(chargecodeId, codeRequest), HttpStatus.ACCEPTED);
    }

    @PutMapping ("/status/{chargecodeId}")
    public ResponseEntity<ApiResponse<MasMainChargeCodeDTO>> changeStatus(
            @PathVariable Long chargecodeId,
            @RequestBody String status) {
        return new ResponseEntity<>(masMainChargeCodeService.changeStatus(chargecodeId, status), HttpStatus.ACCEPTED);
    }
}
