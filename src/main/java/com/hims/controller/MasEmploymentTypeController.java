package com.hims.controller;

import com.hims.entity.MasEmploymentType;
import com.hims.request.MasEmploymentTypeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasEmploymentTypeResponse;
import com.hims.service.MasEmploymentTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/employmentType")
public class MasEmploymentTypeController {
    @Autowired
    private MasEmploymentTypeService masEmploymentTypeService;
    @GetMapping("/getAllEmploymentType/{flag}")
    public ResponseEntity<ApiResponse<List<MasEmploymentTypeResponse>>> getAllMasEmploymentType(@PathVariable int flag){
        return new ResponseEntity<>(masEmploymentTypeService.getAllMasEmploymentType(flag), HttpStatus.OK);

    }

    @GetMapping("/getEmploymentTypeById/{id}")
    public ResponseEntity<ApiResponse<MasEmploymentTypeResponse>> getMasEmploymentTypeById(@PathVariable Long id){
        return new ResponseEntity<>(masEmploymentTypeService.getMasEmploymentTypeId(id), HttpStatus.OK);

    }
    @PostMapping("/addMasEmploymentType")
    public ResponseEntity<ApiResponse<MasEmploymentTypeResponse>> addMasEmploymentType(@RequestBody MasEmploymentTypeRequest masEmploymentTypeRequest){
        return new ResponseEntity<>(masEmploymentTypeService.addMasEmploymentType(masEmploymentTypeRequest),HttpStatus.CREATED);
    }
    @PutMapping("/updateMasEmploymentTypeById/{id}")
    public ResponseEntity<ApiResponse<MasEmploymentTypeResponse>> updateMasEmploymentTypeById(@RequestBody MasEmploymentType masEmploymentType,@PathVariable Long id){
        return new ResponseEntity<>(masEmploymentTypeService.updateMasEmploymentTypeById(masEmploymentType,id),HttpStatus.OK);
    }
    @PutMapping("/updateMasEmploymentTypeByStatus/{id}/{status}")
    public ResponseEntity<ApiResponse<MasEmploymentTypeResponse>> updateMasEmploymentTypeByStatus(@PathVariable Long id,@PathVariable String status){
        return new ResponseEntity<>(masEmploymentTypeService.updateMasEmploymentTypeByStatus(id,status),HttpStatus.OK);
    }


}
