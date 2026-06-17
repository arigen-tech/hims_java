package com.hims.service;

import com.hims.config.PacsProperties;
import com.hims.entity.PacsHmisStudy;
import com.hims.entity.repository.PacsHmisStudyRepository;
import com.hims.response.WeasisLaunchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class PacsViewerService {

    private final PacsProperties pacsProperties;
    private final PacsHmisStudyRepository pacsHmisStudyRepository;

    public WeasisLaunchResponse generateLaunchResponse(String uhid, String orderNo) {
        PacsHmisStudy study = pacsHmisStudyRepository.findAllByUhidAndOrderNo(uhid, orderNo)
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        NOT_FOUND,
                        "No PACS study found for uhid: " + uhid + " and orderNo: " + orderNo
                ));

        return WeasisLaunchResponse.builder()
                .studyInstanceUid(study.getStudyInstanceUid())
                .weasisUrl(generateWeasisUrl(study.getStudyInstanceUid()))
                .build();
    }

    public String generateWeasisUrl(String studyInstanceUid) {
        return pacsProperties.getWeasisConnectorUrl()
                + "?studyUID="
                + URLEncoder.encode(studyInstanceUid, StandardCharsets.UTF_8)
                + "&cdb";
    }
}
