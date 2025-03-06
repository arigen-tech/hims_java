package com.hims.controller;

import com.hims.request.MasOpdSessionRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasOpdSessionResponse;
import com.hims.service.MasOpdSessionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "MasOpdSessionController", description = "Controller for handling OPD Session Master")
@RequestMapping("/opd-session")
public class MasOpdSessionController {

    @Autowired
    private MasOpdSessionService masOpdSessionService;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<MasOpdSessionResponse>>> getAllOpdSessions() {
        ApiResponse<List<MasOpdSessionResponse>> response = masOpdSessionService.getAllOpdSessions();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MasOpdSessionResponse>> getOpdSessionById(@PathVariable Long id) {
        return ResponseEntity.ok(masOpdSessionService.findById(id));
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<MasOpdSessionResponse>> addSession(@RequestBody MasOpdSessionRequest request) {
        ApiResponse<MasOpdSessionResponse> response = masOpdSessionService.addSession(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<MasOpdSessionResponse>> updateSession(
            @PathVariable Long id,
            @RequestBody MasOpdSessionRequest request) {

        ApiResponse<MasOpdSessionResponse> response = masOpdSessionService.updateSession(id, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<ApiResponse<MasOpdSessionResponse>> changeStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(masOpdSessionService.changeStatus(id, status));
    }
}
