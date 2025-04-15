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
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private MasEmploymentTypeRepository masEmploymentTypeRepository;

    @Autowired
    private EmployeeDocumentRepository employeeDocumentRepository;

    @Autowired
    private MasUserTypeRepository masUserTypeRepository;

    @Autowired
    private MasRoleRepository masRoleRepository;

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
    public ApiResponse<List<MasEmployeeDTO>> getEmployeesByStatus(String status) {
        List<MasEmployee> employees = masEmployeeRepository.findByStatus(status);

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
    public ApiResponse<MasEmployeeDTO> getEmployeeById(Long id) {
        MasEmployee employee = masEmployeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + id));

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

        MasEmployeeDTO employeeDTO = MasEmployeeDTO.fromEntity(employee, qualifications, documents);

        return ResponseUtils.createSuccessResponse(employeeDTO, new TypeReference<>() {});
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

            if (masEmployeeRequest.getEmployeeTypeId() != null) {
                MasUserType empTypeObj = masUserTypeRepository.findById(masEmployeeRequest.getEmployeeTypeId().longValue())
                        .orElseThrow(() -> new IllegalArgumentException("Employee Type not found with ID: " + masEmployeeRequest.getEmployeeTypeId()));
                existingEmployee.setEmployeeTypeId(empTypeObj);
            }

            if (masEmployeeRequest.getEmploymentTypeId() != null) {
                MasEmploymentType employmentTypeObj = masEmploymentTypeRepository.findById(masEmployeeRequest.getEmploymentTypeId().longValue())
                        .orElseThrow(() -> new IllegalArgumentException("Employment Type not found with ID: " + masEmployeeRequest.getEmploymentTypeId()));
                existingEmployee.setEmploymentTypeId(employmentTypeObj);
            }

            if (masEmployeeRequest.getRoleId() != null) {
                MasRole roleObj = masRoleRepository.findById(masEmployeeRequest.getRoleId().longValue())
                        .orElseThrow(() -> new IllegalArgumentException("Role not found with ID: " + masEmployeeRequest.getRoleId()));
                existingEmployee.setRoleId(roleObj);
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
        employeeObj.setLastChangedBy(currentUser.getUsername());
        employeeObj.setApprovedBy(currentUser.getUsername());
        employeeObj.setApprovedDate(LocalDateTime.now());

        masEmployeeRepository.save(employeeObj);

        Optional<User> existingUser = userRepo.findByEmployee(employeeObj);

        if (existingUser.isEmpty()) {
            MasUserType userTypeObj = masUserTypeRepository.findById(1L)
                    .orElseThrow(() -> new EntityNotFoundException("Usertype not found with ID: 1"));


            String otp = helperUtils.generateOTP();
            System.out.println("Generated OTP: " + otp);
            if(otp.isEmpty()){
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Unable to Generate OTP", 400);
            }else{
                String respSms = helperUtils.sendSMS(employeeObj.getMobileNo(),employeeObj.getFirstName(),otp);
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
                    .createdBy(currentUser.getUsername())
                    .employee(employeeObj)
                    .userFlag(1)
                    .hospital(currentUser.getHospital())
                    .userType(userTypeObj)
                    .roleId(employeeObj.getRoleId().getId().toString())
                    .dateOfBirth(employeeObj.getDob())
                    .oldPassword(passwordEncoder.encode(otp))
                    .currentPassword(passwordEncoder.encode(otp))
                    .isVerified(true)
                    .lastChangeDate(Instant.now())
                    .lastChangedBy(currentUser.getUsername())
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

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public ApiResponse<MasEmployee> createEmployee(MasEmployeeRequest masEmployeeRequest) {
        log.debug("Creating new Employee: {}", masEmployeeRequest);
        try {
            Map<String, String> validationErrors = validateEmployeeRequest(masEmployeeRequest);
            if (!validationErrors.isEmpty()) {
                String firstError = validationErrors.values().iterator().next();
                log.warn("Validation failed: {}", validationErrors);
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        firstError, HttpStatus.BAD_REQUEST.value());
            }

            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }

            try {
                MasEmployee employee = buildEmployeeFromRequest(masEmployeeRequest, currentUser);
                MasEmployee savedEmployee = masEmployeeRepository.save(employee);

                try {
                    processQualifications(masEmployeeRequest.getQualification(), savedEmployee, currentUser);
                } catch (QualificationProcessingException e) {
                    log.error("Failed to process qualifications: {}", e.getMessage(), e);
                    throw new EmployeeCreationException("Failed to process qualifications: " + e.getMessage(), e);
                }

                try {
                    processDocuments(masEmployeeRequest.getDocument(), savedEmployee, currentUser);
                } catch (DocumentProcessingException e) {
                    log.error("Failed to process documents: {}", e.getMessage(), e);
                    throw new EmployeeCreationException("Failed to process documents: " + e.getMessage(), e);
                }

                log.info("Successfully created employee with ID: {}", savedEmployee.getEmployeeId());
                return ResponseUtils.createSuccessResponse(savedEmployee, new TypeReference<>() {});

            } catch (EntityNotFoundException e) {
                log.error("Entity not found: {}", e.getMessage());
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        e.getMessage(), HttpStatus.NOT_FOUND.value());
            } catch (FileProcessingException e) {
                log.error("File processing error: {}", e.getMessage(), e);
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        e.getMessage(), HttpStatus.BAD_REQUEST.value());
            } catch (EmployeeCreationException e) {
                log.error("Employee creation error: {}", e.getMessage(), e);
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
        } catch (ConstraintViolationException e) {
            log.error("Constraint violation: {}", e.getMessage(), e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<MasEmployee>() {},
                    "Validation failed for required fields: " + e.getMessage(),
                    HttpStatus.BAD_REQUEST.value());
        } catch (Exception e) {
            log.error("Unexpected error creating employee: {}", e.getMessage(), e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<MasEmployee>() {},
                    "An unexpected error occurred: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    private Map<String, String> validateEmployeeRequest(MasEmployeeRequest request) {
        Map<String, String> errors = new HashMap<>();

        if (request == null) {
            errors.put("request", "Employee object cannot be null");
            return errors;
        }

        validateField(errors, request.getFirstName(), "firstName", "First name cannot be blank");
        validateField(errors, request.getGenderId(), "genderId", "Gender ID cannot be blank");
        validateField(errors, request.getAddress1(), "address1", "Address 1 cannot be blank");
        validateField(errors, request.getCountryId(), "countryId", "Country ID cannot be blank");
        validateField(errors, request.getStateId(), "stateId", "State ID cannot be blank");
        validateField(errors, request.getDistrictId(), "districtId", "District ID cannot be blank");
        validateField(errors, request.getCity(), "city", "City cannot be blank");
        validateField(errors, request.getPincode(), "pincode", "Postal code cannot be blank");
        validateField(errors, request.getMobileNo(), "mobileNo", "Phone number cannot be blank");
        validateField(errors, request.getRegistrationNo(), "registrationNo", "ID number cannot be blank");
        validateField(errors, request.getDob(), "dob", "Date of birth cannot be blank");
        validateField(errors, request.getIdentificationType(), "identificationType", "Identification type cannot be blank");
        validateField(errors, request.getIdentificationType(), "employeeTypeId", "Employee type cannot be blank");
        validateField(errors, request.getEmploymentTypeId(), "employmentTypeId", "Employment type cannot be blank");
        validateField(errors, request.getRoleId(), "roleId", "Role cannot be blank");

        validateField(errors, request.getFromDate(), "fromDate", "From date cannot be blank");

        if (request.getIdDocumentName() == null || request.getIdDocumentName().isEmpty()) {
            errors.put("idDocumentName", "ID document cannot be blank");
        }

        if (request.getProfilePicName() == null || request.getProfilePicName().isEmpty()) {
            errors.put("profilePicName", "Profile image cannot be blank");
        }

        if (request.getQualification() == null || request.getQualification().isEmpty()) {
            errors.put("qualification", "Qualification cannot be blank");
        }

        if (request.getDocument() == null || request.getDocument().isEmpty()) {
            errors.put("document", "Document cannot be blank");
        }

        return errors;
    }


    private <T> void validateField(Map<String, String> errors, T value, String fieldName, String errorMessage) {
        if (value == null) {
            errors.put(fieldName, errorMessage);
        } else if (value instanceof String && ((String) value).isEmpty()) {
            errors.put(fieldName, errorMessage);
        }
    }


    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByUserName(username);
        if (user == null) {
            log.warn("User not found for username: {}", username);
        }
        return user;
    }

    private MasEmployee buildEmployeeFromRequest(MasEmployeeRequest request, User currentUser)
            throws EntityNotFoundException, FileProcessingException {

        MasCountry countryObj = masCountryRepository.findById(request.getCountryId())
                .orElseThrow(() -> new EntityNotFoundException("Country not found with ID: " + request.getCountryId()));

        MasState stateObj = masStateRepository.findById(request.getStateId())
                .orElseThrow(() -> new EntityNotFoundException("State not found with ID: " + request.getStateId()));

        MasDistrict districtObj = masDistrictRepository.findById(request.getDistrictId())
                .orElseThrow(() -> new EntityNotFoundException("District not found with ID: " + request.getDistrictId()));

        MasGender genderObj = masGenderRepository.findById(request.getGenderId())
                .orElseThrow(() -> new EntityNotFoundException("Gender not found with ID: " + request.getGenderId()));

        MasIdentificationType idTypeObj = masIdentificationTypeRepository.findById(request.getIdentificationType())
                .orElseThrow(() -> new EntityNotFoundException("ID Type not found with ID: " + request.getIdentificationType()));

        MasUserType empTypeObj = masUserTypeRepository.findById(request.getEmployeeTypeId())
                .orElseThrow(() -> new EntityNotFoundException("Employee Type not found with ID: " + request.getIdentificationType()));

        MasEmploymentType emptTypeObj = masEmploymentTypeRepository.findById(request.getEmploymentTypeId())
                .orElseThrow(() -> new EntityNotFoundException("Employment Type not found with ID: " + request.getIdentificationType()));

        MasRole roleObj = masRoleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new EntityNotFoundException("Role not found with ID: " + request.getIdentificationType()));


        String fileUploadDir = createUploadDirectory();

        String documentPath = processIdDocument(request.getIdDocumentName(), fileUploadDir);

        String profileImagePath = processProfileImage(request.getProfilePicName(), fileUploadDir);

        MasEmployee employee = new MasEmployee();
        employee.setFirstName(request.getFirstName());
        employee.setMiddleName(request.getMiddleName());
        employee.setLastName(request.getLastName());
        employee.setGenderId(genderObj);
        employee.setDob(request.getDob());
        employee.setAddress1(request.getAddress1());
        employee.setCity(request.getCity());
        employee.setMobileNo(request.getMobileNo());
        employee.setRegistrationNo(request.getRegistrationNo());
        employee.setFromDate(request.getFromDate());
        employee.setCountryId(countryObj);
        employee.setStateId(stateObj);
        employee.setDistrictId(districtObj);
        employee.setPincode(request.getPincode());
        employee.setIdentificationType(idTypeObj);
        employee.setRoleId(roleObj);
        employee.setEmployeeTypeId(empTypeObj);
        employee.setEmploymentTypeId(emptTypeObj);
        employee.setProfilePicName(profileImagePath);
        employee.setIdDocumentName(documentPath);
        employee.setLastChangedDate(OffsetDateTime.now().toInstant());
        employee.setLastChangedBy(currentUser.getUsername());
        employee.setStatus("S");

        return employee;
    }

    private String createUploadDirectory() throws FileProcessingException {
        String fileUploadDir = this.uploadDir + "MAS_EMPLOYEE/";
        File directory = new File(fileUploadDir);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (!created) {
                throw new FileProcessingException("Failed to create directory: " + fileUploadDir);
            }
        }
        return fileUploadDir;
    }


    private String processIdDocument(MultipartFile idDocument, String fileUploadDir) throws FileProcessingException {
        if (idDocument == null || idDocument.isEmpty()) {
            throw new FileProcessingException("ID document is missing or empty");
        }

        String documentExtension = getFileExtension(idDocument.getOriginalFilename());
        if (!isValidDocExtension(documentExtension)) {
            throw new FileProcessingException("Document invalid file type. Only PDF, JPG, JPEG and PNG are allowed.");
        }

        String timestamp = String.valueOf(System.currentTimeMillis());
        String newFilename = timestamp + "_" + idDocument.getOriginalFilename();

        String documentPath = Paths.get(fileUploadDir, newFilename)
                .toString()
                .replace("\\", "/");

        try {
            Files.write(Paths.get(documentPath), idDocument.getBytes());
            return documentPath;
        } catch (IOException e) {
            log.error("Failed to save ID document: {}", e.getMessage(), e);
            throw new FileProcessingException("Failed to upload ID document: " + e.getMessage());
        }
    }


    private String processProfileImage(MultipartFile profileImage, String fileUploadDir) throws FileProcessingException {
        if (profileImage == null || profileImage.isEmpty()) {
            throw new FileProcessingException("Profile image is missing or empty");
        }

        String profileImageExtension = getFileExtension(profileImage.getOriginalFilename());
        if (!isValidPicExtension(profileImageExtension)) {
            throw new FileProcessingException("Profile image invalid file type. Only JPG, JPEG and PNG are allowed.");
        }

        String timestamp = String.valueOf(System.currentTimeMillis());
        String newFilename = timestamp + "_" + profileImage.getOriginalFilename();

        String profileImagePath = Paths.get(fileUploadDir, newFilename)
                .toString()
                .replace("\\", "/");

        try {
            Files.write(Paths.get(profileImagePath), profileImage.getBytes());
            return profileImagePath;
        } catch (IOException e) {
            log.error("Failed to save profile picture: {}", e.getMessage(), e);
            throw new FileProcessingException("Failed to upload profile picture: " + e.getMessage());
        }
    }

    private void processQualifications(List<EmployeeQualificationReq> qualifications, MasEmployee employee, User currentUser)
            throws QualificationProcessingException {

        if (qualifications == null || qualifications.isEmpty()) {
            throw new QualificationProcessingException("Qualifications cannot be empty");
        }

        String fileUploadDir = this.uploadDir + "MAS_EMPLOYEE/";

        for (EmployeeQualificationReq qualificationReq : qualifications) {
            String qualificationFilePath = "";
            try {
                log.info("Processing qualification document...");

                if (qualificationReq.getFilePath() == null || qualificationReq.getFilePath().isEmpty()) {
                    throw new QualificationProcessingException("Qualification file is missing or empty");
                }

                String originalFilename = qualificationReq.getFilePath().getOriginalFilename();
                if (originalFilename == null || originalFilename.isBlank()) {
                    throw new QualificationProcessingException("Invalid qualification file name");
                }

                String fileExtension = getFileExtension(originalFilename);
                if (!isValidDocExtension(fileExtension)) {
                    throw new QualificationProcessingException("Invalid qualification file type. Only PDF, JPG, JPEG and PNG are allowed.");
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

                EmployeeQualification imageObj = new EmployeeQualification();
                imageObj.setEmployee(employee);
                imageObj.setCompletionYear(qualificationReq.getCompletionYear());
                imageObj.setQualificationName(qualificationReq.getQualificationName());
                imageObj.setFilePath(qualificationFilePath);
                imageObj.setLastChangedDate(OffsetDateTime.now().toLocalDateTime());
                imageObj.setLastChangedBy(currentUser.getUserId().toString());
                imageObj.setInstitutionName(qualificationReq.getInstitutionName());

                employeeQualificationRepository.save(imageObj);

            } catch (IOException e) {
                log.error("Error while uploading qualification file: {}", e.getMessage(), e);
                throw new QualificationProcessingException("Failed to upload qualification file: " + e.getMessage());
            } catch (Exception e) {
                log.error("Error while processing qualification: {}", e.getMessage(), e);
                throw new QualificationProcessingException("Failed to process qualification: " + e.getMessage());
            }
        }
    }


    private void processDocuments(List<EmployeeDocumentReq> documents, MasEmployee employee, User currentUser)
            throws DocumentProcessingException {

        if (documents == null || documents.isEmpty()) {
            throw new DocumentProcessingException("Documents cannot be empty");
        }

        String fileUploadDir = this.uploadDir + "MAS_EMPLOYEE/";

        for (EmployeeDocumentReq objContent : documents) {
            String imagePath = "";
            try {
                log.debug("Processing employee document...");

                if (objContent.getFilePath() == null || objContent.getFilePath().isEmpty()) {
                    throw new DocumentProcessingException("Document file is missing or empty");
                }

                String imageExtension = getFileExtension(objContent.getFilePath().getOriginalFilename());
                if (!isValidDocExtension(imageExtension)) {
                    throw new DocumentProcessingException("Employee document invalid file type. Only PDF, JPG, JPEG and PNG are allowed.");
                }

                String timestamp = String.valueOf(System.currentTimeMillis());
                String newFilename = timestamp + "_" + objContent.getFilePath().getOriginalFilename();

                imagePath = Paths.get(fileUploadDir, newFilename)
                        .toString()
                        .replace("\\", "/");

                Files.write(Paths.get(imagePath), objContent.getFilePath().getBytes());

                EmployeeDocument contentObj = new EmployeeDocument();
                contentObj.setEmployee(employee);
                contentObj.setDocumentName(objContent.getDocumentName());
                contentObj.setFilePath(imagePath);
                contentObj.setLastChangedDate(OffsetDateTime.now().toLocalDateTime());
                contentObj.setLastChangedBy(currentUser.getUserId().toString());

                employeeDocumentRepository.save(contentObj);

            } catch (IOException e) {
                log.error("Error while uploading document file: {}", e.getMessage(), e);
                throw new DocumentProcessingException("Failed to upload document file: " + e.getMessage());
            } catch (Exception e) {
                log.error("Error while processing document: {}", e.getMessage(), e);
                throw new DocumentProcessingException("Failed to process document: " + e.getMessage());
            }
        }
    }

    public static class FileProcessingException extends Exception {
        public FileProcessingException(String message) {
            super(message);
        }
    }

    public static class EmployeeCreationException extends Exception {
        public EmployeeCreationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class QualificationProcessingException extends Exception {
        public QualificationProcessingException(String message) {
            super(message);
        }
    }

    public static class DocumentProcessingException extends Exception {
        public DocumentProcessingException(String message) {
            super(message);
        }
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