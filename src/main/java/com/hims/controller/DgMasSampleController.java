package com.hims.controller;

import com.hims.request.DgMasSampleRequest;
import com.hims.request.DgUomRequest;
import com.hims.response.ApiResponse;
import com.hims.response.DgMasSampleResponse;
import com.hims.response.DgUomResponse;
import com.hims.service.DgMasSampleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "DgMasSampleController", description = "This controller is used for any DgMasSample Related task.")
@RestController
@RequestMapping("/dg-mas-sample")
public class DgMasSampleController {
    @Autowired
    private DgMasSampleService dgMasSampleService;
    @PostMapping("/addDgMasSample")
    public ResponseEntity<ApiResponse<DgMasSampleResponse>> addDgMasSample(@RequestBody DgMasSampleRequest dgMasSampleRequest){
        return new ResponseEntity<>(dgMasSampleService.addDgMasSample(dgMasSampleRequest), HttpStatus.CREATED);
    }
    @GetMapping("/getByIdDgMasSample/{id}")
    public ResponseEntity<ApiResponse<DgMasSampleResponse>> getByIdDgMasSample(@PathVariable Long id){
        return new ResponseEntity<>(dgMasSampleService.getByIdDgMas(id), HttpStatus.OK);
    }
    @GetMapping("/getAllDgMasSample/{flag}")
    public ResponseEntity<ApiResponse<List<DgMasSampleResponse>>>  getAllDgMasSample(@PathVariable int flag){
        return new ResponseEntity<>(dgMasSampleService.getAllDgMas(flag), HttpStatus.OK);
    }
    @PutMapping("/updateByStatusDgMas/{id}")
    public ResponseEntity<ApiResponse<DgMasSampleResponse>> updateByStatusDgUom(@PathVariable Long id,@RequestParam String status){
        return new ResponseEntity<>(dgMasSampleService.updateByStatusDgUom(id,status), HttpStatus.OK);

    }
    @PutMapping("/updateByIdDgMAs/{id}")
    public ResponseEntity<ApiResponse<DgMasSampleResponse>> updateByIdDgUom(@PathVariable Long id,@RequestBody DgMasSampleRequest dgMasSampleRequest){
        return new ResponseEntity<>(dgMasSampleService.updateByIdDgUom(id,dgMasSampleRequest), HttpStatus.OK);

    }

}
