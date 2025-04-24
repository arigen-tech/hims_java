package com.hims.controller;

import com.hims.request.MasItemTypeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasItemTypeResponse;
import com.hims.service.MasItemTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/MasItemType")
public class MasItemTypeController {
    @Autowired
    private MasItemTypeService masItemTypeService;
    @PostMapping("/addMasItemType")
    public ResponseEntity<ApiResponse<MasItemTypeResponse>> addMasTypeItem(@RequestBody MasItemTypeRequest masItemTypeRequest){
        return new ResponseEntity<>(masItemTypeService.addMasItemType(masItemTypeRequest), HttpStatus.CREATED);

    }
    @PutMapping("/updateMasItemTypeId/{id}")
    public ResponseEntity<ApiResponse<MasItemTypeResponse>> updateMasTypeItemID(@PathVariable int id, @RequestBody MasItemTypeRequest masItemTypeRequest){
        return new ResponseEntity<>(masItemTypeService.updateMasItemTypeID(id,masItemTypeRequest), HttpStatus.OK);

    }
    @PutMapping("/updateMasItemTypeStatus/{id}/{status}")
    public ResponseEntity<ApiResponse<MasItemTypeResponse>> updateMasTypeItemStatus(@PathVariable int id, @PathVariable String status){
        return new ResponseEntity<>(masItemTypeService.updateMasItemTypeStatus(id,status), HttpStatus.OK);

    }

    @GetMapping("/getByIdMasItemTypeStatus/{id}")
    public ResponseEntity<ApiResponse<MasItemTypeResponse>> getByMasTypeItemStatus(@PathVariable int id){
        return new ResponseEntity<>(masItemTypeService.getByMasItemTypeStatus(id), HttpStatus.OK);

    }

    @GetMapping("/getByAllMasItemTypeStatus/{flag}")
    public ResponseEntity<ApiResponse<List<MasItemTypeResponse>>> getByAllMasTypeItemStatus(@PathVariable int flag){
        return new ResponseEntity<>(masItemTypeService.getAllMasItemTypeStatus(flag), HttpStatus.OK);

    }
}
