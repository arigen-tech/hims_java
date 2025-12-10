package com.hims.service;

import com.hims.request.MasMedicalHistoryRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasMedicalHistoryResponse;
import com.hims.response.MasWardResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface MasMedicalHistoryService {
    ApiResponse<List<MasMedicalHistoryResponse>> getAllMas(int flag);

    ApiResponse<MasMedicalHistoryResponse> addMas(MasMedicalHistoryRequest request);

    ApiResponse<MasMedicalHistoryResponse> update(Long id, MasMedicalHistoryRequest request);

    ApiResponse<MasMedicalHistoryResponse> changeMasStatus(Long id, String status);
}
