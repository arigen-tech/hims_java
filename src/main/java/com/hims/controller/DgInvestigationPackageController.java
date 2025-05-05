package com.hims.controller;

import com.hims.request.DgInvestigationPackageRequest;
import com.hims.response.ApiResponse;
import com.hims.response.DgInvestigationPackageDTO;
import com.hims.service.DgInvestigationPackageServices;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Dg-Investigation-Package-Controller", description = "Controller for Investigation Package")
@RequestMapping("/investigation-package")
public class DgInvestigationPackageController {

    @Autowired
    DgInvestigationPackageServices packService;

    @PostMapping("/add")
    ResponseEntity<ApiResponse<DgInvestigationPackageDTO>> createInvestPack(@RequestBody DgInvestigationPackageRequest packReq){
        return new ResponseEntity<>(packService.createInvestPack(packReq), HttpStatus.CREATED);
    }

    @PutMapping("/update/{packId}")
    ResponseEntity<ApiResponse<DgInvestigationPackageDTO>> updateInvestPack(
            @PathVariable Long packId,
            @RequestBody DgInvestigationPackageRequest packReq){
        return new ResponseEntity<>(packService.updateInvestPack(packId, packReq), HttpStatus.ACCEPTED);
    }

    @PutMapping("/status/{packId}")
    ResponseEntity<ApiResponse<DgInvestigationPackageDTO>> changeStatus(
            @PathVariable Long packId,
            @RequestParam String status){
        return new ResponseEntity<>(packService.changeStatus(packId, status), HttpStatus.ACCEPTED);
    }

    @GetMapping("/{packId}")
    ResponseEntity<ApiResponse<DgInvestigationPackageDTO>> getByPackId(@PathVariable Long packId){
        return new ResponseEntity<>(packService.getByPackId(packId), HttpStatus.OK);
    }

    @GetMapping("/getAllPackInvestigation/{flag}")
    ResponseEntity<ApiResponse<List<DgInvestigationPackageDTO>>> getAllPackInvestigation(@PathVariable int flag){
        return new ResponseEntity<>(packService.getAllPackInvestigation(flag), HttpStatus.OK);
    }
}
