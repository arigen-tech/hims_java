package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.projection.IPDPatientWaitingListProjection;
import com.hims.request.IpdPatientRequest;
import com.hims.request.PatientRequest;
import com.hims.response.ApiResponse;
import com.hims.response.IPDPatientWaitingListResponse;
import com.hims.service.IPDPatientService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class IPDPatientServiceImpl implements IPDPatientService {

    private final AuthUtil authUtil;
    private final OpdPatientDetailRepository opdPatientDetailRepository;
    private final PatientRepository patientRepository;
    private final VisitRepository visitRepository;
    private final InpatientRepository inpatientRepository;
    private final MasAdmissionTypeRepository masAdmissionTypeRepository;
    private final MasAdmissionCategoryRepository masAdmissionCategoryRepository;
    private final MasAdmissionSourceRepository masAdmissionSourceRepository;
    private final MasPatientConditionRepository masPatientConditionRepository;
    private final MasCareLevelRepo masCareLevelRepository;
    private final MasWardCategoryRepository masWardCategoryRepository;
    private final MasIcdRepository masIcdRepository;
    private final MasDietPreferenceRepository masDietPreferenceRepository;
    private final MasAdmissionStatusRepository masAdmissionStatusRepository;
    private final MasRelationRepository masRelationRepository;
    private final IpNokDetailsRepository ipNokDetailsRepository;
    private final MasWardRepository masWardRepository;
    private final MasRoomRepo masRoomRepository;
    private final MasBedRepository masBedRepository;
    private final IpBedAllocationRepository ipBedAllocationRepository;

    private final IpDocumentRepository ipDocumentRepository;
    @Autowired
    MasGenderRepository masGenderRepository;

    @Autowired
    MasMaritalStatusRepository masMaritalStatusRepository;
    @Autowired
    MasReligionRepository masReligionRepository;
    @Autowired
    MasDistrictRepository masDistrictRepository;
    @Autowired
    MasStateRepository masStateRepository;
    @Autowired
    MasCountryRepository masCountryRepository;
    @Autowired
    MasHospitalRepository masHospitalRepository;
    @Autowired
    MasDepartmentRepository masDepartmentRepository;

    @Override
    public ApiResponse<Page<IPDPatientWaitingListResponse>> ipdPatientWaitingList(
            int page,
            int size,
            Long hospitalId,
            String patientName,
            String mobileNo
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size);

            patientName = patientName != null && !patientName.trim().isEmpty() ? patientName.trim() : null;

            mobileNo = mobileNo != null && !mobileNo.trim().isEmpty() ? mobileNo.trim() : null;

            Page<IPDPatientWaitingListProjection> waitingListPage =
                    opdPatientDetailRepository.getIPDPatientWaitingList(AppConstants.IPD_ADMISSION_FLAG.toLowerCase(), hospitalId, patientName,
                            mobileNo,
                            pageable);

            Page<IPDPatientWaitingListResponse> responsePage = waitingListPage.map(this::mapToIPDPatientWaitingListResponse);

            return ResponseUtils.createSuccessResponse(responsePage, new TypeReference<Page<IPDPatientWaitingListResponse>>() {}
            );

        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<Page<IPDPatientWaitingListResponse>>() {}, e.getMessage(),
                    500
            );
        }
    }


    @Override
    @Transactional
    public ApiResponse<String> saveIpdPatientDetails(IpdPatientRequest request) {

        try {
            log.info("Saving IPD patient details started for patientId: {}", request.getPatientId());

            Patient patient = patientRepository.findById(request.getPatientId())
                    .orElseThrow(() -> new RuntimeException("Patient not found with id: " + request.getPatientId()));

            Visit visit = visitRepository.findById(request.getVisitId())
                    .orElseThrow(() -> new RuntimeException("Visit not found with id: " + request.getVisitId()));

            Inpatient inpatient = saveInpatientDetails(request, patient, visit);

            saveNokDetails(request, inpatient, patient);

            saveBedAllocationDetails(request, inpatient, patient);

            saveIpDocumentDetails(request, inpatient, patient);

            log.info("Saving IPD patient details completed for patientId: {}, inpatientId: {}", patient.getId(), inpatient.getInpatientId());

            return ResponseUtils.createSuccessResponse("IPD patient details saved successfully", new TypeReference<>() {});

        } catch (Exception e) {
            log.error("Error while saving IPD patient details for patientId: {}. Error: {}", request != null ? request.getPatientId() : null, e.getMessage(),
                    e);
            throw new RuntimeException("Error while saving IPD patient details: " + e.getMessage(), e);
        }
    }

    @Transactional
    private Inpatient saveInpatientDetails(IpdPatientRequest request, Patient patient, Visit visit) {
        User user = authUtil.getCurrentUser();

        Inpatient inpatient = new Inpatient();

        inpatient.setPatient(patient);
        inpatient.setVisit(visit);
        inpatient.setAdmissionDate(request.getAdmissionDate());
        inpatient.setAdmissionTime(request.getAdmissionTime());

        if (request.getAdmissionTypeId() != null) {
            inpatient.setAdmissionType(masAdmissionTypeRepository.getReferenceById(request.getAdmissionTypeId()));
        }

        if (request.getAdmissionCategoryId() != null) {
            inpatient.setAdmissionCategory(masAdmissionCategoryRepository.getReferenceById(request.getAdmissionCategoryId()));
        }

        if (request.getAdmissionSourceId() != null) {
            inpatient.setAdmissionSource(masAdmissionSourceRepository.getReferenceById(request.getAdmissionSourceId()));
        }

        if (request.getPatientConditionId() != null) {
            inpatient.setPatientCondition(masPatientConditionRepository.getReferenceById(request.getPatientConditionId()));
        }

        if (request.getCareLevelId() != null) {
            inpatient.setCareLevel(masCareLevelRepository.getReferenceById(request.getCareLevelId()));
        }

        if (request.getWardCategoryId() != null) {
            inpatient.setWardCategory(masWardCategoryRepository.getReferenceById(request.getWardCategoryId()));
        }

        inpatient.setConditionNotes(request.getConditionNotes());
        inpatient.setLastUpdateDate(LocalDateTime.now());

        if (user != null) {
            inpatient.setCreatedBy(user.getFullName());
            inpatient.setLastUpdatedBy(user.getFullName());
        }

        Inpatient savedInpatient = inpatientRepository.save(inpatient);

        log.info("Inpatient admission details saved successfully. inpatientId: {}", savedInpatient.getInpatientId());
        return savedInpatient;
    }

    @Transactional
    private void saveNokDetails(IpdPatientRequest request, Inpatient inpatient, Patient patient) {
        User user = authUtil.getCurrentUser();

        IpNokDetails nokDetails = new IpNokDetails();

        nokDetails.setInpatient(inpatient);
        nokDetails.setPatient(patient);
        nokDetails.setNokName(request.getNokName());

        if (request.getNokRelationId() != null) {
            nokDetails.setNokRelation(masRelationRepository.getReferenceById(request.getNokRelationId()));
        }
        nokDetails.setContactNo(request.getContactNo());
        nokDetails.setAddressLine(request.getAddressLine());
        nokDetails.setCity(request.getCity());
        nokDetails.setState(request.getState());
        nokDetails.setPincode(request.getPincode());
        nokDetails.setLastUpdateDate(LocalDateTime.now());

        if (user != null) {
            nokDetails.setCreatedBy(user.getFullName());
            nokDetails.setLastUpdatedBy(user.getFullName());
        }
        ipNokDetailsRepository.save(nokDetails);
        log.info("NOK details saved successfully for inpatientId: {}", inpatient.getInpatientId());
    }

    @Transactional
    private void saveBedAllocationDetails(IpdPatientRequest request, Inpatient inpatient, Patient patient) {
        User user = authUtil.getCurrentUser();

        IpBedAllocation bedAllocation = new IpBedAllocation();

        bedAllocation.setInpatient(inpatient);
        bedAllocation.setPatient(patient);

        if (request.getWardId() != null) {
            bedAllocation.setWard(masWardRepository.getReferenceById(request.getWardId()));
        }

        if (request.getRoomId() != null) {
            bedAllocation.setRoom(masRoomRepository.getReferenceById(request.getRoomId()));
        }

        if (request.getBedId() != null) {
            bedAllocation.setBed(masBedRepository.getReferenceById(request.getBedId()));
        }

        bedAllocation.setAllocationStartDate(LocalDateTime.now());
        bedAllocation.setLastUpdateDate(LocalDateTime.now());

        if (user != null) {
            bedAllocation.setCreatedBy(user.getFullName());
            bedAllocation.setLastUpdatedBy(user.getFullName());
        }
        ipBedAllocationRepository.save(bedAllocation);
        log.info("Bed allocation details saved successfully for inpatientId: {}", inpatient.getInpatientId());
    }

    @Transactional
    private void saveIpDocumentDetails(IpdPatientRequest request, Inpatient inpatient, Patient patient) {

        List<IpdPatientRequest.IpDocumentRequest> documents = request.getDocuments();

        if (documents == null || documents.isEmpty()) {
            log.info("No IPD documents uploaded for inpatientId: {}", inpatient.getInpatientId());
            return;
        }



        String uploadDir = "uploads/ipd/documents/";

        try {
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            for (IpdPatientRequest.IpDocumentRequest docReq : documents) {

                if (docReq == null) {
                    continue;
                }

                MultipartFile file = docReq.getIpDocumentUploads();

                if (file == null || file.isEmpty()) {
                    throw new RuntimeException(
                            "File is required for document type: " + docReq.getDocumentType()
                    );
                }

                String originalFileName = file.getOriginalFilename();

                if (originalFileName == null || originalFileName.trim().isEmpty()) {
                    throw new RuntimeException("Invalid file name");
                }

                String safeOriginalFileName = originalFileName.replaceAll("[^a-zA-Z0-9._-]", "_");

                String fileType = getFileExtension(safeOriginalFileName);

                Long fileSizeKb = file.getSize() / 1024;

                String newFileName = UUID.randomUUID() + "_" + safeOriginalFileName;

                Path finalFilePath = uploadPath.resolve(newFileName);

                Files.copy(
                        file.getInputStream(),
                        finalFilePath,
                        StandardCopyOption.REPLACE_EXISTING
                );

                IpDocument document = new IpDocument();

                document.setInpatient(inpatient);
                document.setPatient(patient);
                document.setDocumentDatetime(LocalDateTime.now());
                document.setDocumentType(docReq.getDocumentType());
                document.setFileName(originalFileName);
                document.setFilePath(finalFilePath.toString());
                document.setFileType(fileType);
                document.setFileSizeKb(fileSizeKb);
                document.setLastUpdateDate(LocalDateTime.now());

                ipDocumentRepository.save(document);
            }

            log.info("All IPD documents saved successfully for inpatientId: {}", inpatient.getInpatientId());

        } catch (Exception e) {
            log.error("Error while saving IPD document for inpatientId: {}", inpatient.getInpatientId(), e);
            throw new RuntimeException("Error while saving IPD document: " + e.getMessage(), e);
        }
    }


    private String getFileExtension(String fileName) {

        if (fileName == null || !fileName.contains(".")) {
            return null;
        }

        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }


    private IPDPatientWaitingListResponse mapToIPDPatientWaitingListResponse(
            IPDPatientWaitingListProjection projection) {

        IPDPatientWaitingListResponse response = new IPDPatientWaitingListResponse();

        response.setOpdPatientDetailsId(projection.getOpdPatientDetailsId());
        response.setVisitId(projection.getVisitId());
        response.setPatientId(projection.getPatientId());
        response.setPatientName(projection.getPatientName());
        response.setPatientMobileNo(projection.getPatientMobileNo());
        response.setAge(projection.getAge());
        response.setGender(projection.getGender());
        response.setAdmissionAdviseDate(projection.getAdmissionAdviseDate() != null ? projection.getAdmissionAdviseDate() : null);
        response.setDoctorName(projection.getDoctorName());
        response.setDepartment(projection.getDepartment());
        response.setWardId(projection.getWardId());
        response.setWardName(projection.getWardName());
        response.setCareLevelId(projection.getCareLevelId());
        response.setCareLevel(projection.getCareLevel());
        response.setUhid(projection.getUhid());
        response.setDepartmentId(projection.getDepartmentId());
        response.setAdmissionWardCategoryId(projection.getAdmissionWardCategoryId());
        response.setAdmissionWardCategoryName(projection.getAdmissionWardCategoryName());
        response.setAdmissionSource(null);
        return response;
    }
}
