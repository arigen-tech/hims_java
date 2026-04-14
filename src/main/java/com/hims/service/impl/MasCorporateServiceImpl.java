package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.MasCorporate;
import com.hims.entity.repository.MasCorporateRepository;
import com.hims.response.ApiResponse;
import com.hims.response.MasCorporateResponse;
import com.hims.service.MasCorporateService;
import com.hims.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class MasCorporateServiceImpl implements MasCorporateService {
    @Autowired
    private MasCorporateRepository repository;
    @Override
    public ApiResponse<List<MasCorporateResponse>> getAllMasCorporate(int flag) {

        log.info("Fetching Corporate list, flag={}", flag);

        try {
            List<MasCorporate> list = (flag == 1)
                    ? repository.findByStatusIgnoreCaseOrderByCorporateNameAsc(AppConstants.STATUS_Y.toLowerCase())
                    : repository.findAllByOrderByStatusDescLastChgDateDesc();

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::mapToResponse).toList(),
                    new TypeReference<>() {}
            );

        } catch (Exception e) {
            log.error("Error fetching Corporate list", e);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }
    private MasCorporateResponse mapToResponse(MasCorporate entity) {

        MasCorporateResponse res = new MasCorporateResponse();

        res.setCorporateId(entity.getCorporateId());
        res.setCorporateName(entity.getCorporateName());
        res.setCorporateCode(entity.getCorporateCode());
        res.setContactPerson(entity.getContactPerson());
        res.setContactNo(entity.getContactNo());

        res.setCreditAllowed(entity.getCreditAllowed());
        res.setCreditDays(entity.getCreditDays());

        res.setLastChgDate(entity.getLastChgDate());
        res.setStatus(entity.getStatus());

        return res;
    }
}
