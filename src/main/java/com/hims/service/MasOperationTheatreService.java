package com.hims.service;

import com.hims.request.OperationTheatreRequest;
import com.hims.response.ApiResponse;
import com.hims.response.OperationTheatreResponse;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public interface MasOperationTheatreService {

    ApiResponse<String> saveOperationTheatre(OperationTheatreRequest request);

    ApiResponse<List<OperationTheatreResponse>> getAllOperationTheatres(int flag);

    ApiResponse<OperationTheatreResponse> getById(Long id);

    ApiResponse<OperationTheatreResponse> changeStatus(Long id, String status);

    ApiResponse<String> updateOperationTheatre(Long id, OperationTheatreRequest request);
}
