package com.hims.controller;

import com.hims.request.DgUomRequest;
import com.hims.response.ApiResponse;
import com.hims.response.DgUomResponse;
import com.hims.service.DgUomService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "DgUom", description = "This controller only for use DgUom Master Related any task.")

@RequestMapping("/DgUomController")
public class DgUomController {
    @Autowired
    private DgUomService dgUomService;
    @PostMapping("/addDgUom")
    public ResponseEntity<ApiResponse<DgUomResponse>> addDgUom(@RequestBody DgUomRequest dgUomRequest){
    return new ResponseEntity<>(dgUomService.addDgUom(dgUomRequest), HttpStatus.CREATED);
    }
    @GetMapping("/getByIdDgUom/{id}")
    public ResponseEntity<ApiResponse<DgUomResponse>> getByIdDgUom(@PathVariable Long id){
        return new ResponseEntity<>(dgUomService.getByIdDgUom(id), HttpStatus.OK);
    }
    @GetMapping("/getAllDgUom/{flag}")
    public ResponseEntity<ApiResponse<List<DgUomResponse>>>  getAllDgUom(@PathVariable int flag){
        return new ResponseEntity<>(dgUomService.getAllDgUom(flag), HttpStatus.OK);
    }
    @PutMapping("/updateByStatusDgUom/{id}")
    public ResponseEntity<ApiResponse<DgUomResponse>> updateByStatusDgUom(@PathVariable Long id,@RequestParam String status){
        return new ResponseEntity<>(dgUomService.updateByStatusDgUom(id,status), HttpStatus.OK);

    }
    @PutMapping("/updateByIdDgUom/{id}")
    public ResponseEntity<ApiResponse<DgUomResponse>> updateByIdDgUom(@PathVariable Long id,@RequestBody DgUomRequest dgUomRequest){
        return new ResponseEntity<>(dgUomService.updateByIdDgUom(id,dgUomRequest), HttpStatus.OK);

    }


}
