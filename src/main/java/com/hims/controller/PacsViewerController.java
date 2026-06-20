package com.hims.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.response.ApiResponse;
import com.hims.response.WeasisLaunchResponse;
import com.hims.service.PacsViewerService;
import com.hims.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pacs")
@RequiredArgsConstructor
public class PacsViewerController {

    private final PacsViewerService pacsViewerService;

    @GetMapping("/launch-url")
    public ResponseEntity<ApiResponse<WeasisLaunchResponse>> getLaunchUrl(
            @RequestParam String uhid,
            @RequestParam String orderNo) {
        WeasisLaunchResponse response = pacsViewerService.generateLaunchResponse(uhid, orderNo);
        return ResponseEntity.ok(ResponseUtils.createSuccessResponse(
                response,
                new TypeReference<>() {}
        ));
    }
}
