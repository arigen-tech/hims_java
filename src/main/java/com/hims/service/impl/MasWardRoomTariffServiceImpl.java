package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.request.MasWardRoomTariffRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasWardRoomTariffResponse;
import com.hims.service.MasWardRoomTariffService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MasWardRoomTariffServiceImpl implements MasWardRoomTariffService {

    private final MasWardRoomTariffRepo tariffRepository;
    private final MasWardRepository wardRepository;
    private final MasRoomRepo roomRepository;
    private final AuthUtil authUtil;

    @Override
    public ApiResponse<List<MasWardRoomTariffResponse>> getAllWardRoomTariffs(int flag) {
        try {
            log.info("getAllWardRoomTariffs() Started with flag: {}", flag);

            User currentUser = authUtil.getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createNotFoundResponse("Current User Not Found", HttpStatus.NOT_FOUND.value());
            }

            List<MasWardRoomTariff> tariffs;

            if (flag == 0) {
                tariffs = tariffRepository.findAllByOrderByStatusDescLastUpdatedDateDesc();
            } else if (flag == 1) {
                tariffs = tariffRepository.findByStatusIgnoreCaseOrderByEffectiveFromDesc("y");
            } else {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Invalid Flag Value. Provide flag as 0 or 1", HttpStatus.BAD_REQUEST.value());
            }

            log.info("getAllWardRoomTariffs() Ended. Found {} records", tariffs.size());
            return ResponseUtils.createSuccessResponse(
                    tariffs.stream().map(this::mapToResponse).collect(Collectors.toList()),
                    new TypeReference<>() {}
            );

        } catch (Exception e) {
            log.error("getAllWardRoomTariffs() Error :: ", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    @Transactional
    public ApiResponse<MasWardRoomTariffResponse> createWardRoomTariff(MasWardRoomTariffRequest request) {
        try {
            log.info("createWardRoomTariff() Started...");

            User currentUser = authUtil.getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createNotFoundResponse("Current User Not Found", HttpStatus.NOT_FOUND.value());
            }

            MasWard ward = wardRepository.findById(request.getWardId())
                    .orElseThrow(() -> new RuntimeException("Invalid Ward Id"));

            MasRoom room = roomRepository.findById(request.getRoomId())
                    .orElseThrow(() -> new RuntimeException("Invalid Room Id"));

            if (request.getEffectiveTo() != null && request.getEffectiveTo().isBefore(request.getEffectiveFrom())) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Effective To date cannot be before Effective From date", HttpStatus.BAD_REQUEST.value());
            }

            LocalDate effectiveTo = request.getEffectiveTo() != null ? request.getEffectiveTo() : LocalDate.of(9999, 12, 31);
            boolean overlapping = tariffRepository.existsOverlappingTariff(
                    request.getWardId(),
                    request.getRoomId(),
                    request.getEffectiveFrom(),
                    effectiveTo
            );

            if (overlapping) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "A tariff already exists for this ward-room combination within the specified date range",
                        HttpStatus.CONFLICT.value());
            }

            MasWardRoomTariff tariff = new MasWardRoomTariff();
            tariff.setWard(ward);
            tariff.setRoom(room);
            tariff.setTariff(request.getTariff());
            tariff.setEffectiveFrom(request.getEffectiveFrom());
            tariff.setEffectiveTo(request.getEffectiveTo());
            tariff.setStatus("y");

            String userName = currentUser.getFirstName() + " " + currentUser.getLastName();
            tariff.setCreatedBy(userName);
            tariff.setLastUpdatedBy(userName);
            tariff.setCreatedDate(LocalDateTime.now());

            MasWardRoomTariff saved = tariffRepository.save(tariff);

            log.info("createWardRoomTariff() Ended. Created tariff with ID: {}", saved.getWardRoomTariffId());
            return ResponseUtils.createSuccessResponse(mapToResponse(saved), new TypeReference<>() {});

        } catch (Exception e) {
            log.error("createWardRoomTariff() Error :: ", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    @Transactional
    public ApiResponse<MasWardRoomTariffResponse> updateWardRoomTariff(Long tariffId, MasWardRoomTariffRequest request) {
        try {
            log.info("updateWardRoomTariff() Started for ID: {}", tariffId);

            User currentUser = authUtil.getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createNotFoundResponse("Current User Not Found", HttpStatus.NOT_FOUND.value());
            }

            MasWardRoomTariff tariff = tariffRepository.findById(tariffId)
                    .orElseThrow(() -> new RuntimeException("Invalid Tariff Id"));

            MasWard ward = wardRepository.findById(request.getWardId())
                    .orElseThrow(() -> new RuntimeException("Invalid Ward Id"));

            MasRoom room = roomRepository.findById(request.getRoomId())
                    .orElseThrow(() -> new RuntimeException("Invalid Room Id"));

            if (request.getEffectiveTo() != null && request.getEffectiveTo().isBefore(request.getEffectiveFrom())) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Effective To date cannot be before Effective From date", HttpStatus.BAD_REQUEST.value());
            }

            // Check overlapping excluding current record
            LocalDate effectiveTo = request.getEffectiveTo() != null ? request.getEffectiveTo() : LocalDate.of(9999, 12, 31);
            boolean overlapping = tariffRepository.existsOverlappingTariff(
                    request.getWardId(),
                    request.getRoomId(),
                    request.getEffectiveFrom(),
                    effectiveTo
            );

            if (overlapping) {
                List<MasWardRoomTariff> overlappingTariffs = tariffRepository
                        .findByWard_WardIdAndRoom_RoomIdAndStatusIgnoreCaseOrderByEffectiveFromDesc(
                                request.getWardId(), request.getRoomId(), "y");

                for (MasWardRoomTariff t : overlappingTariffs) {
                    if (!t.getWardRoomTariffId().equals(tariffId)) {
                        return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                                "A tariff already exists for this ward-room combination within the specified date range",
                                HttpStatus.CONFLICT.value());
                    }
                }
            }

            tariff.setWard(ward);
            tariff.setRoom(room);
            tariff.setTariff(request.getTariff());
            tariff.setEffectiveFrom(request.getEffectiveFrom());
            tariff.setEffectiveTo(request.getEffectiveTo());
            tariff.setLastUpdatedBy(currentUser.getFirstName() + " " + currentUser.getLastName());

            MasWardRoomTariff updated = tariffRepository.save(tariff);

            log.info("updateWardRoomTariff() Ended. Updated tariff with ID: {}", updated.getWardRoomTariffId());
            return ResponseUtils.createSuccessResponse(mapToResponse(updated), new TypeReference<>() {});

        } catch (Exception e) {
            log.error("updateWardRoomTariff() Error :: ", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    @Transactional
    public ApiResponse<MasWardRoomTariffResponse> changeActiveStatus(Long tariffId, String status) {
        try {
            log.info("changeActiveStatus() Started for ID: {}", tariffId);

            User currentUser = authUtil.getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createNotFoundResponse("Current User Not Found", HttpStatus.NOT_FOUND.value());
            }

            MasWardRoomTariff tariff = tariffRepository.findById(tariffId)
                    .orElseThrow(() -> new RuntimeException("Invalid Tariff Id"));

            if (!status.equalsIgnoreCase("y") && !status.equalsIgnoreCase("n")) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Invalid status value. Use 'y' or 'n'.", HttpStatus.BAD_REQUEST.value());
            }

            tariff.setStatus(status.toLowerCase());
            tariff.setLastUpdatedBy(currentUser.getFirstName() + " " + currentUser.getLastName());

            MasWardRoomTariff updated = tariffRepository.save(tariff);

            log.info("changeActiveStatus() Ended. Status changed to: {} for ID: {}", status, tariffId);
            return ResponseUtils.createSuccessResponse(mapToResponse(updated), new TypeReference<>() {});

        } catch (Exception e) {
            log.error("changeActiveStatus() Error :: ", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<MasWardRoomTariffResponse> getWardRoomTariffById(Long tariffId) {
        try {
            log.info("getWardRoomTariffById() Started for ID: {}", tariffId);

            User currentUser = authUtil.getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createNotFoundResponse("Current User Not Found", HttpStatus.NOT_FOUND.value());
            }

            MasWardRoomTariff tariff = tariffRepository.findById(tariffId)
                    .orElseThrow(() -> new RuntimeException("Invalid Tariff Id"));

            log.info("getWardRoomTariffById() Ended");
            return ResponseUtils.createSuccessResponse(mapToResponse(tariff), new TypeReference<>() {});

        } catch (Exception e) {
            log.error("getWardRoomTariffById() Error :: ", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<List<MasWardRoomTariffResponse>> getTariffsByWardAndRoom(Long wardId, Long roomId) {
        try {
            log.info("getTariffsByWardAndRoom() Started for wardId: {}, roomId: {}", wardId, roomId);

            User currentUser = authUtil.getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createNotFoundResponse("Current User Not Found", HttpStatus.NOT_FOUND.value());
            }

            List<MasWardRoomTariff> tariffs = tariffRepository
                    .findByWard_WardIdAndRoom_RoomIdAndStatusIgnoreCaseOrderByEffectiveFromDesc(
                            wardId, roomId, "y");

            log.info("getTariffsByWardAndRoom() Ended. Found {} records", tariffs.size());
            return ResponseUtils.createSuccessResponse(
                    tariffs.stream().map(this::mapToResponse).collect(Collectors.toList()),
                    new TypeReference<>() {}
            );

        } catch (Exception e) {
            log.error("getTariffsByWardAndRoom() Error :: ", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    private MasWardRoomTariffResponse mapToResponse(MasWardRoomTariff tariff) {
        MasWardRoomTariffResponse response = new MasWardRoomTariffResponse();
        response.setId(tariff.getWardRoomTariffId());
        response.setTariff(tariff.getTariff());
        response.setEffectiveFrom(tariff.getEffectiveFrom());
        response.setEffectiveTo(tariff.getEffectiveTo());
        response.setStatus(tariff.getStatus());
        response.setCreatedBy(tariff.getCreatedBy());
        response.setCreatedDate(tariff.getCreatedDate());
        response.setLastUpdatedBy(tariff.getLastUpdatedBy());
        response.setLastUpdatedDate(tariff.getLastUpdatedDate());

        if (tariff.getWard() != null) {
            response.setWardId(tariff.getWard().getWardId());
            response.setWardName(tariff.getWard().getWardName());
        }

        if (tariff.getRoom() != null) {
            response.setRoomId(tariff.getRoom().getRoomId());
            response.setRoomName(tariff.getRoom().getRoomName());
        }

        return response;
    }
}