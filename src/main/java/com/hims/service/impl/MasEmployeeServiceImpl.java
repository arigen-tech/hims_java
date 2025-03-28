package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.helperUtil.HelperUtils;
import com.hims.request.EmployeeDocumentReq;
import com.hims.request.EmployeeQualificationReq;
import com.hims.request.MasEmployeeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.EmployeeDocumentDTO;
import com.hims.response.EmployeeQualificationDTO;
import com.hims.response.MasEmployeeDTO;
import com.hims.service.MasEmployeeService;
import com.hims.utils.ResponseUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MasEmployeeServiceImpl implements MasEmployeeService {

    private static final Logger log = LoggerFactory.getLogger(MasEmployeeServiceImpl.class);

    private static final String[] ALLOWED_IMAGE_EXTENSIONS = {"jpg", "jpeg", "png"};
    private static final String[] ALLOWED_DOCUMENT_EXTENSIONS = {"jpg", "jpeg", "png", "pdf"};

    private static final Set<String> ALLOWED_DOC_EXTENSIONS = new HashSet<>(
            Arrays.asList("pdf", "jpg", "jpeg", "png"));

    // Set of allowed image file extensions
    private static final Set<String> ALLOWED_PIC_EXTENSIONS = new HashSet<>(
            Arrays.asList("jpg", "jpeg", "png"));

    @Value("${upload.image.path}")
    private String uploadDir;

    @Autowired
    private EmployeeQualificationRepository employeeQualificationRepository;

    @Autowired
    private MasEmployeeRepository masEmployeeRepository;

    @Autowired
    private UserDepartmentRepository userDepartmentRepository;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private MasCountryRepository masCountryRepository;

    @Autowired
    private MasStateRepository masStateRepository;

    @Autowired
    private MasGenderRepository masGenderRepository;

    @Autowired
    private MasDepartmentRepository masDepartmentRepository;

    @Autowired
    private MasDistrictRepository masDistrictRepository;

    @Autowired
    private MasIdentificationTypeRepository masIdentificationTypeRepository;

    @Autowired
    private EmployeeDocumentRepository employeeDocumentRepository;

    @Autowired
    private MasUserTypeRepository masUserTypeRepository;

    @Autowired
    private HelperUtils helperUtils;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Override
    public ApiResponse<List<MasEmployeeDTO>> getAllEmployees() {
        List<MasEmployee> employees = masEmployeeRepository.findAll();

        if (employees.isEmpty()) {
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    "RECORD NOT FOUND",
                    400
            );
        }

        List<MasEmployeeDTO> employeeDTOs = employees.stream().map(employee -> {
            List<EmployeeQualificationDTO> qualifications = employeeQualificationRepository
                    .findByEmployee(employee)
                    .stream()
                    .map(EmployeeQualificationDTO::fromEntity)
                    .toList();

            List<EmployeeDocumentDTO> documents = employeeDocumentRepository
                    .findByEmployee(employee)
                    .stream()
                    .map(EmployeeDocumentDTO::fromEntity)
                    .toList();

            return MasEmployeeDTO.fromEntity(employee, qualifications, documents);
        }).toList();

        return ResponseUtils.createSuccessResponse(employeeDTOs, new TypeReference<>() {});
    }

    @Override
    public ApiResponse<MasEmployee> getEmployeeById(Long id) {
        log.debug("Fetching Employee with ID: {}", id);
        if (id == null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Employee ID cannot be null", 400);
        }
        Optional<MasEmployee> emp = masEmployeeRepository.findById(id);
        if (emp.isPresent()) {
            log.debug("Employee found: {}", emp.get());
            return ResponseUtils.createSuccessResponse(emp.get(), new TypeReference<>() {});
        } else {
            log.warn("Employee with ID: {} not found.", id);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Employee not found", 404);
        }
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public ApiResponse<MasEmployee> updateEmployee(Long id, MasEmployeeRequest masEmployeeRequest) {
        log.debug("Updating Employee with ID: {}", id);
        try {
            if (id == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "ID cannot be null.", 400);
            }

            MasEmployee existingEmployee = masEmployeeRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));

            User obj = userRepo.findByUserName(SecurityContextHolder.getContext().getAuthentication().getName());
            if (obj == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "CURRENT USER NOT FOUND", 400);
            }

            if (masEmployeeRequest.getFirstName() != null && !masEmployeeRequest.getFirstName().isEmpty()) {
                existingEmployee.setFirstName(masEmployeeRequest.getFirstName());
            }

            if (masEmployeeRequest.getMiddleName() != null) {
                existingEmployee.setMiddleName(masEmployeeRequest.getMiddleName());
            }

            if (masEmployeeRequest.getLastName() != null) {
                existingEmployee.setLastName(masEmployeeRequest.getLastName());
            }

            if (masEmployeeRequest.getGenderId() != null) {
                MasGender genderObj = masGenderRepository.findById(masEmployeeRequest.getGenderId().longValue())
                        .orElseThrow(() -> new IllegalArgumentException("Gender not found with ID: " + masEmployeeRequest.getGenderId()));
                existingEmployee.setGenderId(genderObj);
            }

            if (masEmployeeRequest.getDob() != null) {
                existingEmployee.setDob(masEmployeeRequest.getDob());
            }

            if (masEmployeeRequest.getAddress1() != null && !masEmployeeRequest.getAddress1().isEmpty()) {
                existingEmployee.setAddress1(masEmployeeRequest.getAddress1());
            }

            if (masEmployeeRequest.getCity() != null && !masEmployeeRequest.getCity().isEmpty()) {
                existingEmployee.setCity(masEmployeeRequest.getCity());
            }

            if (masEmployeeRequest.getMobileNo() != null && !masEmployeeRequest.getMobileNo().isEmpty()) {
                existingEmployee.setMobileNo(masEmployeeRequest.getMobileNo());
            }

            if (masEmployeeRequest.getRegistrationNo() != null && !masEmployeeRequest.getRegistrationNo().isEmpty()) {
                existingEmployee.setRegistrationNo(masEmployeeRequest.getRegistrationNo());
            }

            if (masEmployeeRequest.getFromDate() != null) {
                existingEmployee.setFromDate(masEmployeeRequest.getFromDate());
            }

            if (masEmployeeRequest.getCountryId() != null) {
                MasCountry countryObj = masCountryRepository.findById(masEmployeeRequest.getCountryId().longValue())
                        .orElseThrow(() -> new IllegalArgumentException("Country not found with ID: " + masEmployeeRequest.getCountryId()));
                existingEmployee.setCountryId(countryObj);
            }

            if (masEmployeeRequest.getStateId() != null) {
                MasState stateObj = masStateRepository.findById(masEmployeeRequest.getStateId().longValue())
                        .orElseThrow(() -> new IllegalArgumentException("State not found with ID: " + masEmployeeRequest.getStateId()));
                existingEmployee.setStateId(stateObj);
            }

            if (masEmployeeRequest.getDistrictId() != null) {
                MasDistrict districtObj = masDistrictRepository.findById(masEmployeeRequest.getDistrictId().longValue())
                        .orElseThrow(() -> new IllegalArgumentException("District not found with ID: " + masEmployeeRequest.getDistrictId()));
                existingEmployee.setDistrictId(districtObj);
            }

            if (masEmployeeRequest.getIdentificationType() != null) {
                MasIdentificationType idTypeObj = masIdentificationTypeRepository.findById(masEmployeeRequest.getIdentificationType().longValue())
                        .orElseThrow(() -> new IllegalArgumentException("ID Type not found with ID: " + masEmployeeRequest.getIdentificationType()));
                existingEmployee.setIdentificationType(idTypeObj);
            }

            if (masEmployeeRequest.getPincode() != null && !masEmployeeRequest.getPincode().isEmpty()) {
                existingEmployee.setPincode(masEmployeeRequest.getPincode());
            }

            String fileUploadDir = uploadDir + "MAS_EMPLOYEE/";
            File directory = new File(fileUploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            if (masEmployeeRequest.getIdDocumentName() != null && !masEmployeeRequest.getIdDocumentName().isEmpty()) {
                try {
                    String documentExtension = getFileExtension(masEmployeeRequest.getIdDocumentName().getOriginalFilename());
                    if (!isValidDocExtension(documentExtension)) {
                        return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                                "Document Invalid file type. Only PDF, JPG, JPEG and PNG are allowed.", 400);
                    }

                    String timestamp = String.valueOf(System.currentTimeMillis());
                    String newFilename = timestamp + "_" + masEmployeeRequest.getIdDocumentName().getOriginalFilename();

                    String documentPath = Paths.get(fileUploadDir, newFilename)
                            .toString()
                            .replace("\\", "/");
                    Files.write(Paths.get(documentPath), masEmployeeRequest.getIdDocumentName().getBytes());

                    existingEmployee.setIdDocumentName(documentPath);
                } catch (IOException e) {
                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Failed to upload document.", 400);
                }
            }

            if (masEmployeeRequest.getProfilePicName() != null && !masEmployeeRequest.getProfilePicName().isEmpty()) {
                try {
                    String profileImageExtension = getFileExtension(masEmployeeRequest.getProfilePicName().getOriginalFilename());
                    if (!isValidPicExtension(profileImageExtension)) {
                        return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                                "Thumb Image Invalid file type. Only JPG, JPEG and PNG are allowed.", 400);
                    }

                    String timestamp = String.valueOf(System.currentTimeMillis());
                    String newFilename = timestamp + "_" + masEmployeeRequest.getProfilePicName().getOriginalFilename();

                    String profileImagePath = Paths.get(fileUploadDir, newFilename)
                            .toString()
                            .replace("\\", "/");
                    Files.write(Paths.get(profileImagePath), masEmployeeRequest.getProfilePicName().getBytes());

                    existingEmployee.setProfilePicName(profileImagePath);
                } catch (IOException e) {
                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Failed to upload profile image.", 400);
                }
            }

            existingEmployee.setLastChangedDate(OffsetDateTime.now().toInstant());
            existingEmployee.setLastChangedBy(obj.getUserId().toString());

            MasEmployee savedEmp = masEmployeeRepository.save(existingEmployee);

            if (masEmployeeRequest.getQualification() != null && !masEmployeeRequest.getQualification().isEmpty()) {
                for (EmployeeQualificationReq objQualification : masEmployeeRequest.getQualification()) {
                    if (objQualification.getEmployeeQualificationId() != null) {
                        EmployeeQualification existingQualification = employeeQualificationRepository.findById(objQualification.getEmployeeQualificationId())
                                .orElseThrow(() -> new RuntimeException("Employee Qualification not found with id: " + objQualification.getEmployeeQualificationId()));

                        if (objQualification.getQualificationName() != null) {
                            existingQualification.setQualificationName(objQualification.getQualificationName());
                        }

                        if (objQualification.getCompletionYear() != null) {
                            existingQualification.setCompletionYear(objQualification.getCompletionYear());
                        }

                        if (objQualification.getInstitutionName() != null) {
                            existingQualification.setInstitutionName(objQualification.getInstitutionName());
                        }

                        if (objQualification.getFilePath() != null && !objQualification.getFilePath().isEmpty()) {
                            try {
                                String originalFilename = objQualification.getFilePath().getOriginalFilename();
                                if (originalFilename == null || originalFilename.isBlank()) {
                                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid file name.", 400);
                                }

                                String imageExtension = getFileExtension(originalFilename);
                                if (!isValidDocExtension(imageExtension)) {
                                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                                            "Invalid file type. Only PDF, JPG, JPEG and PNG are allowed.", 400);
                                }

                                String timestamp = String.valueOf(System.currentTimeMillis());
                                String newFilename = timestamp + "_" + originalFilename;

                                String imagePath = Paths.get(fileUploadDir, newFilename)
                                        .toString()
                                        .replace("\\", "/");

                                Files.write(Paths.get(imagePath), objQualification.getFilePath().getBytes());
                                existingQualification.setFilePath(imagePath);
                            } catch (IOException e) {
                                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Failed to upload qualification document.", 400);
                            }
                        }

                        existingQualification.setLastChangedDate(OffsetDateTime.now().toLocalDateTime());
                        existingQualification.setLastChangedBy(obj.getUserId().toString());

                        employeeQualificationRepository.save(existingQualification);
                    } else {
                        String imagePath = "";
                        try {
                            if (objQualification.getFilePath() != null && !objQualification.getFilePath().isEmpty()) {
                                String originalFilename = objQualification.getFilePath().getOriginalFilename();
                                if (originalFilename == null || originalFilename.isBlank()) {
                                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid file name.", 400);
                                }

                                String imageExtension = getFileExtension(originalFilename);
                                if (!isValidDocExtension(imageExtension)) {
                                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                                            "Invalid file type. Only PDF, JPG, JPEG and PNG are allowed.", 400);
                                }

                                Path uploadDir = Paths.get(fileUploadDir);
                                if (!Files.exists(uploadDir)) {
                                    Files.createDirectories(uploadDir);
                                }

                                String timestamp = String.valueOf(System.currentTimeMillis());
                                String newFilename = timestamp + "_" + originalFilename;

                                imagePath = Paths.get(fileUploadDir, newFilename)
                                        .toString()
                                        .replace("\\", "/");

                                Files.write(Paths.get(imagePath), objQualification.getFilePath().getBytes());
                            } else {
                                log.warn("Qualification document file is missing or empty.");
                                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Qualification document cannot be empty.", 400);
                            }
                        } catch (IOException e) {
                            log.error("Error while uploading qualification document: {}", e.getMessage());
                            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Failed to upload qualification document.", 400);
                        }

                        EmployeeQualification qualificationObj = new EmployeeQualification();
                        qualificationObj.setEmployee(savedEmp);
                        qualificationObj.setCompletionYear(objQualification.getCompletionYear());
                        qualificationObj.setQualificationName(objQualification.getQualificationName());
                        qualificationObj.setFilePath(imagePath);
                        qualificationObj.setLastChangedDate(OffsetDateTime.now().toLocalDateTime());
                        qualificationObj.setLastChangedBy(obj.getUserId().toString());
                        qualificationObj.setInstitutionName(objQualification.getInstitutionName());

                        employeeQualificationRepository.save(qualificationObj);
                    }
                }
            }

            if (masEmployeeRequest.getDocument() != null && !masEmployeeRequest.getDocument().isEmpty()) {
                for (EmployeeDocumentReq objDocument : masEmployeeRequest.getDocument()) {
                    if (objDocument.getEmployeeDocumentId() != null) {
                        EmployeeDocument existingDocument = employeeDocumentRepository.findById(objDocument.getEmployeeDocumentId())
                                .orElseThrow(() -> new RuntimeException("Employee Document not found with id: " + objDocument.getEmployeeDocumentId()));

                        if (objDocument.getDocumentName() != null) {
                            existingDocument.setDocumentName(objDocument.getDocumentName());
                        }

                        if (objDocument.getFilePath() != null && !objDocument.getFilePath().isEmpty()) {
                            try {
                                String imageExtension = getFileExtension(objDocument.getFilePath().getOriginalFilename());
                                if (!isValidDocExtension(imageExtension)) {
                                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                                            "Document Invalid file type. Only PDF, JPG, JPEG and PNG are allowed.", 400);
                                }

                                String timestamp = String.valueOf(System.currentTimeMillis());
                                String newFilename = timestamp + "_" + objDocument.getFilePath().getOriginalFilename();

                                String imagePath = Paths.get(fileUploadDir, newFilename)
                                        .toString()
                                        .replace("\\", "/");
                                Files.write(Paths.get(imagePath), objDocument.getFilePath().getBytes());

                                existingDocument.setFilePath(imagePath);
                            } catch (IOException e) {
                                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Failed to upload document.", 400);
                            }
                        }

                        existingDocument.setLastChangedDate(OffsetDateTime.now().toLocalDateTime());
                        existingDocument.setLastChangedBy(obj.getUserId().toString());

                        employeeDocumentRepository.save(existingDocument);
                    } else {
                        String imagePath = "";
                        try {
                            if (objDocument.getFilePath() != null && !objDocument.getFilePath().isEmpty()) {
                                String imageExtension = getFileExtension(objDocument.getFilePath().getOriginalFilename());
                                if (!isValidDocExtension(imageExtension)) {
                                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                                            "Document Invalid file type. Only PDF, JPG, JPEG and PNG are allowed.", 400);
                                }

                                String timestamp = String.valueOf(System.currentTimeMillis());
                                String newFilename = timestamp + "_" + objDocument.getFilePath().getOriginalFilename();

                                imagePath = Paths.get(fileUploadDir, newFilename)
                                        .toString()
                                        .replace("\\", "/");
                                Files.write(Paths.get(imagePath), objDocument.getFilePath().getBytes());
                            } else {
                                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Document file cannot be empty.", 400);
                            }
                        } catch (IOException e) {
                            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Failed to upload document.", 400);
                        }

                        EmployeeDocument documentObj = new EmployeeDocument();
                        documentObj.setEmployee(savedEmp);
                        documentObj.setDocumentName(objDocument.getDocumentName());
                        documentObj.setFilePath(imagePath);
                        documentObj.setLastChangedDate(OffsetDateTime.now().toLocalDateTime());
                        documentObj.setLastChangedBy(obj.getUserId().toString());

                        employeeDocumentRepository.save(documentObj);
                    }
                }
            }

            return ResponseUtils.createSuccessResponse(savedEmp, new TypeReference<>() {});

        } catch (ConstraintViolationException e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<MasEmployee>() {},
                    "Validation failed for required fields: " + e.getMessage(), HttpStatus.BAD_REQUEST.value());
        }
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public ApiResponse<MasEmployee> createEmployee(MasEmployeeRequest masEmployeeRequest) {
        log.debug("Creating new Employee: {}", masEmployeeRequest);
        try {
            if (masEmployeeRequest == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Employee object cannot be null", 400);
            }

            if (masEmployeeRequest.getFirstName() == null || masEmployeeRequest.getFirstName().isEmpty()) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Employee NAME CAN NOT BE BLANK", 400);
            }

            if (masEmployeeRequest.getGenderId() == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Employee GENDER ID CAN NOT BE BLANK", 400);
            }

            if (masEmployeeRequest.getAddress1() == null || masEmployeeRequest.getAddress1().isEmpty()) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "ADDRESS1 CAN NOT BE BLANK", 400);
            }

            if (masEmployeeRequest.getCountryId() == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "COUNTRY ID CAN NOT BE BLANK", 400);
            }

            if (masEmployeeRequest.getStateId() == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "STATE ID CAN NOT BE BLANK", 400);
            }

            if (masEmployeeRequest.getCity() == null || masEmployeeRequest.getCity().isEmpty()) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "CITY CAN NOT BE BLANK", 400);
            }

            if (masEmployeeRequest.getPincode() == null || masEmployeeRequest.getPincode().isEmpty()) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "POSTAL CODE CAN NOT BE BLANK", 400);
            }

            if (masEmployeeRequest.getMobileNo() == null || masEmployeeRequest.getMobileNo().isEmpty()) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "PHONE NO CAN NOT BE BLANK", 400);
            }

            if (masEmployeeRequest.getRegistrationNo() == null || masEmployeeRequest.getRegistrationNo().isEmpty()) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "ID NUMBER CAN NOT BE BLANK", 400);
            }

            if (masEmployeeRequest.getDob() == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "DATE OF BIRTH CAN NOT BE BLANK", 400);
            }

            if (masEmployeeRequest.getDistrictId() == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "DISTRICT ID CAN NOT BE BLANK", 400);
            }

            if (masEmployeeRequest.getIdDocumentName() == null || masEmployeeRequest.getIdDocumentName().isEmpty()) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "ID DOCUMENT CAN NOT BE BLANK", 400);
            }

            if (masEmployeeRequest.getProfilePicName() == null || masEmployeeRequest.getProfilePicName().isEmpty()) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "PROFILE IMAGE CAN NOT BE BLANK", 400);
            }

            if (masEmployeeRequest.getFromDate() == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "FROM DATE CAN NOT BE BLANK", 400);
            }

            if (masEmployeeRequest.getQualification() == null || masEmployeeRequest.getQualification().isEmpty()) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "QUALIFICATION CAN NOT BE BLANK", 400);
            }

            if (masEmployeeRequest.getDocument() == null || masEmployeeRequest.getDocument().isEmpty()) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "DOCUMENT CAN NOT BE BLANK", 400);
            }

            User currentUser = userRepo.findByUserName(SecurityContextHolder.getContext().getAuthentication().getName());
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "CURRENT USER NOT FOUND", 400);
            }

            MasCountry countryObj = masCountryRepository.findById(masEmployeeRequest.getCountryId())
                    .orElseThrow(() -> new IllegalArgumentException("Country not found with ID: " + masEmployeeRequest.getCountryId()));

            MasState stateObj = masStateRepository.findById(masEmployeeRequest.getStateId())
                    .orElseThrow(() -> new IllegalArgumentException("State not found with ID: " + masEmployeeRequest.getStateId()));

            MasDistrict districtObj = masDistrictRepository.findById(masEmployeeRequest.getDistrictId())
                    .orElseThrow(() -> new IllegalArgumentException("District not found with ID: " + masEmployeeRequest.getDistrictId()));

            MasGender genderObj = masGenderRepository.findById(masEmployeeRequest.getGenderId())
                    .orElseThrow(() -> new IllegalArgumentException("Gender not found with ID: " + masEmployeeRequest.getGenderId()));

            MasIdentificationType idTypeObj = masIdentificationTypeRepository.findById(masEmployeeRequest.getIdentificationType())
                    .orElseThrow(() -> new IllegalArgumentException("ID Type not found with ID: " + masEmployeeRequest.getIdentificationType()));

            // Create directory for file uploads if it doesn't exist
            String fileUploadDir = this.uploadDir + "MAS_EMPLOYEE/";
            File directory = new File(fileUploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String profileImagePath = "";
            String documentPath = "";

            if (masEmployeeRequest.getIdDocumentName() != null && !masEmployeeRequest.getIdDocumentName().isEmpty()) {
                String documentExtension = getFileExtension(masEmployeeRequest.getIdDocumentName().getOriginalFilename());
                if (!isValidDocExtension(documentExtension)) {
                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                            "Document Invalid file type. Only PDF, JPG, JPEG and PNG are allowed.", 400);
                }

                String timestamp = String.valueOf(System.currentTimeMillis());
                String newFilename = timestamp + "_" + masEmployeeRequest.getIdDocumentName().getOriginalFilename();

                documentPath = Paths.get(fileUploadDir, newFilename)
                        .toString()
                        .replace("\\", "/");

                try {
                    Files.write(Paths.get(documentPath), masEmployeeRequest.getIdDocumentName().getBytes());
                } catch (IOException e) {
                    log.error("Failed to save ID document: {}", e.getMessage());
                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                            "Failed to upload ID document: " + e.getMessage(), 400);
                }
            }

            if (masEmployeeRequest.getProfilePicName() != null && !masEmployeeRequest.getProfilePicName().isEmpty()) {
                String profileImageExtension = getFileExtension(masEmployeeRequest.getProfilePicName().getOriginalFilename());
                if (!isValidPicExtension(profileImageExtension)) {
                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                            "Profile Image Invalid file type. Only JPG, JPEG and PNG are allowed.", 400);
                }

                String timestamp = String.valueOf(System.currentTimeMillis());
                String newFilename = timestamp + "_" + masEmployeeRequest.getProfilePicName().getOriginalFilename();

                profileImagePath = Paths.get(fileUploadDir, newFilename)
                        .toString()
                        .replace("\\", "/");

                try {
                    Files.write(Paths.get(profileImagePath), masEmployeeRequest.getProfilePicName().getBytes());
                } catch (IOException e) {
                    log.error("Failed to save profile picture: {}", e.getMessage());
                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                            "Failed to upload profile picture: " + e.getMessage(), 400);
                }
            }

            MasEmployee employee = new MasEmployee();
            employee.setFirstName(masEmployeeRequest.getFirstName());
            employee.setMiddleName(masEmployeeRequest.getMiddleName());
            employee.setLastName(masEmployeeRequest.getLastName());
            employee.setGenderId(genderObj);
            employee.setDob(masEmployeeRequest.getDob());
            employee.setAddress1(masEmployeeRequest.getAddress1());
            employee.setCity(masEmployeeRequest.getCity());
            employee.setMobileNo(masEmployeeRequest.getMobileNo());
            employee.setRegistrationNo(masEmployeeRequest.getRegistrationNo());
            employee.setFromDate(masEmployeeRequest.getFromDate());
            employee.setCountryId(countryObj);
            employee.setStateId(stateObj);
            employee.setDistrictId(districtObj);
            employee.setPincode(masEmployeeRequest.getPincode());
            employee.setIdentificationType(idTypeObj);
            employee.setProfilePicName(profileImagePath);
            employee.setIdDocumentName(documentPath);
            employee.setLastChangedDate(OffsetDateTime.now().toInstant());
            employee.setLastChangedBy(currentUser.getUserId().toString());
            employee.setStatus("S"); // Set status as Submitted

            MasEmployee savedEmployee = masEmployeeRepository.save(employee);

            for (EmployeeQualificationReq qualificationReq : masEmployeeRequest.getQualification()) {
                String qualificationFilePath = "";
                try {
                    log.info("Processing qualification document...");

                    if (qualificationReq.getFilePath() != null && !qualificationReq.getFilePath().isEmpty()) {
                        String originalFilename = qualificationReq.getFilePath().getOriginalFilename();
                        if (originalFilename == null || originalFilename.isBlank()) {
                            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid qualification file name.", 400);
                        }

                        String fileExtension = getFileExtension(originalFilename);
                        if (!isValidDocExtension(fileExtension)) {
                            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                                    "Invalid qualification file type. Only PDF, JPG, JPEG and PNG are allowed.", 400);
                        }

                        Path uploadPath = Paths.get(fileUploadDir);
                        if (!Files.exists(uploadPath)) {
                            Files.createDirectories(uploadPath);
                        }

                        String timestamp = String.valueOf(System.currentTimeMillis());
                        String newFilename = timestamp + "_" + originalFilename;

                        qualificationFilePath = Paths.get(fileUploadDir, newFilename)
                                .toString()
                                .replace("\\", "/");

                        Files.write(Paths.get(qualificationFilePath), qualificationReq.getFilePath().getBytes());
                        log.info("Qualification file uploaded successfully to: {}", qualificationFilePath);
                    } else {
                        log.warn("Qualification file is missing or empty.");
                        return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Qualification cannot be empty.", 400);
                    }
                } catch (IOException e) {
                    log.error("Error while uploading image: {}", e.getMessage());
                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Failed to upload image.", 400);
                }

                EmployeeQualification imageObj = new EmployeeQualification();
                imageObj.setEmployee(savedEmployee);
                imageObj.setCompletionYear(qualificationReq.getCompletionYear());
                imageObj.setQualificationName(qualificationReq.getQualificationName());
                imageObj.setFilePath(qualificationFilePath);
                imageObj.setLastChangedDate(OffsetDateTime.now().toLocalDateTime());
                imageObj.setLastChangedBy(currentUser.getUserId().toString());
                imageObj.setInstitutionName(qualificationReq.getInstitutionName());

                employeeQualificationRepository.save(imageObj);
            }

            for (EmployeeDocumentReq objContent : masEmployeeRequest.getDocument()) {
                String imagePaths = "";
                try {
                    log.debug("Received request to upload image in png or jpg....");

                    if (objContent.getFilePath() != null && !objContent.getFilePath().isEmpty()) {
                        String imageExtension = getFileExtension(objContent.getFilePath().getOriginalFilename());
                        if (!isValidDocExtension(imageExtension)) {
                            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                                    "Employee Document Invalid file type. Only PDF, JPG, JPEG and PNG are allowed.", 400);
                        }

                        String timestamp = String.valueOf(System.currentTimeMillis());
                        String newFilename = timestamp + "_" + objContent.getFilePath().getOriginalFilename();

                        imagePaths = Paths.get(fileUploadDir, newFilename)
                                .toString()
                                .replace("\\", "/");
                        Files.write(Paths.get(imagePaths), objContent.getFilePath().getBytes());
                    }
                } catch (IOException e) {
                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Failed to upload image.", 400);
                }

                EmployeeDocument contentObj = new EmployeeDocument();
                contentObj.setEmployee(savedEmployee);
                contentObj.setDocumentName(objContent.getDocumentName());
                contentObj.setFilePath(imagePaths);
                contentObj.setLastChangedDate(OffsetDateTime.now().toLocalDateTime());
                contentObj.setLastChangedBy(currentUser.getUserId().toString());
                employeeDocumentRepository.save(contentObj);
            }
            return ResponseUtils.createSuccessResponse(savedEmployee, new TypeReference<>() {});

        } catch (ConstraintViolationException e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<MasEmployee>() {}, "Validation failed for required fields: " + e.getMessage(), HttpStatus.BAD_REQUEST.value());
        }
    }


    @Transactional(rollbackFor = {Exception.class})
    @Override
    public ApiResponse<MasEmployee> updateEmployeeApprovalStatus(Long empId, Long deptId) {
        if (empId == null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Employee ID CAN NOT BE BLANK", 400);
        }
        if (deptId == null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Department ID CAN NOT BE BLANK", 400);
        }

        User currentUser = userRepo.findByUserName(SecurityContextHolder.getContext().getAuthentication().getName());
        if (currentUser == null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "CURRENT USER NOT FOUND", 400);
        }

        MasEmployee employeeObj = masEmployeeRepository.findById(empId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + empId));

        employeeObj.setStatus("A");
        employeeObj.setLastChangedDate(Instant.now());
        employeeObj.setLastChangedBy(currentUser.getUserId().toString());
        employeeObj.setApprovedBy(currentUser.getUserId().toString());
        employeeObj.setApprovedDate(LocalDateTime.now());

        masEmployeeRepository.save(employeeObj);

        Optional<User> existingUser = userRepo.findByEmployee(employeeObj);

        if (existingUser.isEmpty()) {
            MasUserType userTypeObj = masUserTypeRepository.findById(1L)
                    .orElseThrow(() -> new EntityNotFoundException("Usertype not found with ID: 1"));


            String otp = HelperUtils.generateOTP();
            System.out.println("Generated OTP: " + otp);
            if(otp.isEmpty()){
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Unable to Generate OTP", 400);
            }else{
                String respSms = HelperUtils.sendSMS(employeeObj.getMobileNo(),employeeObj.getFirstName(),otp);
                if(respSms.isEmpty()){
                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Unable to Send OTP", 400);
                }
            }

            String username = employeeObj.getMobileNo();
            if (username == null || username.isEmpty()) {
                username = employeeObj.getMobileNo();
            }


            User newUser = User.builder()
                    .status("y")
                    .userName(username)
                    .mobileNo(employeeObj.getMobileNo())
                    .firstName(employeeObj.getFirstName())
                    .lastName(employeeObj.getLastName())
                    .middleName(employeeObj.getMiddleName())
                    .email(employeeObj.getEmail())
                    .createdAt(Instant.now())
                    .createdBy(currentUser.getUserId().toString())
                    .employee(employeeObj)
                    .userFlag(1)
                    .hospital(currentUser.getHospital())
                    .userType(userTypeObj)
                    .oldPassword(passwordEncoder.encode(otp))
                    .currentPassword(passwordEncoder.encode(otp))
                    .isVerified(true)
                    .lastChangeDate(Instant.now())
                    .build();

            User createNewUser = userRepo.save(newUser);

            MasDepartment deptObj = masDepartmentRepository.findById(deptId)
                    .orElseThrow(() -> new EntityNotFoundException("Department not found with ID: " + deptId));

            UserDepartment newUserdept = new UserDepartment();
            newUserdept.setUser(createNewUser);
            newUserdept.setDepartment(deptObj);
            userDepartmentRepository.save(newUserdept);




        }

        return ResponseUtils.createSuccessResponse(employeeObj, new TypeReference<MasEmployee>() {});
    }




    @Transactional(rollbackFor = {Exception.class})
    @Override
    public ApiResponse<MasEmployee> createAndApproveEmployee(MasEmployeeRequest masEmployeeRequest) {
        if (masEmployeeRequest.getDepartmentId() == null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Department ID CAN NOT BE BLANK", 400);
        }

        ApiResponse<MasEmployee> createResponse = createEmployee(masEmployeeRequest);

        if (createResponse.getStatus() != HttpStatus.OK.value()) {
            return createResponse;
        }

        MasEmployee createdEmployee = createResponse.getResponse();

        return updateEmployeeApprovalStatus(createdEmployee.getEmployeeId(), masEmployeeRequest.getDepartmentId());
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    private boolean isValidDocExtension(String fileExtension) {
        return ALLOWED_DOC_EXTENSIONS.contains(fileExtension);
    }

    private boolean isValidPicExtension(String fileExtension) {
        return ALLOWED_PIC_EXTENSIONS.contains(fileExtension);
    }
}