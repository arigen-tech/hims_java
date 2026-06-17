package com.hims.controller;

import com.hims.response.WeasisLaunchResponse;
import com.hims.service.PacsViewerService;
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
    public ResponseEntity<WeasisLaunchResponse> getLaunchUrl(
            @RequestParam String uhid,
            @RequestParam String orderNo) {
        return ResponseEntity.ok(pacsViewerService.generateLaunchResponse(uhid, orderNo));
    }
}
