package com.hims.service;

import com.hims.entity.MasServiceOpd;
import com.hims.request.MasServiceOpdRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasServiceOpdResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MasServiceOpdService {
    ApiResponse<Page<MasServiceOpdResponse>> getOpdTariffByDepartmentAndDoctor(Long hospitalId, Long departmentId, Long doctorId,String doctorName, Pageable pageable);

    ApiResponse<String> save(MasServiceOpdRequest req);

    ApiResponse<String> edit(Long id, MasServiceOpdRequest req);

    ApiResponse<String> updateStatus(Long id, String status);
}
