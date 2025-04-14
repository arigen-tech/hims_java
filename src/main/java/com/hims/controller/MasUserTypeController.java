package com.hims.controller;

import com.hims.entity.MasUserType;
import com.hims.request.MasUserTypeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasUserTypeResponse;
import com.hims.service.MasUserTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/userType")
public class MasUserTypeController {
    @Autowired
    private MasUserTypeService masUserTypeService;

    @GetMapping("getAllUserType/{flag}")
    public ResponseEntity<ApiResponse<List<MasUserType>>> getAll(@PathVariable int flag){
       return new ResponseEntity<>( masUserTypeService.getAllMasUserType(flag), HttpStatus.OK);
    }

    @GetMapping("/getById/{id}")
    public ResponseEntity<ApiResponse<MasUserTypeResponse>> getByIdMasUserType(@PathVariable Long id){
        return new ResponseEntity<>(masUserTypeService.getByIdMasUserType(id),HttpStatus.OK) ;
    }

    @PostMapping("/newAddMasUserType")
    public ResponseEntity<ApiResponse<MasUserTypeResponse>> newAddMasUser(@RequestBody MasUserTypeRequest masUserTypeRequest){
        return new ResponseEntity<>(masUserTypeService.newAddMasUser(masUserTypeRequest),HttpStatus.OK);
    }
    @PutMapping("/updateById/{id}")
    public ResponseEntity<ApiResponse<MasUserTypeResponse>> updateMasUserType(@RequestBody MasUserType masUserType, @PathVariable Long id){
        return new ResponseEntity<>(masUserTypeService.updateMasUserType(masUserType,id),HttpStatus.OK);
    }
    @PutMapping("/updateByStatusById/{id}/{status}")
    public ResponseEntity<ApiResponse<MasUserTypeResponse>> updateMasUserTypeStatus(@PathVariable Long id,@PathVariable String status){
        return new ResponseEntity<>(masUserTypeService.updateMasUserTypeStatus(id,status),HttpStatus.OK);
    }

}
