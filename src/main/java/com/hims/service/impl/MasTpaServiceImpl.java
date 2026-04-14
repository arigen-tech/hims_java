package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.MasTpa;
import com.hims.entity.repository.MasTpaRepository;
import com.hims.response.ApiResponse;
import com.hims.response.MasTpaResponse;
import com.hims.service.MasTpaService;
import com.hims.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class MasTpaServiceImpl implements MasTpaService {
    @Autowired
    private MasTpaRepository repository;
    @Override
    public ApiResponse<List<MasTpaResponse>> getAllMasTpa(int flag) {

        log.info("Fetching TPA list, flag={}", flag);

        try {
            List<MasTpa> list = (flag == 1)
                    ? repository.findByStatusIgnoreCaseOrderByTpaNameAsc(AppConstants.STATUS_Y.toLowerCase())
                    : repository.findAllByOrderByStatusDescLastChgDateDesc();

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::mapToResponse).toList(),
                    new TypeReference<>() {}
            );

        } catch (Exception e) {
            log.error("Error fetching TPA list", e);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }
    private MasTpaResponse mapToResponse(MasTpa entity) {

        MasTpaResponse res = new MasTpaResponse();

        res.setTpaId(entity.getTpaId());
        res.setTpaName(entity.getTpaName());
        res.setTpaCode(entity.getTpaCode());
        res.setContactPerson(entity.getContactPerson());
        res.setContactNo(entity.getContactNo());

        res.setLastChgDate(entity.getLastChgDate());
        res.setStatus(entity.getStatus());

        return res;
    }
}
