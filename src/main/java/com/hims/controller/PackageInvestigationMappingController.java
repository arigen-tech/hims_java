package com.hims.controller;

import com.hims.request.PackageInvestigationMappingRequest;
import com.hims.response.ApiResponse;
import com.hims.response.PackageInvestigationMappingDTO;
import com.hims.service.PackageInvestigationMappingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Package-Investigation-Mapping", description = "Controller for Package Investigation Mapping")
@RequestMapping("/package-investigation-mapping")
public class PackageInvestigationMappingController {

    @Autowired
    PackageInvestigationMappingService mapService;

    @PostMapping("/add")
    ResponseEntity<ApiResponse<PackageInvestigationMappingDTO>> createPackMap(@RequestBody PackageInvestigationMappingRequest mapRequest){
        return new ResponseEntity<>(mapService.createPackMap(mapRequest), HttpStatus.CREATED);
    }

    @PutMapping("/update/{pimId}")
    ResponseEntity<ApiResponse<PackageInvestigationMappingDTO>> updatePackMap(
            @PathVariable Long pimId,
            @RequestBody PackageInvestigationMappingRequest mapRequest){
        return new ResponseEntity<>(mapService.updatePackMap(pimId, mapRequest), HttpStatus.CREATED);
    }

    @PutMapping("/updateStatus/{pimId}/{status}")
    ResponseEntity<ApiResponse<PackageInvestigationMappingDTO>> changeStatus(
            @PathVariable Long pimId,
            @PathVariable String status){
        return new ResponseEntity<>(mapService.changeStatus(pimId, status), HttpStatus.ACCEPTED);
    }

    @GetMapping("/{pimId}")
    ResponseEntity<ApiResponse<PackageInvestigationMappingDTO>> getByPimId (@PathVariable Long pimId){
        return new ResponseEntity<>(mapService.getByPimId(pimId), HttpStatus.OK);
    }

    @GetMapping("/getAllPackageMap/{flag}")
    ResponseEntity<ApiResponse<List<PackageInvestigationMappingDTO>>> getAllPackageMap (int flag){
        return new ResponseEntity<>(mapService.getAllPackageMap(flag), HttpStatus.OK);
    }
}
