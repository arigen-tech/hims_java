package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.MasPaymentGateway;
import com.hims.entity.repository.MasPaymentGatewayRepository;
import com.hims.exception.SDDException;
import com.hims.response.ApiResponse;
import com.hims.response.MasPaymentGatewayResponse;
import com.hims.service.MasPaymentGatewayService;
import com.hims.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MasPaymentGatewayServiceImpl implements MasPaymentGatewayService {

    private final MasPaymentGatewayRepository masPaymentGatewayRepository;
    @Override
    public ApiResponse<List<MasPaymentGatewayResponse>> getMasPaymentGateways(int flag) {
        try {

            List<MasPaymentGatewayResponse> responses;
          if(flag==1){
               responses=masPaymentGatewayRepository.findByStatusIgnoreCaseOrderByGatewayCodeDesc("Y")
                       .stream()
                       .map(entity -> new MasPaymentGatewayResponse(
                               entity.getGatewayId(),
                               entity.getGatewayCode(),
                               entity.getGatewayName()
                       )).toList();
          } else if (flag==0) {
                responses=masPaymentGatewayRepository.findByStatusInIgnoreCaseOrderByGatewayCodeDesc(List.of("Y","N"))
                      .stream()
                      .map(entity -> new MasPaymentGatewayResponse(
                              entity.getGatewayId(),
                              entity.getGatewayCode(),
                              entity.getGatewayName()
                      )).toList();
          }else {
              throw  new SDDException("Invalid Flag",
                      HttpStatus.NOT_FOUND.value(),
                      "Flag should be either 0 or 1");
          }

          return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
        } catch (Exception e) {
          log.info("Error occurred while fetching payment gateways: {}", e);
          return  ResponseUtils.createFailureResponse(null,
                  new TypeReference<>() {},
                  AppConstants.INTERNAL_SERVER_ERR_MSG,
                  HttpStatus.INTERNAL_SERVER_ERROR.value()
          );
        }
    }
}
