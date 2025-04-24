package com.hims.controller;

import com.hims.request.MasStoreUnitRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasStoreUnitResponse;
import com.hims.service.MasStoreUnitService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "MasStoreUnitController", description = "Controller for handling Store units")
@RequestMapping("/store-unit")
public class MasStoreUnitController {

    @Autowired
    private MasStoreUnitService masStoreUnitService;

    @GetMapping("/getAllUnits/{flag}")
    public ApiResponse<List<MasStoreUnitResponse>> getAllUnits(@PathVariable int flag){
        return masStoreUnitService.getAllUnits(flag);
    }

    @GetMapping("/{unit_id}")
    public ResponseEntity<ApiResponse<MasStoreUnitResponse>> findByUnit(@PathVariable Long unit_id){
        return ResponseEntity.ok(masStoreUnitService.findByUnit(unit_id));
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<MasStoreUnitResponse>> addUnit(@RequestBody MasStoreUnitRequest unitRequest){
        ApiResponse<MasStoreUnitResponse> response = masStoreUnitService.addUnit(unitRequest);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update/{unit_id}")
    public ResponseEntity<ApiResponse<MasStoreUnitResponse>> updateUnit(
            @PathVariable Long unit_id,
            @RequestBody MasStoreUnitRequest unitRequest){
        ApiResponse<MasStoreUnitResponse> response = masStoreUnitService.updateUnit(unit_id, unitRequest);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/status/{unit_id}")
    public ResponseEntity<ApiResponse<MasStoreUnitResponse>> changeStat(
            @PathVariable Long unit_id,
            @RequestParam String stat){
        return ResponseEntity.ok(masStoreUnitService.changeStat(unit_id, stat));
    }

}
