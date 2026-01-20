package com.hims.controller;

import com.hims.request.PackageInvestigationMappingRequest;
import com.hims.response.ApiResponse;
import com.hims.response.InvestigationPackageDTO;
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
    private PackageInvestigationMappingService mapService;

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<List<PackageInvestigationMappingDTO>>> createPackMap(
            @RequestBody PackageInvestigationMappingRequest request) {
        return new ResponseEntity<>(mapService.createPackMap(request), HttpStatus.CREATED);
    }

    @GetMapping("/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<PackageInvestigationMappingDTO>>> getAllMappings(@PathVariable int flag) {
        return new ResponseEntity<>(mapService.getAllMappings(flag), HttpStatus.OK);
    }

    @PutMapping("/update/{pimId}")
    public ResponseEntity<ApiResponse<PackageInvestigationMappingDTO>> updatePackMap(
            @PathVariable Long pimId,
            @RequestBody PackageInvestigationMappingRequest mapRequest){
        return new ResponseEntity<>(mapService.updatePackMap(pimId, mapRequest), HttpStatus.CREATED);
    }

    @PutMapping("/updateStatus/{pimId}")
    public ResponseEntity<ApiResponse<PackageInvestigationMappingDTO>> changeStatus(
            @PathVariable Long pimId,
            @RequestParam String status){
        return new ResponseEntity<>(mapService.changeStatus(pimId, status), HttpStatus.ACCEPTED);
    }

    @GetMapping("/{pimId}")
    public ResponseEntity<ApiResponse<PackageInvestigationMappingDTO>> getByPimId (@PathVariable Long pimId){
        return new ResponseEntity<>(mapService.getByPimId(pimId), HttpStatus.OK);
    }

    @GetMapping("/getAllPackageMap/{flag}")
    public ResponseEntity<ApiResponse<List<InvestigationPackageDTO>>> getAllPackageMap (@PathVariable int flag){
        return new ResponseEntity<>(mapService.getAllPackageMap(flag), HttpStatus.OK);
    }

    // New endpoint to get investigations by package ID
    @GetMapping("/getByPackageId/{packageId}")
    public ResponseEntity<ApiResponse<List<PackageInvestigationMappingDTO>>> getInvestigationsByPackageId(@PathVariable Long packageId) {
        return new ResponseEntity<>(mapService.getInvestigationsByPackageId(packageId), HttpStatus.OK);
    }

    // New endpoint to update package investigations
    @PutMapping("/updatePackageInvestigations/{packageId}")
    public ResponseEntity<ApiResponse<List<PackageInvestigationMappingDTO>>> updatePackageInvestigations(
            @PathVariable Long packageId,
            @RequestBody PackageInvestigationMappingRequest request) {
        return new ResponseEntity<>(mapService.updatePackageInvestigations(packageId, request), HttpStatus.OK);
    }
}
