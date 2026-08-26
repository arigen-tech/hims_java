package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.utils.AuthUtil;
import org.springframework.beans.factory.annotation.Value;
import com.hims.entity.repository.InpatientRepository;
import com.hims.entity.repository.OtBookingRequestDtRepository;
import com.hims.entity.repository.OtBookingRequestHdRepository;
import com.hims.exception.SDDException;
import com.hims.projection.ActiveAdmissionOtProjection;
import com.hims.projection.ActiveAdmissionProjectionResponse;
import com.hims.request.OtBookingRequestDtDto;
import com.hims.entity.OtBookingRequestDt;
import com.hims.entity.OtBookingRequestHd;
import com.hims.request.OtBookingRequestHdDto;
import com.hims.response.ActiveAdmissionOtResponse;
import com.hims.response.ActiveAdmissionResponse;
import com.hims.response.ApiResponse;
import com.hims.service.OtService;
import com.hims.utils.ResponseUtils;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.autoconfigure.observation.ObservationProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtServiceImpl implements OtService {

    private final OtBookingRequestHdRepository hdRepository;
    private final OtBookingRequestDtRepository dtRepository;
    private final InpatientRepository inpatientRepository;
    @Value("${ipd.admission.status.admitted}")
    Long ipdAdmissionStatusAdmitted;

    @Transactional
    @Override
    public ApiResponse<String> createOrUpdateOtBookingHeader(OtBookingRequestHdDto dto) {
        if (dto == null) {
            throw new SDDException(
                    "otBooking",
                    400,
                    "OT Booking request cannot be null"
            );
        }

        if (dto.getPatientId() == null) {
            throw new SDDException(
                    "patient",
                    400,
                    "Patient ID is required"
            );
        }

        if (dto.getVisitId() == null) {
            throw new SDDException(
                    "visit",
                    400,
                    "Visit ID is required"
            );
        }

        // ================= HEADER =================

        OtBookingRequestHd hd;

        if (dto.getOtBookingRequestId() != null) {
            // UPDATE existing booking
            hd = hdRepository.findById(dto.getOtBookingRequestId())
                    .orElseThrow(() ->
                            new SDDException(
                                    "otBooking",
                                    404,
                                    "OT Booking Request not found with ID: "
                                            + dto.getOtBookingRequestId()
                            )
                    );
        } else {
            Optional<OtBookingRequestHd> optionalHd = hdRepository.findByVisitId(dto.getVisitId());
            if (optionalHd.isPresent()) {
                hd = optionalHd.get();
            } else {
                hd = new OtBookingRequestHd();
                hd.setRequestNo(dto.getRequestNo());
            }
        }

        hd.setPatientId(dto.getPatientId());
        hd.setVisitId(dto.getVisitId());
        hd.setDepartmentId(dto.getDepartmentId());
        hd.setPrimarySurgeonId(dto.getPrimarySurgeonId());
        hd.setDiagnosis(dto.getDiagnosis());
        hd.setRequestSource(dto.getRequestSource());
        hd.setPreferredOtId(dto.getPreferredOtId());
        hd.setPreferredDate(dto.getPreferredDate());
        hd.setPreferredStartTime(dto.getPreferredStartTime());
        hd.setPreferredEndTime(dto.getPreferredEndTime());
        hd.setStatus(dto.getStatus());
        hd.setBookingStatusId(dto.getBookingStatusId());
        hd.setPriority(dto.getPriority());
        hd.setRequestedBy(dto.getRequestedBy());
        hd.setRequestedDate(dto.getRequestedDate());
        hd.setLastChgBy(dto.getLastChgBy());
        hd.setLastChgDate(LocalDateTime.now());
        OtBookingRequestHd savedHd = hdRepository.save(hd);

        log.info("OT Booking Header saved successfully. ID: {}",savedHd.getOtBookingRequestId());
        // ================= DETAILS =================
        saveOrUpdateOtDetails(savedHd,dto.getSurgeryDetails());
        log.info("OT Booking Details saved successfully. Header ID: {}", savedHd.getOtBookingRequestId());

        // ================= RESPONSE =================
        return ResponseUtils.createSuccessResponse(
                "OT Booking saved successfully",
                new TypeReference<>() {
                }
        );
    }

    @Override
    public ApiResponse<Page<ActiveAdmissionOtResponse>> activeAdmissionList(int page, int size, String patientName, String mobileNo, String admissionNo, Long wardId) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "inpatientId"));
            Page<ActiveAdmissionOtProjection> projectionPage = inpatientRepository.activeAdmissionList(ipdAdmissionStatusAdmitted,

                    patientName,
                    mobileNo,
                    admissionNo,
                    wardId,
                    pageable
            );
            Page<ActiveAdmissionOtResponse> responsePage = projectionPage.map(this::mapActiveAdmissionOtResponse);

            return ResponseUtils.createSuccessResponse(responsePage, new TypeReference<>() {});

        } catch (Exception e) {
            log.error("Error while fetching active admission list. ");
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Error while fetching active admission list: " + e.getMessage(), 500);
        }
    }


    private void saveOrUpdateOtDetails(OtBookingRequestHd hd, List<OtBookingRequestDtDto> requestDetails) {

        log.info("Saving OT details for Header ID: {}",hd.getOtBookingRequestId());

        // Frontend ne koi details nahi bheji
        if (requestDetails == null) {
            requestDetails = Collections.emptyList();
        }

        // Existing DB details
        List<OtBookingRequestDt> existingDetails = dtRepository.findByOtBookingRequest(hd);

        Map<Long, OtBookingRequestDt> existingMap =
                existingDetails.stream()
                        .filter(Objects::nonNull)
                        .filter(dt -> dt.getOtBookingRequestDtId() != null)
                        .collect(Collectors.toMap(
                                OtBookingRequestDt::getOtBookingRequestDtId,
                                Function.identity()
                        ));

        // Frontend se jo existing IDs aaye hain
        Set<Long> requestDetailIds = new HashSet<>();
        List<OtBookingRequestDt> detailsToSave = new ArrayList<>();
        long sequence = 1;
        for (OtBookingRequestDtDto dto : requestDetails) {
            if (dto == null) {
                continue;
            }
            OtBookingRequestDt dt;
            // ================= UPDATE =================
            if (dto.getOtBookingRequestDtId() != null) {
                dt = existingMap.get(dto.getOtBookingRequestDtId());
                if (dt == null) {
                    throw new SDDException(
                            "otBookingDetail",
                            404,
                            "OT Booking Detail not found with ID: "
                                    + dto.getOtBookingRequestDtId()
                    );
                }

                requestDetailIds.add(dto.getOtBookingRequestDtId());
            }
            // ================= CREATE =================
            else {
                dt = new OtBookingRequestDt();
                dt.setOtBookingRequest(hd);
            }
            // ================= MAP DATA =================
            dt.setOtBookingRequest(hd);
            dt.setSurgeryId(dto.getSurgeryId());
            dt.setSurgeryTypeId(dto.getSurgeryTypeId());
            dt.setSequenceNo(sequence++);
            dt.setExpectedDurationMin(dto.getExpectedDurationMin());
            dt.setStatus(AppConstants.STATUS_N);
            dt.setLastChgBy(dto.getLastChgBy());
            dt.setLastChgDate(LocalDateTime.now());
            detailsToSave.add(dt);
        }
        // ================= SAVE =================
        if (!detailsToSave.isEmpty()) {
            dtRepository.saveAll(detailsToSave);
            log.info(
                    "Saved {} OT booking details for Header ID: {}",
                    detailsToSave.size(),
                    hd.getOtBookingRequestId()
            );
        }

        // ================= DELETE REMOVED DETAILS =================

        List<OtBookingRequestDt> detailsToDelete =
                existingDetails.stream()
                        .filter(existing ->
                                existing.getOtBookingRequestDtId() != null
                                        && !requestDetailIds.contains(
                                        existing.getOtBookingRequestDtId()
                                )
                        )
                        .toList();

        if (!detailsToDelete.isEmpty()) {
            dtRepository.deleteAll(detailsToDelete);
            log.info(
                    "Deleted {} removed OT booking details for Header ID: {}",
                    detailsToDelete.size(),
                    hd.getOtBookingRequestId()
            );
        }
    }
    private ActiveAdmissionOtResponse mapActiveAdmissionOtResponse(
            ActiveAdmissionOtProjection projection) {

        ActiveAdmissionOtResponse response = new ActiveAdmissionOtResponse();

        response.setInpatientId(projection.getInpatientId());
        response.setVisitId(projection.getVisitId());
        response.setPatientId(projection.getPatientId());
        response.setPatientName(projection.getPatientName());
        response.setUhid(projection.getUhid());
        response.setAge(projection.getAge());
        response.setGenderId(projection.getGenderId());
        response.setGender(projection.getGender());
        response.setMobileNo(projection.getMobileNo());
        response.setAdmissionNo(projection.getAdmissionNo());
        response.setWardId(projection.getWardId());
        response.setWard(projection.getWard());
        response.setRooId(projection.getRooId());
        response.setRoom(projection.getRoom());
        response.setBedId(projection.getBedId());
        response.setBed(projection.getBed());
        response.setAdmissionDateTime(projection.getAdmissionDateTime());
        response.setDoctorName(projection.getDoctorName());

        return response;
    }
}