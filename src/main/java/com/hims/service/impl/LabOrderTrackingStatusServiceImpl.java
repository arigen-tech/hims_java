package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.LabOrderTrackingStatus;
import com.hims.entity.User;
import com.hims.entity.repository.LabOrderTrackingStatusRepository;
import com.hims.request.LabOrderTrackingStatusRequest;
import com.hims.response.ApiResponse;
import com.hims.response.LabOrderTrackingStatusResponse;
import com.hims.service.LabOrderTrackingStatusService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LabOrderTrackingStatusServiceImpl implements LabOrderTrackingStatusService {

    private  final LabOrderTrackingStatusRepository orderTrackingStatusRepository;

    private final AuthUtil authUtil;


    @Override
    public ApiResponse<LabOrderTrackingStatusResponse> create(LabOrderTrackingStatusRequest request) {
        try {
            log.info("labOrderStatusCreate() started..");
            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 404);
            }
            LabOrderTrackingStatus entity=new LabOrderTrackingStatus();
            entity.setOrderStatusCode(request.getOrderStatusCode());
            entity.setOrderStatusName(request.getOrderStatusName());
            entity.setDescription(request.getDescription());
            entity.setStatus("y");
            entity.setCreatedBy(user.getFirstName()+" "+user.getMiddleName()+" "+user.getLastName());
            entity.setUpdatedBy(user.getFirstName()+" "+user.getMiddleName()+" "+user.getLastName());
            LabOrderTrackingStatus save = orderTrackingStatusRepository.save(entity);
            log.info("labOrderStatusCreate() ended..");
            return ResponseUtils.createSuccessResponse(mapToResponse(save), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("create() Error :: ",e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},"Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    private  LabOrderTrackingStatusResponse mapToResponse(LabOrderTrackingStatus entity){
        LabOrderTrackingStatusResponse response= new LabOrderTrackingStatusResponse();
        response.setOrderStatusId(entity.getOrderStatusId());
        response.setOrderStatusCode(entity.getOrderStatusCode());
        response.setOrderStatusName(entity.getOrderStatusName());
        response.setDescription(entity.getDescription());
        response.setStatus(entity.getStatus());
        response.setUpdateDate(entity.getUpdateDate());
        return response;
    }
}
