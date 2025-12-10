package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.*;
import com.hims.entity.repository.MasDepartmentRepository;
import com.hims.entity.repository.MasRoomRepo;
import com.hims.entity.repository.MasRoomCategoryRepo;
import com.hims.entity.repository.MasWardRepository;
import com.hims.request.MasRoomRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasRoomResponse;
import com.hims.service.MasRoomService;
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
public class MasRoomServiceImpl implements MasRoomService {

    private final MasRoomRepo masRoomRepo;
    private final MasRoomCategoryRepo masRoomCategoryRepo;
    private final MasWardRepository masWardRepo;
    private final AuthUtil authUtil;

    private final MasDepartmentRepository departmentRepository;

    @Override
    public ApiResponse<MasRoomResponse> createRoom(MasRoomRequest request) {
        try {
            log.info("createRoom() Started...");

            User currentUser = authUtil.getCurrentUser();
            if (currentUser == null)
                return ResponseUtils.createNotFoundResponse("Current User Not Found", HttpStatus.NOT_FOUND.value());

            MasRoomCategory category = masRoomCategoryRepo.findById(request.getRoomCategoryId())
                    .orElseThrow(() -> new RuntimeException("Invalid Room Category Id"));

            MasDepartment department = departmentRepository.findById(request.getDeptId())
                    .orElseThrow(() -> new RuntimeException("Invalid Department Id"));

            MasRoom entity = new MasRoom();
            entity.setRoomName(request.getRoomName());
            entity.setNoOfBeds(request.getNoOfBeds());
            entity.setMasRoomCategory(category);
            entity.setMasDepartment(department);
            entity.setStatus("y");
            entity.setCreatedBy(currentUser.getFirstName() + " " + currentUser.getLastName());
            entity.setLastUpdatedBy(currentUser.getFirstName() + " " + currentUser.getLastName());

            MasRoom saved = masRoomRepo.save(entity);

            log.info("createRoom() Ended...");
            return ResponseUtils.createSuccessResponse(mapToResponse(saved), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("createRoom() Error :: ", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<MasRoomResponse> updateRoom(Long roomId, MasRoomRequest request) {
        try {
            log.info("updateRoom() Started...");

            User currentUser = authUtil.getCurrentUser();
            if (currentUser == null)
                return ResponseUtils.createNotFoundResponse("Current User Not Found", HttpStatus.NOT_FOUND.value());

            MasRoom entity = masRoomRepo.findById(roomId)
                    .orElseThrow(() -> new RuntimeException("Invalid Room Id"));

            MasRoomCategory category = masRoomCategoryRepo.findById(request.getRoomCategoryId())
                    .orElseThrow(() -> new RuntimeException("Invalid Room Category Id"));

            MasDepartment department = departmentRepository.findById(request.getDeptId())
                    .orElseThrow(() -> new RuntimeException("Invalid Department Id"));

            entity.setRoomName(request.getRoomName());
            entity.setNoOfBeds(request.getNoOfBeds());
            entity.setMasRoomCategory(category);
            entity.setMasDepartment(department);
            entity.setLastUpdatedBy(currentUser.getFirstName() + " " + currentUser.getLastName());
//            entity.setLastUpdatedDate(LocalDate.now());

            MasRoom saved = masRoomRepo.save(entity);

            log.info("updateRoom() Ended...");
            return ResponseUtils.createSuccessResponse(mapToResponse(saved), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("updateRoom() Error :: ", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<MasRoomResponse> changeActiveStatus(Long roomId, String status) {
        try {
            log.info("changeActiveStatus() Started...");

            User currentUser = authUtil.getCurrentUser();
            if (currentUser == null)
                return ResponseUtils.createNotFoundResponse("Current User Not Found", HttpStatus.NOT_FOUND.value());

            MasRoom entity = masRoomRepo.findById(roomId)
                    .orElseThrow(() -> new RuntimeException("Invalid Room Id"));

            entity.setStatus(status);
            entity.setLastUpdatedBy(currentUser.getFirstName() + " " + currentUser.getLastName());
            entity.setLastUpdatedDate(LocalDate.now());

            MasRoom saved = masRoomRepo.save(entity);

            log.info("changeActiveStatus() Ended...");
            return ResponseUtils.createSuccessResponse(mapToResponse(saved), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("changeActiveStatus() Error :: ", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<MasRoomResponse> getById(Long roomId) {
        try {
            log.info("getById() Started...");

            User currentUser = authUtil.getCurrentUser();
            if (currentUser == null)
                return ResponseUtils.createNotFoundResponse("Current User Not Found", HttpStatus.NOT_FOUND.value());

            MasRoom entity = masRoomRepo.findById(roomId)
                    .orElseThrow(() -> new RuntimeException("Invalid Room Id"));

            return ResponseUtils.createSuccessResponse(mapToResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("getById() Error :: ", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<List<MasRoomResponse>> getAll(int flag) {
        try {
            log.info("getAll() Started...");

            User currentUser = authUtil.getCurrentUser();
            if (currentUser == null)
                return ResponseUtils.createNotFoundResponse("Current User Not Found", HttpStatus.NOT_FOUND.value());

            List<MasRoom> list;

            if (flag == 0) {
                list = masRoomRepo.findByStatusIgnoreCaseInOrderByLastUpdatedDateDesc(List.of("y", "n"));
            } else if (flag == 1) {
                list = masRoomRepo.findByStatusIgnoreCaseOrderByLastUpdatedDateDesc("y");
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
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    private MasRoomResponse mapToResponse(MasRoom entity) {
        MasRoomResponse response = new MasRoomResponse();
        response.setRoomId(entity.getRoomId());
        response.setRoomName(entity.getRoomName());
        response.setStatus(entity.getStatus());
        response.setLastUpdatedDate(entity.getLastUpdatedDate());

        response.setNoOfBeds(entity.getNoOfBeds());
        if (entity.getMasDepartment() != null) {
            response.setDepartmentId(entity.getMasDepartment().getId());
            response.setWardName(entity.getMasDepartment().getDepartmentName());
        }

        if (entity.getMasRoomCategory() != null) {
            response.setRoomCategoryId(entity.getMasRoomCategory().getRoomCategoryId());
            response.setRoomCategoryName(entity.getMasRoomCategory().getRoomCategoryName());
        }

        return response;
    }
}
