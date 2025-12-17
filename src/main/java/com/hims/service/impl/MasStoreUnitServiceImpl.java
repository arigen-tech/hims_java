package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasStoreUnit;
import com.hims.entity.repository.MasStoreUnitRepository;
import com.hims.request.MasStoreUnitRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasStoreUnitResponse;
import com.hims.service.MasStoreUnitService;
import com.hims.utils.ResponseUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MasStoreUnitServiceImpl implements MasStoreUnitService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Autowired
    private MasStoreUnitRepository masUnitRepository;

    @Override
    public ApiResponse<List<MasStoreUnitResponse>> getAllUnits(int flag){
        List<MasStoreUnit> units;

        if (flag == 1) {
            units = masUnitRepository.findByStatusIgnoreCaseOrderByUnitNameAsc("Y");
        } else if (flag == 0) {
            units = masUnitRepository.findByStatusInIgnoreCaseOrderByLastChgDateDesc(List.of("Y", "N"));
        } else {
            return ResponseUtils.createFailureResponse
                    (null, new TypeReference<>() {}, "Invalid flag value. Use 0 or 1.", 400);
        }

        List<MasStoreUnitResponse> responses = units.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
    }

    private MasStoreUnitResponse convertToResponse(MasStoreUnit store) {
        MasStoreUnitResponse unitResponse = new MasStoreUnitResponse();
        unitResponse.setUnitId(store.getUnitId());
        unitResponse.setUnitName(store.getUnitName());
        unitResponse.setStatus(store.getStatus());
        unitResponse.setLastChgBy(store.getLastChgBy());
        unitResponse.setLastChgDate(store.getLastChgDate());
        unitResponse.setLastChgTime(store.getLastChangeTime());

        return unitResponse;
    }

    public ApiResponse<MasStoreUnitResponse> findByUnit(Long unitId) {
        Optional<MasStoreUnit> unit = masUnitRepository.findById(unitId);
        return unit.map(value ->
                ResponseUtils.createSuccessResponse(
                        convertToResponse(value), new TypeReference<>() {})
        ).orElseGet(() -> ResponseUtils.createNotFoundResponse("Session not found", 404));
    }

    // Add new store unit
    @Override
    public ApiResponse<MasStoreUnitResponse> addUnit(MasStoreUnitRequest unitRequest) {
        MasStoreUnit StoreUnit = new MasStoreUnit();
        StoreUnit.setUnitName(unitRequest.getUnitName());
        StoreUnit.setStatus(unitRequest.getStatus());
        StoreUnit.setLastChgBy(unitRequest.getLastChgBy());

        masUnitRepository.save(StoreUnit);
        return ResponseUtils.createSuccessResponse(convertToResponse(StoreUnit), new TypeReference<>() {});
    }

    @Override
    public ApiResponse<MasStoreUnitResponse> updateUnit(Long unit_id, MasStoreUnitRequest unitRequest) {
        Optional<MasStoreUnit> unitOptional = masUnitRepository.findById(unit_id);

        if (unitOptional.isPresent()) {
            MasStoreUnit unit = unitOptional.get();
            unit.setUnitName(unitRequest.getUnitName());
            unit.setStatus(unitRequest.getStatus());
            unit.setLastChgBy(unit.getLastChgBy());

            masUnitRepository.save(unit);
            return ResponseUtils.createSuccessResponse(convertToResponse(unit), new TypeReference<>() {});
        }

        return ResponseUtils.createNotFoundResponse("Session not found", HttpStatus.NOT_FOUND.value());
    }

//    change the status of Units
    @Transactional
    public ApiResponse<MasStoreUnitResponse> changeStat(Long unit_id, String stat) {
        Optional<MasStoreUnit> existingStoreOpt = masUnitRepository.findById(unit_id);
        if (existingStoreOpt.isPresent()) {
            MasStoreUnit existingUnit = existingStoreOpt.get();

            if (!stat.equalsIgnoreCase("y") && !stat.equalsIgnoreCase("n")) {
                return ResponseUtils.createFailureResponse(
                        null,
                        new TypeReference<>() {},
                        "Invalid status value. Use 'Y' for Active and 'N' for Inactive.",
                        400
                );
            }

            existingUnit.setStatus(stat);
            MasStoreUnit updateUnit  = masUnitRepository.save(existingUnit);

            return ResponseUtils.createSuccessResponse(
                    convertToResponse(updateUnit),
                    new TypeReference<>() {}
            );
        } else {
            return ResponseUtils.createNotFoundResponse("Session not found", 404);
        }
    }
}
