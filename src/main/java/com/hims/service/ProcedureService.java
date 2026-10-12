package com.hims.service;

import com.hims.request.ProcedureRequestHd;
import com.hims.response.ProcedureResponse;
import com.hims.response.ProcedureWorklistResponse;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

public interface ProcedureService {
    @Transactional
    ProcedureResponse createProcedure(ProcedureRequestHd request,String billingMethod,String priority);


    Page<ProcedureWorklistResponse> getProcedureWorklist(
            String mobileNo,
            String patientName,
            int page,
            int size
    );
}
