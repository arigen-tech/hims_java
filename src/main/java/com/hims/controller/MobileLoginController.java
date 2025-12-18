package com.hims.controller;
import com.hims.request.LoginRequest;
import com.hims.response.ApiResponse;
import com.hims.service.MobileLoginService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@Tag(name = "MobileLogin", description = "This controller is used for any ")
@RequestMapping("/m logon")
@Slf4j
@RequiredArgsConstructor
public class MobileLoginController {
    @Autowired
    MobileLoginService mobileLoginService;
    @PostMapping("/mlogin")
    public ResponseEntity<ApiResponse>  loginResponse(@RequestBody LoginRequest request) {
        return new ResponseEntity<>(mobileLoginService.loginRequest(request), HttpStatus.OK);
    }
}
