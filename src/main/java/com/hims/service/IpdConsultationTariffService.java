package com.hims.service;

import com.hims.request.IpdConsultationTariffRequest;
import com.hims.response.ApiResponse;
import com.hims.response.IpdConsultationTariffResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IpdConsultationTariffService {
    ApiResponse<Page<IpdConsultationTariffResponse>> getAllIpdConsultationTariff(Long departmentId, Long doctorId,
                                                                                 int page,
                                                                                 int size);

    ApiResponse<IpdConsultationTariffResponse> getByIdIpdConsultationTariff(Long id);

    ApiResponse<IpdConsultationTariffResponse> createIpdConsultationTariff(IpdConsultationTariffRequest request);

    ApiResponse<IpdConsultationTariffResponse> updateIpdConsultationTariff(Long id, IpdConsultationTariffRequest request);

    ApiResponse<IpdConsultationTariffResponse> changeStatusIpdConsultationTariff(Long id, String status);
}
