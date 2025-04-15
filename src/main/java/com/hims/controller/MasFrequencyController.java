package com.hims.controller;

import com.hims.request.MasFrequencyRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasFrequencyResponse;
import com.hims.service.MasFrequencyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name="MasFrequencyController")
@RequestMapping("/MasFrequencyController")
public class MasFrequencyController {
    @Autowired
    private MasFrequencyService masFrequencyService;

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<MasFrequencyResponse>> createMasFrequency(@RequestBody MasFrequencyRequest masFrequencyRequest){
        return new ResponseEntity<>(masFrequencyService.createMasFrequency( masFrequencyRequest), HttpStatus.CREATED);

    }
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<MasFrequencyResponse>> updateMasFrequency(@PathVariable Long id,@RequestBody MasFrequencyRequest masFrequencyRequest ){
        return new ResponseEntity<>(masFrequencyService.updateMasFrequency(id,masFrequencyRequest), HttpStatus.OK);

    }
    @PutMapping("/idUpdate/{id}/{status}")
    public ResponseEntity<ApiResponse<MasFrequencyResponse>> updateMasFrequencyByStatus(@PathVariable Long id,@PathVariable String status ){
        return new ResponseEntity<>(masFrequencyService.updateMasFrequencyByStatus(id,status), HttpStatus.OK);

    }
    @GetMapping("/getById/{id}")
    public ResponseEntity<ApiResponse<MasFrequencyResponse>> getByIdMasFrequency(@PathVariable Long id ){
        return new ResponseEntity<>(masFrequencyService.getByIdMasFrequency(id), HttpStatus.OK);

    }
    @GetMapping("/getByAll/{flag}")
    public ResponseEntity<ApiResponse<List<MasFrequencyResponse>>> getByMasFrequency(@PathVariable int flag ){
        return new ResponseEntity<>(masFrequencyService.getByAllMasFrequency(flag), HttpStatus.OK);

    }


}

