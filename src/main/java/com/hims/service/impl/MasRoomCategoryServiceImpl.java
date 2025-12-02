package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasRoomCategory;
import com.hims.entity.User;
import com.hims.entity.repository.MasRoomCategoryRepo;
import com.hims.request.MasRoomCategoryRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasRoomCategoryResponse;
import com.hims.service.MasRoomCategoryService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MasRoomCategoryServiceImpl implements MasRoomCategoryService {

    private final MasRoomCategoryRepo masRoomCategoryRepo;
    private final AuthUtil authUtil;

    @Override
    public ApiResponse<MasRoomCategoryResponse> createRoomCategory(MasRoomCategoryRequest request) {

        try {
            log.info("createRoomCategory() Started...");

            User currentUser = authUtil.getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createNotFoundResponse("Current User Not Found", HttpStatus.NOT_FOUND.value());
            }

            MasRoomCategory entity = new MasRoomCategory();
            entity.setRoomCategoryName(request.getRoomCategoryName());
            entity.setStatus("y");
            entity.setCreatedBy(currentUser.getFirstName() + " " + currentUser.getLastName());
            entity.setUpdatedBy(currentUser.getFirstName() + " " + currentUser.getLastName());
            entity.setLastUpdatedDate(LocalDate.now());

            MasRoomCategory saved = masRoomCategoryRepo.save(entity);

            log.info("createRoomCategory() Ended...");
            return ResponseUtils.createSuccessResponse(mapToResponse(saved), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("createRoomCategory() Error :: ", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<MasRoomCategoryResponse> updateRoomCategory(Long roomCategoryId, MasRoomCategoryRequest request) {
        try {
            log.info("updateRoomCategory() Started...");

            User currentUser = authUtil.getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createNotFoundResponse("Current User Not Found", HttpStatus.NOT_FOUND.value());
            }

            MasRoomCategory entity = masRoomCategoryRepo.findById(roomCategoryId)
                    .orElseThrow(() -> new RuntimeException("Invalid Room Category Id"));

            entity.setRoomCategoryName(request.getRoomCategoryName());
            entity.setUpdatedBy(currentUser.getFirstName() + " " + currentUser.getLastName());
            entity.setLastUpdatedDate(LocalDate.now());

            MasRoomCategory saved = masRoomCategoryRepo.save(entity);

            log.info("updateRoomCategory() Ended...");
            return ResponseUtils.createSuccessResponse(mapToResponse(saved), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("updateRoomCategory() Error :: ", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<MasRoomCategoryResponse> changeActiveStatus(Long roomCategoryId, String status) {
        try {
            log.info("changeActiveStatus() Started...");

            User currentUser = authUtil.getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createNotFoundResponse("Current User Not Found", HttpStatus.NOT_FOUND.value());
            }

            MasRoomCategory entity = masRoomCategoryRepo.findById(roomCategoryId)
                    .orElseThrow(() -> new RuntimeException("Invalid Room Category Id"));

            entity.setStatus(status);
            entity.setUpdatedBy(currentUser.getFirstName() + " " + currentUser.getLastName());
            entity.setLastUpdatedDate(LocalDate.now());

            MasRoomCategory saved = masRoomCategoryRepo.save(entity);

            log.info("changeActiveStatus() Ended...");
            return ResponseUtils.createSuccessResponse(mapToResponse(saved), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("changeActiveStatus() Error :: ", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<MasRoomCategoryResponse> getById(Long roomCategoryId) {
        try {
            log.info("getById() Started...");

            User currentUser = authUtil.getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createNotFoundResponse("Current User Not Found", HttpStatus.NOT_FOUND.value());
            }

            MasRoomCategory entity = masRoomCategoryRepo.findById(roomCategoryId)
                    .orElseThrow(() -> new RuntimeException("Invalid Room Category Id"));

            return ResponseUtils.createSuccessResponse(mapToResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("getById() Error :: ", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<List<MasRoomCategoryResponse>> getAll(int flag) {
        try {
            log.info("getAll() Started...");

            User currentUser = authUtil.getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createNotFoundResponse("Current User Not Found", HttpStatus.NOT_FOUND.value());
            }

            List<MasRoomCategory> list;

            if (flag == 0) {
                list = masRoomCategoryRepo.findByStatusIgnoreCaseInOrderByLastUpdatedDateDesc(List.of("y", "n"));
            } else if (flag == 1) {
                list = masRoomCategoryRepo.findByStatusIgnoreCaseOrderByLastUpdatedDateDesc("y");
            } else {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Invalid Flag Value , Provide flag as 0 or 1", HttpStatus.BAD_REQUEST.value());
            }

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::mapToResponse).toList(),
                    new TypeReference<>() {}
            );

        } catch (Exception e) {
            log.error("getAll() Error :: ", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    private MasRoomCategoryResponse mapToResponse(MasRoomCategory entity) {
        MasRoomCategoryResponse response = new MasRoomCategoryResponse();
        response.setRoomCategoryId(entity.getRoomCategoryId());
        response.setRoomCategoryName(entity.getRoomCategoryName());
        response.setStatus(entity.getStatus());
        response.setLastUpdatedDate(entity.getLastUpdatedDate());
        return response;
    }
}
