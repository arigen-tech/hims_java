package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.*;
import com.hims.entity.repository.MasBedRepository;
import com.hims.entity.repository.MasBedStatusRepo;
import com.hims.entity.repository.MasBedTypeRepository;
import com.hims.entity.repository.MasRoomRepo;
import com.hims.request.MasBedRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasBedResponse;
import com.hims.service.MasBedService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.hims.constants.AppConstants.*;

@Slf4j
@Service
public class MasBedServiceImpl implements MasBedService {

    @Autowired
    private AuthUtil authUtil;
    @Autowired
    private MasBedRepository masBedRepository;
    @Autowired
    private MasRoomRepo masRoomRepository;
    @Autowired
    private MasBedTypeRepository masBedTypeRepository;
    @Autowired
    private MasBedStatusRepo masBedStatusRepository;


    @Override
    public ApiResponse<?> createRoomCategory(MasBedRequest request) {
        try {
            User currentUser = authUtil.getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createNotFoundResponse(MSG_CURRENT_USER_NOT_FOUND, HttpStatus.NOT_FOUND.value());
            }

            MasBed masBed = new MasBed();
            masBed.setBedNumber(request.getBedNumber());
            masBed.setStatus(STATUS_ACTIVE);
            masBed.setCreatedBy(currentUser.getFirstName() + " " + currentUser.getLastName());
            masBed.setLastUpdatedBy(currentUser.getFirstName() + " " + currentUser.getLastName());
            masBed.setLastUpdateDate(LocalDate.now());

            // ROOM
            Optional<MasRoom> room = masRoomRepository.findById(request.getRoomId());
            if (room.isEmpty()) {
                return ResponseUtils.createNotFoundResponse("Room Not Found", HttpStatus.NOT_FOUND.value());
            }
            masBed.setRoomId(room.get());

            // BED TYPE
            Optional<MasBedType> bedType = masBedTypeRepository.findById(request.getBedTypeId());
            if (bedType.isEmpty()) {
                return ResponseUtils.createNotFoundResponse("Bed Type Not Found", HttpStatus.NOT_FOUND.value());
            }
            masBed.setBedTypeId(bedType.get());

            // BED STATUS
            Optional<MasBedStatus> bedStatus = masBedStatusRepository.findById(request.getBedStatusId());
            if (bedStatus.isEmpty()) {
                return ResponseUtils.createNotFoundResponse("Bed Status Not Found", HttpStatus.NOT_FOUND.value());
            }
            masBed.setBedStatusId(bedStatus.get());

            MasBed saved = masBedRepository.save(masBed);

            return ResponseUtils.createSuccessResponse(mapToResponse(saved), new TypeReference<>() {});

        } catch (Exception e) {
            log.error("addMasBed() Error :: ", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    MSG_INTERNAL_ERROR,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    public ApiResponse<?> updateRoomCategory(Long id, MasBedRequest request) {
        try {
            User currentUser = authUtil.getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createNotFoundResponse(MSG_CURRENT_USER_NOT_FOUND, HttpStatus.NOT_FOUND.value());
            }

            MasBed masBed = masBedRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Invalid Bed Id"));

            masBed.setBedNumber(request.getBedNumber());
            masBed.setLastUpdatedBy(currentUser.getFirstName() + " " + currentUser.getLastName());
            masBed.setLastUpdateDate(LocalDate.now());

            // ROOM
            Optional<MasRoom> room = masRoomRepository.findById(request.getRoomId());
            if (room.isEmpty()) {
                return ResponseUtils.createNotFoundResponse("Room Not Found", HttpStatus.NOT_FOUND.value());
            }
            masBed.setRoomId(room.get());

            // BED TYPE
            Optional<MasBedType> bedType = masBedTypeRepository.findById(request.getBedTypeId());
            if (bedType.isEmpty()) {
                return ResponseUtils.createNotFoundResponse("Bed Type Not Found", HttpStatus.NOT_FOUND.value());
            }
            masBed.setBedTypeId(bedType.get());

            // BED STATUS
            Optional<MasBedStatus> bedStatus = masBedStatusRepository.findById(request.getBedStatusId());
            if (bedStatus.isEmpty()) {
                return ResponseUtils.createNotFoundResponse("Bed Status Not Found", HttpStatus.NOT_FOUND.value());
            }
            masBed.setBedStatusId(bedStatus.get());

            MasBed saved = masBedRepository.save(masBed);

            return ResponseUtils.createSuccessResponse(mapToResponse(saved), new TypeReference<>() {});

        } catch (Exception e) {
            log.error("updateMasBed() Error :: ", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    MSG_INTERNAL_ERROR,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    public ApiResponse<MasBedResponse> changeActiveStatus(Long id, String status) {
        try {
            User currentUser = authUtil.getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createNotFoundResponse(MSG_CURRENT_USER_NOT_FOUND, HttpStatus.NOT_FOUND.value());
            }

            MasBed masBed = masBedRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Mas Bed Not Found"));

            masBed.setStatus(status);
            masBed.setLastUpdatedBy(currentUser.getFirstName() + " " + currentUser.getLastName());
            masBed.setLastUpdateDate(LocalDate.now());

            MasBed saved = masBedRepository.save(masBed);

            return ResponseUtils.createSuccessResponse(mapToResponse(saved), new TypeReference<>() {
            });

        } catch (Exception e) {
            log.error("changeMasBedStatus() Error :: ", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {
                    },
                    MSG_INTERNAL_ERROR,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    public ApiResponse<?> getById(Long id) {
        try {
            Optional<MasBed> masBed = masBedRepository.findById(id);
            if (masBed.isEmpty()) {
                return ResponseUtils.createNotFoundResponse("Mas Bed Not Found", HttpStatus.NOT_FOUND.value());
            }

            return ResponseUtils.createSuccessResponse(
                    mapToResponse(masBed.get()),
                    new TypeReference<>() {}
            );

        } catch (Exception e) {
            log.error("findById() Error :: ", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    MSG_INTERNAL_ERROR,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    public ApiResponse<?> getAll(int flag) {
        try {
            List<MasBed> masBeds;

            if (flag == FLAG_ALL) {
                masBeds = masBedRepository.findAllByOrderByStatusDescLastUpdateDateDesc();
            } else if (flag == FLAG_ACTIVE_ONLY) {
                masBeds = masBedRepository.findByStatusIgnoreCase(STATUS_ACTIVE);
            } else {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Invalid Flag Value , Provide flag as 0 or 1",
                        HttpStatus.BAD_REQUEST.value()
                );
            }

            return ResponseUtils.createSuccessResponse(
                    masBeds.stream().map(this::mapToResponse).toList(),
                    new TypeReference<>() {}
            );

        } catch (Exception e) {
            log.error("getAllMasBed Error :: ", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    MSG_INTERNAL_ERROR,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    private MasBedResponse mapToResponse(MasBed masBed) {
        MasBedResponse res = new MasBedResponse();

        res.setBedId(masBed.getBedId());
        res.setBedNumber(masBed.getBedNumber());
        res.setStatus(masBed.getStatus());
        res.setLastUpdateDate(masBed.getLastUpdateDate());
//        res.setCreatedBy(masBed.getCreatedBy());
//        res.setLastUpdatedBy(masBed.getLastUpdatedBy());

        // Room
        if (masBed.getRoomId() != null) {
            res.setRoomId(masBed.getRoomId().getRoomId());
            res.setRoomName(masBed.getRoomId().getRoomName());
            res.setDepartmentId(masBed.getRoomId().getMasDepartment().getId());
            res.setDepartmentName(masBed.getRoomId().getMasDepartment().getDepartmentName());
        }


        if (masBed.getBedTypeId() != null) {
            res.setBedTypeId(masBed.getBedTypeId().getBedTypeId());
            res.setBedTypeName(masBed.getBedTypeId().getBedTypeName());
        }


        if (masBed.getBedStatusId() != null) {
            res.setBedStatusId(masBed.getBedStatusId().getBedStatusId());
            res.setBedStatusName(masBed.getBedStatusId().getBedStatusName());
        }


        return res;
    }
}
