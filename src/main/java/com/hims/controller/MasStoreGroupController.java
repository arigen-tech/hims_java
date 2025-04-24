package com.hims.controller;

import com.hims.entity.MasStoreGroup;
import com.hims.request.MasStoreGroupRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasStoreGroupResponse;
import com.hims.service.MasStoreGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/masStoreGroup")
public class MasStoreGroupController {
    @Autowired
    private MasStoreGroupService masStoreGroupService;

    @GetMapping("/getByAllId/{flag}")
    public ResponseEntity<ApiResponse<List<MasStoreGroupResponse>>> getMasStoreGroupByAllId(@PathVariable int flag){

        return new ResponseEntity<>(masStoreGroupService.getMasStoreGroupAllId(flag), HttpStatus.OK);
    }


    @GetMapping("/getById/{id}")
    public ResponseEntity<ApiResponse<MasStoreGroupResponse>> getMasStoreGroupById(@RequestParam int id){

        return new ResponseEntity<>(masStoreGroupService.getMasStoreGroup(id), HttpStatus.OK);
    }
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<MasStoreGroupResponse>> addMasStoreGroup(@RequestBody MasStoreGroupRequest masStoreGroupRequest){
        return new ResponseEntity<>(masStoreGroupService.addMasStoreGroup(masStoreGroupRequest), HttpStatus.CREATED);
    }
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<MasStoreGroupResponse>> updateMasStoreGroup(@RequestParam int id,@RequestBody MasStoreGroup masStoreGroup) {
        return new ResponseEntity<>(masStoreGroupService.updateMasStoreGroup(id,masStoreGroup), HttpStatus.OK);
    }
    @PutMapping("/update/{id}/{status}")
    public ResponseEntity<ApiResponse<MasStoreGroupResponse>> updateStatusMasStoreGroup(@RequestParam int id,@RequestParam String status) {
        return new ResponseEntity<>(masStoreGroupService.updateStatusMasStoreGroup(id,status), HttpStatus.OK);
    }
}