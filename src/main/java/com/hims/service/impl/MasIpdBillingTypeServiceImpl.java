package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.MasIpdBillingType;
import com.hims.entity.repository.MasIpdBillingTypeRepository;
import com.hims.response.ApiResponse;
import com.hims.response.MasIpdBillingTypeResponse;
import com.hims.service.MasIpdBillingTypeService;
import com.hims.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class MasIpdBillingTypeServiceImpl implements MasIpdBillingTypeService {
    @Autowired
    private MasIpdBillingTypeRepository repository;
    @Override
    public ApiResponse<List<MasIpdBillingTypeResponse>> getAllMasIpdBillingType(int flag) {

        log.info("Fetching Billing Type list, flag={}", flag);

        try {
            List<MasIpdBillingType> list = (flag == 1)
                    ? repository.findByStatusIgnoreCaseOrderByBillingTypeNameAsc(AppConstants.STATUS_Y.toLowerCase())
                    : repository.findAllByOrderByStatusDescLastUpdateDateDesc();

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::mapToResponse).toList(),
                    new TypeReference<>() {}
            );

        } catch (Exception e) {
            log.error("Error fetching Billing Type list", e);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }
    private MasIpdBillingTypeResponse mapToResponse(MasIpdBillingType entity) {

        MasIpdBillingTypeResponse res = new MasIpdBillingTypeResponse();

        res.setBillingTypeId(entity.getBillingTypeId());
        res.setBillingTypeName(entity.getBillingTypeName());
        res.setDescription(entity.getDescription());
        res.setStatus(entity.getStatus());

        res.setLastUpdateDate(entity.getLastUpdateDate());

        return res;
    }
}
