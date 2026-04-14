package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.MasInsurance;
import com.hims.entity.repository.MasInsuranceRepository;
import com.hims.response.ApiResponse;
import com.hims.response.MasInsuranceResponse;
import com.hims.service.MasInsuranceService;
import com.hims.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class MasInsuranceServiceImpl implements MasInsuranceService {
    @Autowired
    private MasInsuranceRepository repository;
    @Override
    public ApiResponse<List<MasInsuranceResponse>> getAllMasInsurance(int flag) {

        log.info("Fetching Insurance list, flag={}", flag);

        try {
            List<MasInsurance> list = (flag == 1)
                    ? repository.findByStatusIgnoreCaseOrderByInsuranceNameAsc(AppConstants.STATUS_Y.toLowerCase())
                    : repository.findAllByOrderByStatusDescLastChgDateDesc();

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::mapToResponse).toList(),
                    new TypeReference<>() {}
            );

        } catch (Exception e) {
            log.error("Error fetching Insurance list", e);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }
    private MasInsuranceResponse mapToResponse(MasInsurance entity) {

        MasInsuranceResponse res = new MasInsuranceResponse();

        res.setInsuranceId(entity.getInsuranceId());
        res.setInsuranceName(entity.getInsuranceName());
        res.setInsuranceCode(entity.getInsuranceCode());
        res.setContactPerson(entity.getContactPerson());
        res.setContactNo(entity.getContactNo());

        res.setLastChgDate(entity.getLastChgDate());
        res.setStatus(entity.getStatus());

        return res;
    }
}
