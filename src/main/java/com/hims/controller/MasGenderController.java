package com.hims.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasGender;
import com.hims.helperUtil.ResponseUtils;
import com.hims.response.ApiResponse;
import com.hims.response.MasGenderResponse;
import com.hims.service.MasGenderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "MasGenderController", description = "Controller for handling Gender Master")
@RequestMapping("/gender")
public class MasGenderController {

    @Autowired
    private MasGenderService masGenderService;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<MasGenderResponse>>> getAllGenders() {
        ApiResponse<List<MasGenderResponse>> response = masGenderService.getAllGenders();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
