package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.MasIcd;
import com.hims.entity.repository.MasIcdRepository;
import com.hims.response.ApiResponse;
import com.hims.response.MasIcdResponse;
import com.hims.service.MasIcdService;
import com.hims.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class MasIcdServiceImpl implements MasIcdService {

    @Autowired
    private MasIcdRepository masIcdRepository;


//    @Override
//    public ApiResponse<List<MasIcdResponse>> getAllIcds(int flag) {
//
//        List<MasIcd> icdList;
//
//        if (flag == 1) {
//            // Only status 'Y'
//            icdList = masIcdRepository.findByStatusIgnoreCase("Y");
//
//        } else if (flag == 0) {
//            // All records (Y or N)
//            icdList = masIcdRepository.findByStatusInIgnoreCase(List.of("Y", "N"));
//
//        } else {
//            // Invalid flag
//            return ResponseUtils.createFailureResponse(
//                    null,
//                    new TypeReference<>() {},
//                    "Invalid flag value. Use 0 or 1.",
//                    400
//            );
//        }
//
//        List<MasIcdResponse> responses = icdList.stream()
//                .map(this::convertToResponse)
//                .collect(Collectors.toList());
//
//        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
//    }

//    private MasIcdResponse convertToResponse(MasIcd entity) {
//        MasIcdResponse res = new MasIcdResponse();
//        res.setIcdId(entity.getIcdId());
//        res.setIcdCode(entity.getIcdCode());
//        res.setIcdName(entity.getIcdName());
//        res.setStatus(entity.getStatus());
//        return res;
//    }
//
//
//    @Override
//    public ApiResponse<Page<MasIcdResponse>> getAllIcd(int flag, int page, int size, String search) {
//        Pageable pageable = PageRequest.of(page, size);
//
//        Page<MasIcd> icdPage;
//
//        if (search != null && !search.trim().isEmpty()) {
//            icdPage = masIcdRepository.searchICD(
//                    flag,
//                    "%" + search.toLowerCase() + "%",
//                    pageable
//            );
//        }
//
//
//        else if (flag == 1) {
//            icdPage = masIcdRepository.findByStatusIgnoreCase("y", pageable);
//        }
//        else {
//            icdPage = masIcdRepository.findByStatusInIgnoreCase(List.of("Y", "N"), pageable);
//        }
//
//        Page<MasIcdResponse> responsePage = icdPage.map(this::convertToResponse);
//        return ResponseUtils.createSuccessResponse(responsePage, new TypeReference<>() {});
//    }

    @Override
    public ApiResponse<Page<MasIcdResponse>> getAllIcd(int flag, int page, int size, String search) {

        try {
            log.info("getAllIcd method started with keyword - {}",search);

            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC,"icdName"));

            Page<MasIcdResponse> responsePage =
                    masIcdRepository.findAllIcdWithFilter(flag, search, pageable);


            log.info("getAllIcd method ended with keyword - {}",search);
            return ResponseUtils.createSuccessResponse(responsePage, new TypeReference<>() {});

        } catch (Exception e) {
            log.error("getAllIcd method error :: ",e);
            return  ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

    }

}
