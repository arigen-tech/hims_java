package com.hims.m1.service.serviceImpl;

import com.hims.m1.apiResponse.ApiResponse;
import com.hims.m1.apiResponse.ResponseUtils;
import com.hims.m1.config.RequestHeaderContext;
import com.hims.m1.enumData.ErrorCodeEnum;
import com.hims.m1.exception.HeaderValidationException;
import com.hims.m1.exception.SDDException;
import com.hims.m1.response.MasterResponse;
import com.hims.m1.service.MasterService;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class MasterServiceImpl implements MasterService {



    @Override
    public ApiResponse<List<MasterResponse>> getVerificationType() {
        try {

            List<MasterResponse> responseList = new ArrayList<>();

            MasterResponse m1 = new MasterResponse();
            m1.setId("1001");
            m1.setMaster_name("Aadhaar Number");
            m1.setMaster_description("Aadhaar Number");

            MasterResponse m2 = new MasterResponse();
            m2.setId("1002");
            m2.setMaster_name("Mobile Number");
            m2.setMaster_description("Mobile Number");

            MasterResponse m3 = new MasterResponse();
            m3.setId("1003");
            m3.setMaster_name("Abha Address");
            m3.setMaster_description("Abha Address");

            MasterResponse m4 = new MasterResponse();
            m4.setId("1004");
            m4.setMaster_name("Abha Number");
            m4.setMaster_description("Abha register Mobile Number");


            responseList.add(m1);
            responseList.add(m2);
            responseList.add(m3);
            responseList.add(m4);

            log.info("Verification types fetched successfully, total: {}", responseList.size());

            return ResponseUtils.createSuccessResponse(
                    responseList,
                    new TypeReference<List<MasterResponse>>() {
                    },
                    true,
                    ErrorCodeEnum.PR1097.getMessage()
            );

        } catch (HeaderValidationException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error while fetching verification types", ex);
            throw new SDDException(
                    false,
                    "Internal error while fetching verification types",
                    ex
            );
        }
    }


}
