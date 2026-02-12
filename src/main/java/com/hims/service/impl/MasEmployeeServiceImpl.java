package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.helperUtil.HelperUtils;
import com.hims.request.*;
import com.hims.response.*;
import com.hims.service.MasEmployeeService;
import com.hims.utils.ResponseUtils;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.websocket.Session;
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
import java.time.*;
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

    @Autowired
    private MasDesignationRepository masDesignationRepository;

    @Autowired
    private EmployeeSpecialtyCenterRepository employeeSpecialtyCenterRepository;

    @Autowired
    private EmployeeWorkExperienceRepository employeeWorkExperienceRepository;

    @Autowired
    private EmployeeMembershipRepository employeeMembershipRepository;

    @Autowired
    private EmployeeAwardRepository employeeAwardRepository;

    @Autowired
    private EmployeeSpecialtyInterestRepository employeeSpecialtyInterestRepository;

    @Autowired
    private MasSpecialtyCenterRepository masSpecialtyCenterRepository;
    @Autowired
    private VisitRepository visitRepository;
    @Autowired
    private AppSetupRepository appSetupRepository;


    @Value("${app.opdDepartmentType}")
    private Long opdId;

    @Value("${app.role.doctor}")
    private Long roleId;
    @Autowired
    private MasServiceOpdRepository masServiceOpdRepository;
    @Autowired
    private MasLanguageRepository masLanguageRepository;
    @Autowired
    private   MasEmployeeLanguageMappingRepository masEmployeeLanguageMappingRepository;
    @Autowired
    private MasOpdSessionRepository masOpdSessionRepository;



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
            List<EmployeeSpecialtyCenterMappingDTO> specialtyCenters = employeeSpecialtyCenterRepository
                    .findByEmpId(employee.getEmployeeId())
                    .stream()
                    .map(EmployeeSpecialtyCenterMappingDTO::fromEntity)
                    .toList();

            List<EmployeeWorkExperienceDTO> workExperiences = employeeWorkExperienceRepository
                    .findByEmployee(employee)
                    .stream()
                    .map(EmployeeWorkExperienceDTO::fromEntity)
                    .toList();

            List<EmployeeMembershipDTO> memberships = employeeMembershipRepository
                    .findByEmployee(employee)
                    .stream()
                    .map(EmployeeMembershipDTO::fromEntity)
                    .toList();

            List<EmployeeSpecialtyInterestDTO> specialtyInterests = employeeSpecialtyInterestRepository
                    .findByEmployee(employee)
                    .stream()
                    .map(EmployeeSpecialtyInterestDTO::fromEntity)
                    .toList();

            List<EmployeeAwardDTO> awards = employeeAwardRepository
                    .findByEmployee(employee)
                    .stream()
                    .map(EmployeeAwardDTO::fromEntity)
                    .toList();

            List<EmployeeDocumentDTO> documents = employeeDocumentRepository
                    .findByEmployee(employee)
                    .stream()
                    .map(EmployeeDocumentDTO::fromEntity)
                    .toList();

            List<EmployeeLanguageDTO> languages = masEmployeeLanguageMappingRepository
                    .findByEmpId(employee.getEmployeeId())
                    .stream()
                    .map(EmployeeLanguageDTO::fromEntity)
                    .toList();
            return MasEmployeeDTO.fromEntity(employee, qualifications, documents,specialtyCenters,workExperiences,memberships,specialtyInterests,awards,languages);
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

            List<EmployeeSpecialtyCenterMappingDTO> specialtyCenters = employeeSpecialtyCenterRepository
                    .findByEmpId(employee.getEmployeeId())
                    .stream()
                    .map(EmployeeSpecialtyCenterMappingDTO::fromEntity)
                    .toList();

            List<EmployeeWorkExperienceDTO> workExperiences = employeeWorkExperienceRepository
                    .findByEmployee(employee)
                    .stream()
                    .map(EmployeeWorkExperienceDTO::fromEntity)
                    .toList();

            List<EmployeeMembershipDTO> memberships = employeeMembershipRepository
                    .findByEmployee(employee)
                    .stream()
                    .map(EmployeeMembershipDTO::fromEntity)
                    .toList();

            List<EmployeeSpecialtyInterestDTO> specialtyInterests = employeeSpecialtyInterestRepository
                    .findByEmployee(employee)
                    .stream()
                    .map(EmployeeSpecialtyInterestDTO::fromEntity)
                    .toList();

            List<EmployeeAwardDTO> awards = employeeAwardRepository
                    .findByEmployee(employee)
                    .stream()
                    .map(EmployeeAwardDTO::fromEntity)
                    .toList();

            List<EmployeeDocumentDTO> documents = employeeDocumentRepository
                    .findByEmployee(employee)
                    .stream()
                    .map(EmployeeDocumentDTO::fromEntity)
                    .toList();

            List<EmployeeLanguageDTO> languages = masEmployeeLanguageMappingRepository
                    .findByEmpId(employee.getEmployeeId())
                    .stream()
                    .map(EmployeeLanguageDTO::fromEntity)
                    .toList();

            return MasEmployeeDTO.fromEntity(employee, qualifications, documents ,specialtyCenters,workExperiences,memberships,specialtyInterests,awards,languages);
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

        List<EmployeeSpecialtyCenterMappingDTO> specialtyCenters = employeeSpecialtyCenterRepository
                .findByEmpId(employee.getEmployeeId())
                .stream()
                .map(EmployeeSpecialtyCenterMappingDTO::fromEntity)
                .toList();

        List<EmployeeWorkExperienceDTO> workExperiences = employeeWorkExperienceRepository
                .findByEmployee(employee)
                .stream()
                .map(EmployeeWorkExperienceDTO::fromEntity)
                .toList();

        List<EmployeeMembershipDTO> memberships = employeeMembershipRepository
                .findByEmployee(employee)
                .stream()
                .map(EmployeeMembershipDTO::fromEntity)
                .toList();

        List<EmployeeSpecialtyInterestDTO> specialtyInterests = employeeSpecialtyInterestRepository
                .findByEmployee(employee)
                .stream()
                .map(EmployeeSpecialtyInterestDTO::fromEntity)
                .toList();

        List<EmployeeAwardDTO> awards = employeeAwardRepository
                .findByEmployee(employee)
                .stream()
                .map(EmployeeAwardDTO::fromEntity)
                .toList();

        List<EmployeeDocumentDTO> documents = employeeDocumentRepository
                .findByEmployee(employee)
                .stream()
                .map(EmployeeDocumentDTO::fromEntity)
                .toList();

        List<EmployeeLanguageDTO> languages = masEmployeeLanguageMappingRepository
                .findByEmpId(employee.getEmployeeId())
                .stream()
                .map(EmployeeLanguageDTO::fromEntity)
                .toList();

        MasEmployeeDTO employeeDTO = MasEmployeeDTO.fromEntity(employee, qualifications, documents,
                specialtyCenters,
                workExperiences,
                memberships,
                specialtyInterests,
                awards,languages);

        return ResponseUtils.createSuccessResponse(employeeDTO, new TypeReference<>() {});
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ApiResponse<MasEmployee> updateEmployee(Long id, MasEmployeeRequest req) {
        if (id == null) {
            throw new RuntimeException("Employee ID cannot be null");
        }
        MasEmployee employee = masEmployeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
        User currentUser = userRepo.findByUserName(
                SecurityContextHolder.getContext().getAuthentication().getName()
        );
        if (currentUser == null) {
            throw new RuntimeException("Current user not found");
        }
        updateBasicFields(employee, req);
        updateMasterReferences(employee, req);
        updateEmploymentFields(employee, req);
        handleIdDocument(employee, req);
        handleProfileImage(employee, req);
        updateSpecialtyCenters(employee, req);
        updateLanguages(employee, req);
        updateWorkExperiences(employee, req);
        updateMemberships(employee, req);
        updateSpecialtyInterests(employee, req);
        updateAwards(employee, req);
        MasEmployee savedEmployee = masEmployeeRepository.save(employee);
        updateQualifications(savedEmployee, req, currentUser);
        updateDocuments(savedEmployee, req, currentUser);
        audit(employee, currentUser);

        return ResponseUtils.createSuccessResponse(savedEmployee, new TypeReference<>() {});
    }

    private void updateBasicFields(MasEmployee emp, MasEmployeeRequest req) {
        if (req.getFirstName() != null) emp.setFirstName(req.getFirstName());
        if (req.getMiddleName() != null) emp.setMiddleName(req.getMiddleName());
        if (req.getLastName() != null) emp.setLastName(req.getLastName());
        if (req.getDob() != null) emp.setDob(req.getDob());
        if (req.getAddress1() != null) emp.setAddress1(req.getAddress1());
        if (req.getCity() != null) emp.setCity(req.getCity());
        if (req.getPincode() != null) emp.setPincode(req.getPincode());
        if (req.getRegistrationNo() != null) emp.setRegistrationNo(req.getRegistrationNo());
        if (req.getProfileDescription() != null) emp.setProfileDescription(req.getProfileDescription());
    }
    private void updateMasterReferences(MasEmployee emp, MasEmployeeRequest req) {
        if (req.getGenderId() != null) {
            emp.setGenderId(
                    masGenderRepository.findById(req.getGenderId())
                            .orElseThrow(() -> new RuntimeException("Gender not found"))
            );
        }
        if (req.getCountryId() != null) {
            emp.setCountryId(
                    masCountryRepository.findById(req.getCountryId())
                            .orElseThrow(() -> new RuntimeException("Country not found"))
            );
        }
        if (req.getStateId() != null) {
            emp.setStateId(
                    masStateRepository.findById(req.getStateId())
                            .orElseThrow(() -> new RuntimeException("State not found"))
            );
        }
        if (req.getDistrictId() != null) {
            emp.setDistrictId(
                    masDistrictRepository.findById(req.getDistrictId())
                            .orElseThrow(() -> new RuntimeException("District not found"))
            );
        }
        if (req.getIdentificationType() != null) {
            emp.setIdentificationType(
                    masIdentificationTypeRepository.findById(req.getIdentificationType())
                            .orElseThrow(() -> new RuntimeException("Identification type not found"))
            );
        }
        if (req.getMasDesignationId() != null) {
            emp.setMasDesignationId(
                    masDesignationRepository.findById(req.getMasDesignationId())
                            .orElseThrow(() -> new RuntimeException("Designation not found"))
            );
        }
    }
    private void updateEmploymentFields(MasEmployee emp, MasEmployeeRequest req) {
        if (req.getMobileNo() != null) {
            masEmployeeRepository.findByMobileNo(req.getMobileNo())
                    .filter(e -> !e.getEmployeeId().equals(emp.getEmployeeId()))
                    .ifPresent(e -> {
                        throw new RuntimeException("Mobile number already exists");
                    });
            emp.setMobileNo(req.getMobileNo());
        }
        if (req.getEmployeeTypeId() != null) {
            emp.setEmployeeTypeId(
                    masUserTypeRepository.findById(req.getEmployeeTypeId())
                            .orElseThrow(() -> new RuntimeException("Employee type not found"))
            );
        }
        if (req.getEmploymentTypeId() != null) {
            emp.setEmploymentTypeId(
                    masEmploymentTypeRepository.findById(req.getEmploymentTypeId())
                            .orElseThrow(() -> new RuntimeException("Employment type not found"))
            );
        }
        if (req.getRoleId() != null) {
            emp.setRoleId(
                    masRoleRepository.findById(req.getRoleId())
                            .orElseThrow(() -> new RuntimeException("Role not found"))
            );
        }
        if (req.getFromDate() != null) emp.setFromDate(req.getFromDate());
        if (req.getYearOfExperience() != null) emp.setYearOfExperience(req.getYearOfExperience());
    }
    private void handleIdDocument(MasEmployee emp, MasEmployeeRequest req) {
        MultipartFile file = req.getIdDocumentName();
        if (file == null || file.isEmpty()) return;
        validateDocExtension(file);
        deleteFile(emp.getIdDocumentName());
        emp.setIdDocumentName(saveFile(file));
    }
    private void handleProfileImage(MasEmployee emp, MasEmployeeRequest req) {
        MultipartFile file = req.getProfilePicName();
        if (file == null || file.isEmpty()) return;
        validatePicExtension(file);
        deleteFile(emp.getProfilePicName());
        emp.setProfilePicName(saveFile(file));
    }
    private void audit(MasEmployee emp, User user) {
        emp.setLastChangedDate(Instant.now());
        emp.setLastChangedBy(user.getUserId().toString());
    }
    private void updateSpecialtyCenters(MasEmployee emp, MasEmployeeRequest req) {
        if (req.getSpecialtyCenter() == null) return;
        employeeSpecialtyCenterRepository.deleteAll(
                employeeSpecialtyCenterRepository.findByEmpId(emp.getEmployeeId())
        );
        for (EmployeeSpecialtyCenterRequest sc : req.getSpecialtyCenter()) {
            if (sc.getCenterId() == null) continue;

            masSpecialtyCenterRepository.findById(sc.getCenterId())
                    .orElseThrow(() -> new RuntimeException("Invalid specialty center"));
            MasEmployeeCenterMapping m = new MasEmployeeCenterMapping();
            m.setEmpId(emp.getEmployeeId());
            m.setCenterId(sc.getCenterId());
            m.setIsPrimary(Boolean.TRUE.equals(sc.getIsPrimary()));
            m.setLastUpdateDate(Instant.now());
            employeeSpecialtyCenterRepository.save(m);
        }
    }

    private void updateLanguages(MasEmployee employee, MasEmployeeRequest req) {
        if (req.getLanguages() == null) {
            log.debug("Languages not provided in update request, keeping existing languages");
            return;
        }
        masEmployeeLanguageMappingRepository.deleteByEmpId(employee.getEmployeeId());
        log.debug("Deleted existing language mappings for employee ID: {}", employee.getEmployeeId());

        if (req.getLanguages().isEmpty()) {
            log.debug("Empty languages array provided, no languages to add");
            return;
        }
        for (EmployeeLanguageRequest languageReq : req.getLanguages()) {
            if (languageReq.getLanguageId() == null || languageReq.getLanguageId() == 0L) {
                continue;
            }

            try {
                MasEmployeeLanguageMapping mapping = MasEmployeeLanguageMapping.builder()
                        .empId(employee.getEmployeeId())
                        .languageId(languageReq.getLanguageId())
                        .lastChgBy(getCurrentUser().getUserId())
                        .build();

                masEmployeeLanguageMappingRepository.save(mapping);
                log.debug("Added language mapping: employeeId={}, languageId={}",
                        employee.getEmployeeId(), languageReq.getLanguageId());

            } catch (Exception e) {
                log.error("Failed to add language mapping for languageId: {}", languageReq.getLanguageId(), e);
                throw new RuntimeException("Failed to process language: " + languageReq.getLanguageId(), e);
            }
        }
    }
    private void updateWorkExperiences(MasEmployee emp, MasEmployeeRequest req) {
        if (req.getWorkExperiences() == null) return;
        employeeWorkExperienceRepository.deleteAll(
                employeeWorkExperienceRepository.findByEmployee(emp)
        );
        for (EmployeeWorkExperienceRequest w : req.getWorkExperiences()) {
            if (w.getExperienceSummary() == null) continue;
            EmployeeWorkExperience we = new EmployeeWorkExperience();
            we.setEmployee(emp);
            we.setExperienceSummary(w.getExperienceSummary());
            we.setLastUpdateDate(Instant.now());
            employeeWorkExperienceRepository.save(we);
        }
    }
    private void updateMemberships(MasEmployee emp, MasEmployeeRequest req) {
        if (req.getEmployeeMemberships() == null) return;
        employeeMembershipRepository.deleteAll(
                employeeMembershipRepository.findByEmployee(emp)
        );
        for (EmployeeMembershipRequest m : req.getEmployeeMemberships()) {
            if (m.getMembershipSummary() == null) continue;
            EmployeeMembership em = new EmployeeMembership();
            em.setEmployee(emp);
            em.setMembershipSummary(m.getMembershipSummary());
            em.setLastUpdateDate(Instant.now());
            employeeMembershipRepository.save(em);
        }
    }
    private void updateSpecialtyInterests(MasEmployee emp, MasEmployeeRequest req) {
        if (req.getEmployeeSpecialtyInterests() == null) return;
        employeeSpecialtyInterestRepository.deleteAll(
                employeeSpecialtyInterestRepository.findByEmployee(emp)
        );
        for (EmployeeSpecialtyInterestRequest i : req.getEmployeeSpecialtyInterests()) {
            if (i.getInterestSummary() == null) continue;
            EmployeeSpecialtyInterest si = new EmployeeSpecialtyInterest();
            si.setEmployee(emp);
            si.setInterestSummary(i.getInterestSummary());
            si.setLastUpdateDate(Instant.now());
            employeeSpecialtyInterestRepository.save(si);
        }
    }
    private void updateAwards(MasEmployee emp, MasEmployeeRequest req) {
        if (req.getEmployeeAwards() == null) return;
        employeeAwardRepository.deleteAll(
                employeeAwardRepository.findByEmployee(emp)
        );
        for (EmployeeAwardRequest a : req.getEmployeeAwards()) {
            if (a.getAwardSummary() == null) continue;
            EmployeeAward aw = new EmployeeAward();
            aw.setEmployee(emp);
            aw.setAwardSummary(a.getAwardSummary());
            aw.setLastUpdateDate(Instant.now());
            employeeAwardRepository.save(aw);
        }
    }
    private void updateQualifications(MasEmployee emp, MasEmployeeRequest req, User user) {
        if (req.getQualification() == null) return;
        employeeQualificationRepository.deleteAll(
                employeeQualificationRepository.findByEmployee(emp)
        );
        for (EmployeeQualificationReq q : req.getQualification()) {
            String filePath = saveFile(q.getFilePath());
            EmployeeQualification eq = new EmployeeQualification();
            eq.setEmployee(emp);
            eq.setQualificationName(q.getQualificationName());
            eq.setCompletionYear(q.getCompletionYear());
            eq.setInstitutionName(q.getInstitutionName());
            eq.setFilePath(filePath);
            eq.setLastChangedBy(user.getUserId().toString());
            eq.setLastChangedDate(OffsetDateTime.now().toLocalDateTime());
            employeeQualificationRepository.save(eq);
        }
    }
    private void updateDocuments(MasEmployee emp, MasEmployeeRequest req, User user) {
        if (req.getDocument() == null) return;
        employeeDocumentRepository.deleteAll(
                employeeDocumentRepository.findByEmployee(emp)
        );
        for (EmployeeDocumentReq d : req.getDocument()) {
            String filePath = saveFile(d.getFilePath());
            EmployeeDocument doc = new EmployeeDocument();
            doc.setEmployee(emp);
            doc.setDocumentName(d.getDocumentName());
            doc.setFilePath(filePath);
            doc.setLastChangedBy(user.getUserId().toString());
            doc.setLastChangedDate(OffsetDateTime.now().toLocalDateTime());
            employeeDocumentRepository.save(doc);
        }
    }
    private String saveFile(MultipartFile file) {
        try {
            String original = file.getOriginalFilename();
            String clean = original.trim().replaceAll("\\s+", "_");
            if (!clean.matches("^\\d+_.*")) {
                clean = System.currentTimeMillis() + "_" + clean;
            }
            Path path = Paths.get(uploadDir + "MAS_EMPLOYEE/", clean);
            Files.createDirectories(path.getParent());
            Files.write(path, file.getBytes());
            return path.toString().replace("\\", "/");

        } catch (IOException e) {
            throw new RuntimeException("File upload failed");
        }
    }
    private void deleteFile(String path) {
        if (path == null) return;
        try {
            Files.deleteIfExists(Paths.get(path));
        } catch (IOException ignored) {}
    }
    private void validateDocExtension(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            throw new RuntimeException("Invalid document file name");
        }
        String extension = getFileExtension(filename);
        if (!isValidDocExtension(extension)) {
            throw new RuntimeException(
                    "Document invalid file type. Only PDF, JPG, JPEG and PNG are allowed"
            );
        }
    }
    private void validatePicExtension(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            throw new RuntimeException("Invalid image file name");
        }
        String extension = getFileExtension(filename);
        if (!isValidPicExtension(extension)) {
            throw new RuntimeException(
                    "Profile image invalid file type. Only JPG, JPEG and PNG are allowed"
            );
        }
    }


//    @Transactional(rollbackFor = {Exception.class})
//    @Override
//    public ApiResponse<MasEmployee> updateEmployee(Long id, MasEmployeeRequest masEmployeeRequest) {
//        log.debug("Updating Employee with ID: {}", id);
//        try {
//            if (id == null) {
//                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "ID cannot be null.", 400);
//            }
//
//            MasEmployee existingEmployee = masEmployeeRepository.findById(id)
//                    .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
//
//            User obj = userRepo.findByUserName(SecurityContextHolder.getContext().getAuthentication().getName());
//            if (obj == null) {
//                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "CURRENT USER NOT FOUND", 400);
//            }
//
//            if (masEmployeeRequest.getFirstName() != null && !masEmployeeRequest.getFirstName().isEmpty()) {
//                existingEmployee.setFirstName(masEmployeeRequest.getFirstName());
//            }
//
//            if (masEmployeeRequest.getMiddleName() != null) {
//                existingEmployee.setMiddleName(masEmployeeRequest.getMiddleName());
//            }
//
//            if (masEmployeeRequest.getLastName() != null) {
//                existingEmployee.setLastName(masEmployeeRequest.getLastName());
//            }
//
//            if (masEmployeeRequest.getGenderId() != null) {
//                MasGender genderObj = masGenderRepository.findById(masEmployeeRequest.getGenderId().longValue())
//                        .orElseThrow(() -> new IllegalArgumentException("Gender not found with ID: " + masEmployeeRequest.getGenderId()));
//                existingEmployee.setGenderId(genderObj);
//            }
//
//            if (masEmployeeRequest.getDob() != null) {
//                existingEmployee.setDob(masEmployeeRequest.getDob());
//            }
//
//            if (masEmployeeRequest.getAddress1() != null && !masEmployeeRequest.getAddress1().isEmpty()) {
//                existingEmployee.setAddress1(masEmployeeRequest.getAddress1());
//            }
//
//            if (masEmployeeRequest.getCity() != null && !masEmployeeRequest.getCity().isEmpty()) {
//                existingEmployee.setCity(masEmployeeRequest.getCity());
//            }
//
//            if (masEmployeeRequest.getMobileNo() != null && !masEmployeeRequest.getMobileNo().isEmpty()) {
//                existingEmployee.setMobileNo(masEmployeeRequest.getMobileNo());
//            }
//
//            if (masEmployeeRequest.getRegistrationNo() != null && !masEmployeeRequest.getRegistrationNo().isEmpty()) {
//                existingEmployee.setRegistrationNo(masEmployeeRequest.getRegistrationNo());
//            }
//
//            if (masEmployeeRequest.getFromDate() != null) {
//                existingEmployee.setFromDate(masEmployeeRequest.getFromDate());
//            }
//
//            if (masEmployeeRequest.getMobileNo() != null && !masEmployeeRequest.getMobileNo().isEmpty()) {
//                Optional<MasEmployee> existingWithSameMobile = masEmployeeRepository.findByMobileNo(masEmployeeRequest.getMobileNo());
//
//                if (existingWithSameMobile.isPresent() && !existingWithSameMobile.get().getEmployeeId().equals(existingEmployee.getEmployeeId())) {
//                    throw new IllegalArgumentException("Mobile number already exists: " + masEmployeeRequest.getMobileNo());
//                }
//
//                existingEmployee.setMobileNo(masEmployeeRequest.getMobileNo());
//            }
//
//            if (masEmployeeRequest.getStateId() != null) {
//                MasState stateObj = masStateRepository.findById(masEmployeeRequest.getStateId().longValue())
//                        .orElseThrow(() -> new IllegalArgumentException("State not found with ID: " + masEmployeeRequest.getStateId()));
//                existingEmployee.setStateId(stateObj);
//            }
//            if (masEmployeeRequest.getCountryId() != null) {
//                MasCountry countryObj = masCountryRepository.findById(masEmployeeRequest.getCountryId().longValue())
//                        .orElseThrow(() -> new IllegalArgumentException("Country not found with ID: " + masEmployeeRequest.getCountryId()));
//                existingEmployee.setCountryId(countryObj);
//            }
//
//            if (masEmployeeRequest.getDistrictId() != null) {
//                MasDistrict districtObj = masDistrictRepository.findById(masEmployeeRequest.getDistrictId().longValue())
//                        .orElseThrow(() -> new IllegalArgumentException("District not found with ID: " + masEmployeeRequest.getDistrictId()));
//                existingEmployee.setDistrictId(districtObj);
//            }
//
//            if (masEmployeeRequest.getIdentificationType() != null) {
//                MasIdentificationType idTypeObj = masIdentificationTypeRepository.findById(masEmployeeRequest.getIdentificationType().longValue())
//                        .orElseThrow(() -> new IllegalArgumentException("ID Type not found with ID: " + masEmployeeRequest.getIdentificationType()));
//                existingEmployee.setIdentificationType(idTypeObj);
//            }
//
//            if (masEmployeeRequest.getEmployeeTypeId() != null) {
//                MasUserType empTypeObj = masUserTypeRepository.findById(masEmployeeRequest.getEmployeeTypeId().longValue())
//                        .orElseThrow(() -> new IllegalArgumentException("Employee Type not found with ID: " + masEmployeeRequest.getEmployeeTypeId()));
//                existingEmployee.setEmployeeTypeId(empTypeObj);
//            }
//
//            if (masEmployeeRequest.getEmploymentTypeId() != null) {
//                MasEmploymentType employmentTypeObj = masEmploymentTypeRepository.findById(masEmployeeRequest.getEmploymentTypeId().longValue())
//                        .orElseThrow(() -> new IllegalArgumentException("Employment Type not found with ID: " + masEmployeeRequest.getEmploymentTypeId()));
//                existingEmployee.setEmploymentTypeId(employmentTypeObj);
//            }
//
//            if (masEmployeeRequest.getRoleId() != null) {
//                MasRole roleObj = masRoleRepository.findById(masEmployeeRequest.getRoleId().longValue())
//                        .orElseThrow(() -> new IllegalArgumentException("Role not found with ID: " + masEmployeeRequest.getRoleId()));
//                existingEmployee.setRoleId(roleObj);
//            }
//
//            if (masEmployeeRequest.getPincode() != null && !masEmployeeRequest.getPincode().isEmpty()) {
//                existingEmployee.setPincode(masEmployeeRequest.getPincode());
//            }
//            if (masEmployeeRequest.getYearOfExperience() != null) {
//                existingEmployee.setYearOfExperience(
//                        masEmployeeRequest.getYearOfExperience()
//                );
//            }
//
//            if (masEmployeeRequest.getProfileDescription() != null) {
//                existingEmployee.setProfileDescription(
//                        masEmployeeRequest.getProfileDescription()
//                );
//            }
//
//            if (masEmployeeRequest.getMasDesignationId() != null) {
//                MasDesignation designation = masDesignationRepository
//                        .findById(masEmployeeRequest.getMasDesignationId())
//                        .orElseThrow(() ->
//                                new IllegalArgumentException("Designation not found"));
//
//                existingEmployee.setMasDesignationId(designation);
//            }
//
//
//            String fileUploadDir = uploadDir + "MAS_EMPLOYEE/";
//            File directory = new File(fileUploadDir);
//            if (!directory.exists()) {
//                directory.mkdirs();
//            }
//
//            if (masEmployeeRequest.getIdDocumentName() != null && !masEmployeeRequest.getIdDocumentName().isEmpty()) {
//                try {
//                    String uploadedFilename = masEmployeeRequest.getIdDocumentName().getOriginalFilename();
//
//                    String documentExtension = getFileExtension(masEmployeeRequest.getIdDocumentName().getOriginalFilename());
//                    if (!isValidDocExtension(documentExtension)) {
//                        return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
//                                "Document Invalid file type. Only PDF, JPG, JPEG and PNG are allowed.", 400);
//                    }
//
//                    // Check if existing document already has timestamp
//                    boolean shouldUpdate = true;
//                    if (masEmployeeRequest.getIdDocumentName() != null && !masEmployeeRequest.getIdDocumentName().isEmpty()) {
//                        if (hasTimestamp(masEmployeeRequest.getIdDocumentName().getOriginalFilename())) {
//                            // Existing document already has timestamp, don't update
//                            shouldUpdate = false;
//                            log.info("ID document already has timestamp, skipping update to avoid timestamp chaining");
//                        } else {
//                            // Delete old file if it doesn't have timestamp
//                            try {
//                                Path oldFilePath = Paths.get(existingEmployee.getIdDocumentName());
//                                Files.deleteIfExists(oldFilePath);
//                            } catch (IOException e) {
//                                log.warn("Could not delete old ID document: {}", existingEmployee.getIdDocumentName(), e);
//                            }
//                        }
//                    }
//
//                    if (shouldUpdate) {
//                        String timestamp = String.valueOf(System.currentTimeMillis());
//                        String newFilename = timestamp + "_" + masEmployeeRequest.getIdDocumentName().getOriginalFilename();
//
//                        String documentPath = Paths.get(fileUploadDir, newFilename)
//                                .toString()
//                                .replace("\\", "/");
//                        Files.write(Paths.get(documentPath), masEmployeeRequest.getIdDocumentName().getBytes());
//
//                        existingEmployee.setIdDocumentName(documentPath);
//                    }
//                } catch (IOException e) {
//                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Failed to upload document.", 400);
//                }
//            }
//            // ========== UPDATE SPECIALTY CENTERS ==========
//            // Remove all existing mappings first
//            List<MasEmployeeCenterMapping> existingMappings = employeeSpecialtyCenterRepository.findByEmpId(existingEmployee.getEmployeeId());
//            if (!existingMappings.isEmpty()) {
//                employeeSpecialtyCenterRepository.deleteAll(existingMappings);
//            }
//
//            // Add all new mappings
//            for (EmployeeSpecialtyCenterRequest centerReq : masEmployeeRequest.getSpecialtyCenter()) {
//                if (centerReq.getCenterId() != null) {
//                    // Verify center exists
//                    masSpecialtyCenterRepository.findById(centerReq.getCenterId())
//                            .orElseThrow(() -> new IllegalArgumentException("Specialty Center not found with ID: " + centerReq.getCenterId()));
//
//                    MasEmployeeCenterMapping mapping = new MasEmployeeCenterMapping();
//                    mapping.setEmpId(existingEmployee.getEmployeeId());
//                    mapping.setCenterId(centerReq.getCenterId());
//                    mapping.setIsPrimary(centerReq.getIsPrimary() != null ? centerReq.getIsPrimary() : false);
//                    mapping.setLastUpdateDate(Instant.now());
//
//                    employeeSpecialtyCenterRepository.save(mapping);
//                }
//            }           // ========== UPDATE WORK EXPERIENCES ==========
//            if (masEmployeeRequest.getWorkExperiences() != null && !masEmployeeRequest.getWorkExperiences().isEmpty()) {
//                // Remove existing work experiences
//                List<EmployeeWorkExperience> existingExperiences = employeeWorkExperienceRepository.findByEmployee(existingEmployee);
//                if (existingExperiences != null && !existingExperiences.isEmpty()) {
//                    employeeWorkExperienceRepository.deleteAll(existingExperiences);
//                }
//
//                for (EmployeeWorkExperienceRequest expReq : masEmployeeRequest.getWorkExperiences()) {
//                    if (expReq.getExperienceSummary() != null && !expReq.getExperienceSummary().isEmpty()) {
//                        EmployeeWorkExperience workExperience = new EmployeeWorkExperience();
//                        workExperience.setEmployee(existingEmployee);
//                        workExperience.setExperienceSummary(expReq.getExperienceSummary());
//                        workExperience.setLastUpdateDate(Instant.now());
//
//                        employeeWorkExperienceRepository.save(workExperience);
//                    }
//                }
//            }
//            // ========== UPDATE MEMBERSHIPS ==========
//            if (masEmployeeRequest.getEmployeeMemberships() != null && !masEmployeeRequest.getEmployeeMemberships().isEmpty()) {
//                // Remove existing memberships
//                List<EmployeeMembership> existingMemberships = employeeMembershipRepository.findByEmployee(existingEmployee);
//                if (existingMemberships != null && !existingMemberships.isEmpty()) {
//                    employeeMembershipRepository.deleteAll(existingMemberships);
//                }
//
//                // Add new memberships
//                for (EmployeeMembershipRequest membershipReq : masEmployeeRequest.getEmployeeMemberships()) {
//                    if (membershipReq.getMembershipSummary() != null && !membershipReq.getMembershipSummary().isEmpty()) {
//                        EmployeeMembership membership = new EmployeeMembership();
//                        membership.setEmployee(existingEmployee);
//                        membership.setMembershipSummary(membershipReq.getMembershipSummary());
//                        membership.setLastUpdateDate(Instant.now());
//
//                        employeeMembershipRepository.save(membership);
//                    }
//                }
//            }
//
//            // ========== UPDATE SPECIALTY INTERESTS ==========
//            if (masEmployeeRequest.getEmployeeSpecialtyInterests() != null && !masEmployeeRequest.getEmployeeSpecialtyInterests().isEmpty()) {
//                // Remove existing interests
//                List<EmployeeSpecialtyInterest> existingInterests = employeeSpecialtyInterestRepository.findByEmployee(existingEmployee);
//                if (existingInterests != null && !existingInterests.isEmpty()) {
//                    employeeSpecialtyInterestRepository.deleteAll(existingInterests);
//                }
//
//                // Add new interests
//                for (EmployeeSpecialtyInterestRequest interestReq : masEmployeeRequest.getEmployeeSpecialtyInterests()) {
//                    if (interestReq.getInterestSummary() != null && !interestReq.getInterestSummary().isEmpty()) {
//                        EmployeeSpecialtyInterest specialtyInterest = new EmployeeSpecialtyInterest();
//                        specialtyInterest.setEmployee(existingEmployee);
//                        specialtyInterest.setInterestSummary(interestReq.getInterestSummary());
//                        specialtyInterest.setLastUpdateDate(Instant.now());
//
//                        employeeSpecialtyInterestRepository.save(specialtyInterest);
//                    }
//                }
//            }
//
//            // ========== UPDATE AWARDS ==========
//            if (masEmployeeRequest.getEmployeeAwards() != null && !masEmployeeRequest.getEmployeeAwards().isEmpty()) {
//                // Remove existing awards
//                List<EmployeeAward> existingAwards = employeeAwardRepository.findByEmployee(existingEmployee);
//                if (existingAwards != null && !existingAwards.isEmpty()) {
//                    employeeAwardRepository.deleteAll(existingAwards);
//                }
//
//                for (EmployeeAwardRequest awardReq : masEmployeeRequest.getEmployeeAwards()) {
//                    if (awardReq.getAwardSummary() != null && !awardReq.getAwardSummary().isEmpty()) {
//                        EmployeeAward award = new EmployeeAward();
//                        award.setEmployee(existingEmployee);
//                        award.setAwardSummary(awardReq.getAwardSummary());
//                        award.setLastUpdateDate(Instant.now());
//
//                        employeeAwardRepository.save(award);
//                    }
//                }
//            }
//
//            if (masEmployeeRequest.getProfilePicName() != null && !masEmployeeRequest.getProfilePicName().isEmpty()) {
//                try {
//                    String profileImageExtension = getFileExtension(masEmployeeRequest.getProfilePicName().getOriginalFilename());
//                    if (!isValidPicExtension(profileImageExtension)) {
//                        return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
//                                "Thumb Image Invalid file type. Only JPG, JPEG and PNG are allowed.", 400);
//                    }
//
//                    boolean shouldUpdate = true;
//                    if (masEmployeeRequest.getProfilePicName() != null && !masEmployeeRequest.getProfilePicName().isEmpty()) {
//                        if (hasTimestamp(masEmployeeRequest.getProfilePicName().getOriginalFilename())) {
//                            // Existing document already has timestamp, don't update
//                            shouldUpdate = false;
//                            log.info("profile pic already has timestamp, skipping update to avoid timestamp chaining");
//                        } else {
//                            // Delete old file if it doesn't have timestamp
//                            try {
//                                Path oldFilePath = Paths.get(existingEmployee.getProfilePicName());
//                                Files.deleteIfExists(oldFilePath);
//                            } catch (IOException e) {
//                                log.warn("Could not delete old ID document: {}", existingEmployee.getProfilePicName(), e);
//                            }
//                        }
//                    }
//                    if(shouldUpdate){
//                    String timestamp = String.valueOf(System.currentTimeMillis());
//                    String newFilename = timestamp + "_" + masEmployeeRequest.getProfilePicName().getOriginalFilename();
//
//                    String profileImagePath = Paths.get(fileUploadDir, newFilename)
//                            .toString()
//                            .replace("\\", "/");
//                    Files.write(Paths.get(profileImagePath), masEmployeeRequest.getProfilePicName().getBytes());
//                        existingEmployee.setProfilePicName(profileImagePath);
//                    }
//                } catch (IOException e) {
//                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Failed to upload profile image.", 400);
//                }
//            }
//
//            existingEmployee.setLastChangedDate(OffsetDateTime.now().toInstant());
//            existingEmployee.setLastChangedBy(obj.getUserId().toString());
//
//            MasEmployee savedEmp = masEmployeeRepository.save(existingEmployee);
//
//
//            if (masEmployeeRequest.getQualification() != null && !masEmployeeRequest.getQualification().isEmpty()) {
//                for (EmployeeQualificationReq objQualification : masEmployeeRequest.getQualification()) {
//                    if (objQualification.getEmployeeQualificationId() != null) {
//                        EmployeeQualification existingQualification = employeeQualificationRepository.findById(objQualification.getEmployeeQualificationId())
//                                .orElseThrow(() -> new RuntimeException("Employee Qualification not found with id: " + objQualification.getEmployeeQualificationId()));
//
//                        if (objQualification.getQualificationName() != null) {
//                            existingQualification.setQualificationName(objQualification.getQualificationName());
//                        }
//
//                        if (objQualification.getCompletionYear() != null) {
//                            existingQualification.setCompletionYear(objQualification.getCompletionYear());
//                        }
//
//                        if (objQualification.getInstitutionName() != null) {
//                            existingQualification.setInstitutionName(objQualification.getInstitutionName());
//                        }
//
//                        if (objQualification.getFilePath() != null && !objQualification.getFilePath().isEmpty()) {
//                            try {
//                                String originalFilename = objQualification.getFilePath().getOriginalFilename();
//                                if (originalFilename == null || originalFilename.isBlank()) {
//                                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid file name.", 400);
//                                }
//
//                                String imageExtension = getFileExtension(originalFilename);
//                                if (!isValidDocExtension(imageExtension)) {
//                                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
//                                            "Invalid file type. Only PDF, JPG, JPEG and PNG are allowed.", 400);
//                                }
//
//                                boolean shouldUpdate = true;
//                                if (objQualification.getFilePath() != null && !objQualification.getFilePath().isEmpty()) {
//                                    if (hasTimestamp(objQualification.getFilePath().getOriginalFilename())) {
//                                        // Existing document already has timestamp, don't update
//                                        shouldUpdate = false;
//                                        log.info("Employee document already has timestamp, skipping update");
//                                    } else {
//                                        // Delete old file if it doesn't have timestamp
//                                        try {
//                                            Path oldFilePath = Paths.get(existingQualification.getFilePath());
//                                            Files.deleteIfExists(oldFilePath);
//                                        } catch (IOException e) {
//                                            log.warn("Could not delete old employee document: {}", existingQualification.getFilePath(), e);
//                                        }
//                                    }
//                                }
//                                if(shouldUpdate) {
//                                    String timestamp = String.valueOf(System.currentTimeMillis());
//                                    String newFilename = timestamp + "_" + originalFilename;
//
//                                    String imagePath = Paths.get(fileUploadDir, newFilename)
//                                            .toString()
//                                            .replace("\\", "/");
//
//                                    Files.write(Paths.get(imagePath), objQualification.getFilePath().getBytes());
//                                    existingQualification.setFilePath(imagePath);
//                                }
//                            } catch (IOException e) {
//                                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Failed to upload qualification document.", 400);
//                            }
//                        }
//
//                        existingQualification.setLastChangedDate(OffsetDateTime.now().toLocalDateTime());
//                        existingQualification.setLastChangedBy(obj.getUserId().toString());
//
//                        employeeQualificationRepository.save(existingQualification);
//                    } else {
//                        String imagePath = "";
//                        try {
//                            if (objQualification.getFilePath() != null && !objQualification.getFilePath().isEmpty()) {
//                                String originalFilename = objQualification.getFilePath().getOriginalFilename();
//                                if (originalFilename == null || originalFilename.isBlank()) {
//                                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid file name.", 400);
//                                }
//
//                                String imageExtension = getFileExtension(originalFilename);
//                                if (!isValidDocExtension(imageExtension)) {
//                                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
//                                            "Invalid file type. Only PDF, JPG, JPEG and PNG are allowed.", 400);
//                                }
//
//                                Path uploadDir = Paths.get(fileUploadDir);
//                                if (!Files.exists(uploadDir)) {
//                                    Files.createDirectories(uploadDir);
//                                }
//
//                                String timestamp = String.valueOf(System.currentTimeMillis());
//                                String newFilename = timestamp + "_" + originalFilename;
//
//                                imagePath = Paths.get(fileUploadDir, newFilename)
//                                        .toString()
//                                        .replace("\\", "/");
//
//                                Files.write(Paths.get(imagePath), objQualification.getFilePath().getBytes());
//                            } else {
//                                log.warn("Qualification document file is missing or empty.");
//                                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Qualification document cannot be empty.", 400);
//                            }
//                        } catch (IOException e) {
//                            log.error("Error while uploading qualification document: {}", e.getMessage());
//                            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Failed to upload qualification document.", 400);
//                        }
//
//                        EmployeeQualification qualificationObj = new EmployeeQualification();
//                        qualificationObj.setEmployee(savedEmp);
//                        qualificationObj.setCompletionYear(objQualification.getCompletionYear());
//                        qualificationObj.setQualificationName(objQualification.getQualificationName());
//                        qualificationObj.setFilePath(imagePath);
//                        qualificationObj.setLastChangedDate(OffsetDateTime.now().toLocalDateTime());
//                        qualificationObj.setLastChangedBy(obj.getUserId().toString());
//                        qualificationObj.setInstitutionName(objQualification.getInstitutionName());
//
//                        employeeQualificationRepository.save(qualificationObj);
//                    }
//                }
//            }
//
//            Set<Long> requestQualificationIds = masEmployeeRequest.getQualification().stream()
//                    .filter(q -> q.getEmployeeQualificationId() != null)
//                    .map(EmployeeQualificationReq::getEmployeeQualificationId)
//                    .collect(Collectors.toSet());
//
//            // Find and delete qualifications not in the request
//            List<EmployeeQualification> existingQualifications = employeeQualificationRepository.findByEmployee(existingEmployee);
//            for (EmployeeQualification existingQual : existingQualifications) {
//                if (!requestQualificationIds.contains(existingQual.getEmployeeQualificationId())) {
//                    employeeQualificationRepository.delete(existingQual);
//                }
//            }
//
//            if (masEmployeeRequest.getDocument() != null && !masEmployeeRequest.getDocument().isEmpty()) {
//
//                List<EmployeeDocument> existingDocuments = employeeDocumentRepository.findByEmployee(savedEmp);
//
//                for (EmployeeDocumentReq objDocument : masEmployeeRequest.getDocument()) {
//                    if (objDocument.getEmployeeDocumentId() != null) {
//                        EmployeeDocument existingDocument = employeeDocumentRepository.findById(objDocument.getEmployeeDocumentId())
//                                .orElseThrow(() -> new RuntimeException("Employee Document not found with id: " + objDocument.getEmployeeDocumentId()));
//
//                        if (objDocument.getDocumentName() != null) {
//                            existingDocument.setDocumentName(objDocument.getDocumentName());
//                        }
//
//                        if (objDocument.getFilePath() != null && !objDocument.getFilePath().isEmpty()) {
//                            try {
//                                String imageExtension = getFileExtension(objDocument.getFilePath().getOriginalFilename());
//                                if (!isValidDocExtension(imageExtension)) {
//                                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
//                                            "Document Invalid file type. Only PDF, JPG, JPEG and PNG are allowed.", 400);
//                                }
//                                // Check if we should update based on EXISTING file in database
//                                boolean shouldUpdate = true;
//                                if (objDocument.getFilePath() != null && !objDocument.getFilePath().isEmpty()) {
//                                    if (hasTimestamp(objDocument.getFilePath().getOriginalFilename())) {
//                                        // Existing document in database already has timestamp, don't update
//                                        shouldUpdate = false;
//                                        log.info("Employee document already has timestamp, skipping update");
//                                    } else {
//                                        // Delete old file from storage if it doesn't have timestamp
//                                        try {
//                                            Path oldFilePath = Paths.get(existingDocument.getFilePath());
//                                            Files.deleteIfExists(oldFilePath);
//                                        } catch (IOException e) {
//                                            log.warn("Could not delete old employee document: {}", existingDocument.getFilePath(), e);
//                                        }
//                                    }
//                                }
//
//                                // Only update if shouldUpdate is true
//                                if (shouldUpdate) {
//                                    String timestamp = String.valueOf(System.currentTimeMillis());
//                                    String newFilename = timestamp + "_" + objDocument.getFilePath().getOriginalFilename();
//
//                                    String imagePath = Paths.get(fileUploadDir, newFilename)
//                                            .toString()
//                                            .replace("\\", "/");
//                                    Files.write(Paths.get(imagePath), objDocument.getFilePath().getBytes());
//
//                                    existingDocument.setFilePath(imagePath);
//                                }
//                            } catch (IOException e) {
//                                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Failed to upload document.", 400);
//                            }
//                        }
//
//                        existingDocument.setLastChangedDate(OffsetDateTime.now().toLocalDateTime());
//                        existingDocument.setLastChangedBy(obj.getUserId().toString());
//
//                        employeeDocumentRepository.save(existingDocument);
//                    } else {
//                        String imagePath = "";
//                        try {
//                            if (objDocument.getFilePath() != null && !objDocument.getFilePath().isEmpty()) {
//                                String imageExtension = getFileExtension(objDocument.getFilePath().getOriginalFilename());
//                                if (!isValidDocExtension(imageExtension)) {
//                                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
//                                            "Document Invalid file type. Only PDF, JPG, JPEG and PNG are allowed.", 400);
//                                }
//
//                                String timestamp = String.valueOf(System.currentTimeMillis());
//                                String newFilename = timestamp + "_" + objDocument.getFilePath().getOriginalFilename();
//
//                                imagePath = Paths.get(fileUploadDir, newFilename)
//                                        .toString()
//                                        .replace("\\", "/");
//                                Files.write(Paths.get(imagePath), objDocument.getFilePath().getBytes());
//                            } else {
//                                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Document file cannot be empty.", 400);
//                            }
//                        } catch (IOException e) {
//                            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Failed to upload document.", 400);
//                        }
//
//                        EmployeeDocument documentObj = new EmployeeDocument();
//                        documentObj.setEmployee(savedEmp);
//                        documentObj.setDocumentName(objDocument.getDocumentName());
//                        documentObj.setFilePath(imagePath);
//                        documentObj.setLastChangedDate(OffsetDateTime.now().toLocalDateTime());
//                        documentObj.setLastChangedBy(obj.getUserId().toString());
//
//                        employeeDocumentRepository.save(documentObj);
//                    }
//                }
//            }
//
//            return ResponseUtils.createSuccessResponse(savedEmp, new TypeReference<>() {});
//
//        } catch (ConstraintViolationException e) {
//
//            return ResponseUtils.createFailureResponse(null, new TypeReference<MasEmployee>() {},
//                    "Validation failed for required fields: " + e.getMessage(), HttpStatus.BAD_REQUEST.value());
//        } catch (Exception e) {
//            log.error("Unexpected error :: ",e);
//            return  ResponseUtils.createFailureResponse(null, new TypeReference<>() {},"Internal Server Error",HttpStatus.INTERNAL_SERVER_ERROR.value());
//        }
//    }

    private boolean hasTimestamp(String filename) {
        if (filename == null || filename.isEmpty()) {
            return false;
        }

        // Extract just the filename from the full path
        String justFilename = filename.contains("/")
                ? filename.substring(filename.lastIndexOf("/") + 1)
                : filename;

        // Check if filename starts with digits followed by underscore
        return justFilename.matches("^\\d+_.*");
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
                    .profilePicture(employeeObj.getProfilePicName())
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
            newUserdept.setStatus("y");
            newUserdept.setLasUpdatedDt(OffsetDateTime.now());
            newUserdept.setLastChgBy(currentUser.getUsername());
            newUserdept.setLasUpdatedDt(OffsetDateTime.now());
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

@Override
public ApiResponse<List<SpecialitiesAndDoctorResponse>> getDepartmentAndDoctor(String search,Long hospitalId) {
    try {
        log.info("Fetching OPD departments and doctors list with search: {}", search);
        if (search == null || search.isBlank()) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Search text is required", HttpStatus.BAD_REQUEST.value()
            );
        }
        String keyword = search.trim();
        // ================= Departments =================
        List<MasDepartment> departments;
        departments = masDepartmentRepository
                            .findByHospitalIdAndDepartmentTypeIdAndDepartmentNameContainingIgnoreCaseOrderByDepartmentNameAsc(
                                    hospitalId, opdId, keyword);

        List<SpecialitiesResponse> deptResponseList =
                departments.stream().map(dept -> {
                    SpecialitiesResponse dto = new SpecialitiesResponse();
                    dto.setSpecialityId(dept.getId());
                    dto.setSpecialityName(dept.getDepartmentName());
                    return dto;
                }).toList();
// Doctors
        List<Long> employeeIds;
        // Step 1: Departments (hospital + OPD)
            List<MasDepartment> departments2 = masDepartmentRepository.findByHospitalIdAndDepartmentTypeId(hospitalId, opdId);
            // Step 2: UserDepartment → EmployeeIds
            List<UserDepartment> userDepartments = userDepartmentRepository.findByDepartmentIn(departments2);
            employeeIds = userDepartments.stream()
                            .map(UserDepartment::getUser)
                            .filter(Objects::nonNull)
                            .map(User::getEmployee)
                            .filter(Objects::nonNull)
                            .map(MasEmployee::getEmployeeId)
                            .distinct()
                            .toList();
       // }
// =========================
// CASE 2: hospitalId NOT present
// =========================
//        else {
//            // Step 1: Departments (ONLY OPD)
//            List<MasDepartment> departments2 = masDepartmentRepository.findByDepartmentTypeId(opdId);
//
//            // Step 2: UserDepartment → EmployeeIds
//            List<UserDepartment> userDepartments = userDepartmentRepository.findByDepartmentIn(departments2);
//            employeeIds = userDepartments.stream()
//                            .map(UserDepartment::getUser)
//                            .filter(Objects::nonNull)
//                            .map(User::getEmployee)
//                            .filter(Objects::nonNull)
//                            .map(MasEmployee::getEmployeeId)
//                            .distinct()
//                            .toList();
//        }

//        if (employeeIds.isEmpty()) {
//            return ResponseUtils.createFailureResponse(
//                    null, new TypeReference<>() {},
//                    "No doctor found",
//                    HttpStatus.NOT_FOUND.value()
//            );
//        }

        List<MasEmployee> doctors =
                masEmployeeRepository
                        .findByEmployeeIdInAndRoleIdIdAndStatusIgnoreCaseAndFirstNameContainingIgnoreCaseOrderByFirstNameAsc(
                                employeeIds, roleId, "A", keyword
                        );

        List<DoctorResponse> doctorResponseList =
                doctors.stream().map(emp -> {
                    DoctorResponse dto = new DoctorResponse();
                    User user = userRepo.findByEmployee_EmployeeId(emp.getEmployeeId());
                    dto.setDoctorId( user.getUserId());
                    //dto.setDoctorId(emp.getEmployeeId());
                    dto.setYearOfExperience(user.getEmployee().getYearOfExperience());
                    dto.setDoctorName(emp.getFirstName() + (emp.getLastName() != null ? " " + emp.getLastName() : ""));
                   // User user=userRepo.findByEmployee_EmployeeId(emp.getEmployeeId());
                    Optional<MasServiceOpd> masServiceOpd=masServiceOpdRepository.findByDoctorId_UserId(user!=null?user.getUserId():null);
                    dto.setConsultancyFee(masServiceOpd.map(MasServiceOpd::getBaseTariff).orElse(null));
                    List<Object[]> rows = appSetupRepository.findDistinctDoctorSessionNextDay(user.getUserId());
                    List<SessionResponseList> sessionList = rows.stream().map(r -> {
                        SessionResponseList s = new SessionResponseList();
                        s.setSessionId(((Number) r[1]).longValue());
                        s.setDay((String) r[2]);          // <-- nearest day for that session
                        s.setStartTime((String) r[3]);
                        s.setEndTime((String) r[4]);
                        return s;
                    }).toList();
                    dto.setSessionResponseLists(sessionList);
                    return dto;
                }).toList();

        if (deptResponseList.isEmpty() && doctorResponseList.isEmpty()) {
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "No department or doctor found",
                    HttpStatus.NOT_FOUND.value()
            );
        }

        SpecialitiesAndDoctorResponse response = new SpecialitiesAndDoctorResponse();
        response.setSpecialitiesResponseList(deptResponseList);
        response.setDoctorResponseList(doctorResponseList);
        return ResponseUtils.createSuccessResponse(Collections.singletonList(response), new TypeReference<>() {}
        );

    } catch (Exception ex) {
        log.error("Error while fetching Department and Doctor data", ex);
        return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
    }
    }

    @Override
    public ApiResponse<List<SpecialityResponse>> getSpecialityAndDoctor(Long specialityId) {

        log.info("Fetching OPD Speciality and Doctor list, specialityId={}", specialityId);
        try {
            //  OPD Departments
            List<MasDepartment> opdDepartments = masDepartmentRepository.findByDepartmentTypeId(opdId);
            if (opdDepartments == null || opdDepartments.isEmpty()) {
                log.info("No OPD departments found for departmentTypeId = 5");
                return ResponseUtils.createSuccessResponse(
                        Collections.emptyList(),
                        new TypeReference<>() {}
                );
            }

            // Filter by specialityId (if provided)
            if (specialityId != null) {
                opdDepartments = opdDepartments.stream().filter(d -> d.getId().equals(specialityId)).toList();
                log.debug("Filtered OPD departments by specialityId={}", specialityId);
            }

            if (opdDepartments.isEmpty()) {
                log.info("No department found for specialityId={}", specialityId);
                return ResponseUtils.createSuccessResponse(
                        Collections.emptyList(),
                        new TypeReference<>() {}
                );
            }

            List<Long> deptIds = opdDepartments.stream()
                    .map(MasDepartment::getId)
                    .toList();

            //  UserDepartment → User → Employee
            List<UserDepartment> userDepartments = userDepartmentRepository.findByDepartmentIds(deptIds);

            //  Grouping Response
            Map<Long, SpecialityResponse> responseMap = new LinkedHashMap<>();

            for (UserDepartment ud : userDepartments) {
                User user = ud.getUser();
                MasEmployee emp = user != null ? user.getEmployee() : null;

//               (role_id = 3)
                if (emp == null
                        || emp.getRoleId() == null
                        || !emp.getRoleId().getId().equals(3L)) {
                    continue;
                }

                MasDepartment dept = ud.getDepartment();

               // MasEmployee emp = ud.getUser().getEmployee();
               // User user=ud.getUser();

                // Speciality
                SpecialityResponse speciality =
                        responseMap.computeIfAbsent(dept.getId(), k -> {
                            SpecialityResponse s = new SpecialityResponse();
                            s.setSpecialityId(dept.getId());
                            s.setSpecialityName(dept.getDepartmentName());
                            s.setHospitalId(dept.getHospital().getId());
                            s.setHospitalName(dept.getHospital().getHospitalName());
                            s.setDoctorResponseListList(new ArrayList<>());
                            return s;
                        });

                // Doctor
                DoctorResponseList doctor = new DoctorResponseList();
               // User user=userRepo.findByEmployee_EmployeeId(emp.getEmployeeId());
                doctor.setDoctorId(user.getUserId());
                doctor.setDoctorName(emp.getFirstName() + " " + (emp.getMiddleName() != null ? emp.getMiddleName() + " " : "") + emp.getLastName());
                doctor.setSpecialityName(dept.getDepartmentName());
                doctor.setGender(emp.getGenderId() != null ? emp.getGenderId().getGenderName() : null);
                doctor.setPhoneNo(emp.getMobileNo());
                doctor.setAge(emp.getAge() != null ? emp.getAge().toString() : null);
                doctor.setYearsOfExperience(emp.getYearOfExperience());
                List<Object[]> rows = appSetupRepository.findDistinctDoctorSessionNextDay(user.getUserId());
                List<SessionResponseList> sessionList = rows.stream().map(r -> {
                    SessionResponseList s = new SessionResponseList();
                    s.setSessionId(((Number) r[1]).longValue());
                    s.setDay((String) r[2]);
                    s.setStartTime((String) r[3]);
                    s.setEndTime((String) r[4]);
                    return s;
                }).toList();

                doctor.setSessionResponseLists(sessionList);
                Optional<MasServiceOpd> masServiceOpd=masServiceOpdRepository.findByDoctorId_UserId(user!=null?user.getUserId():null);
                doctor.setConsultancyFee(masServiceOpd.map(MasServiceOpd::getBaseTariff).orElse(null));
                speciality.getDoctorResponseListList().add(doctor);

            }

            List<SpecialityResponse> response = new ArrayList<>(responseMap.values());

            log.info("Successfully fetched {} specialities", response.size());
            return ResponseUtils.createSuccessResponse(
                    response,
                    new TypeReference<>() {}
            );

        } catch (Exception ex) {
            log.error("Error while fetching OPD Speciality and Doctor list", ex);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    public ApiResponse<DoctorDetailResponse> getDoctor(Long doctorId) {
        log.info("Fetching doctor details for doctorId={}", doctorId);
        try {
            //Employee


            // User using employeeId
            Optional<User> optionalUser = userRepo.findById(doctorId);
           // MasEmployee emp = masEmployeeRepository.findById().orElseThrow(() -> new RuntimeException("Doctor not found"));

            List<SpecialitiesResponse> specialitiesList = new ArrayList<>();
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                // UserDepartment using userId
                List<UserDepartment> userDepartments = userDepartmentRepository.findByUserUserId(user.getUserId());
                for (UserDepartment ud : userDepartments) {
                    MasDepartment dept = ud.getDepartment();
                    SpecialitiesResponse sr = new SpecialitiesResponse();
                    sr.setSpecialityId(dept!=null?dept.getId():null);
                    sr.setSpecialityName(dept!=null?dept.getDepartmentName():null);
                    specialitiesList.add(sr);
                }
            }

            // Response
            DoctorDetailResponse doctor = new DoctorDetailResponse();
            doctor.setDoctorId( optionalUser.get().getUserId());
            List<AppSetup> appSetups =
                    appSetupRepository.findByDoctorId_UserId(optionalUser.get().getUserId());

            List<SessionResponse> appSetResponseList = appSetups.stream()
                    .filter(a -> a.getStartTime() != null && !a.getStartTime().isBlank()
                            && a.getEndTime()   != null && !a.getEndTime().isBlank())
                    .map(appSetup -> {
                        SessionResponse resp = new SessionResponse();
                        resp.setMinDay(appSetup.getMinNoOfDays());
                        resp.setMaxDay(appSetup.getMaxNoOfDays());
                        resp.setStartTime(appSetup.getStartTime());
                        resp.setEndTime(appSetup.getEndTime());
                        resp.setSessionId(appSetup.getSession() != null ? appSetup.getSession().getId() : null);
                        resp.setDay(appSetup.getDays());
                        return resp;
                    })
                    .toList();

            doctor.setSessionResponseList(appSetResponseList);
//            List<Object[]> rows = appSetupRepository.findDistinctDoctorSession(optionalUser.get().getUserId());
//            List<SessionResponseList> sessionList1 = rows.stream().map(r -> {
//                SessionResponseList s = new SessionResponseList();
//                s.setSessionId(((Number) r[1]).longValue());
//                s.setStartTime((String) r[2]);
//                s.setEndTime((String) r[3]);
//
//                return s;
//            }).toList();
//
//            doctor.setSessionResponseLists(sessionList1);

            BasicInfo basicInfo=new BasicInfo();
            basicInfo.setDoctorName(optionalUser.get().getEmployee().getFirstName()+ " " + (optionalUser.get().getEmployee().getMiddleName() != null ? optionalUser.get().getEmployee().getMiddleName() + " " : "") + optionalUser.get().getEmployee().getLastName());
            basicInfo.setGender(optionalUser.get().getEmployee().getGenderId() != null ? optionalUser.get().getEmployee().getGenderId().getGenderName() : null);
            basicInfo.setPhoneNo(optionalUser.get().getEmployee().getMobileNo());
            basicInfo.setAge(optionalUser.get().getEmployee().getAge() != null ? optionalUser.get().getEmployee().getAge().toString() : null);
            basicInfo.setYearsOfExperience(optionalUser.get().getEmployee().getYearOfExperience());
            basicInfo.setProfileDescription(optionalUser.get().getEmployee().getProfileDescription());
            User user=userRepo.findByEmployee_EmployeeId(optionalUser.get().getEmployee().getEmployeeId());
            Optional<MasServiceOpd> masServiceOpd=masServiceOpdRepository.findByDoctorId_UserId(user.getUserId());
            basicInfo.setConsultancyFee(masServiceOpd.map(MasServiceOpd::getBaseTariff).orElse(null));
            doctor.setBasicInfo( basicInfo);
            doctor.setHospitalName(optionalUser.map(User::getHospital).map(MasHospital::getHospitalName).orElse(null));
            doctor.setEducation(employeeQualificationRepository
                            .findById(optionalUser.get().getEmployee().getEmployeeId())
                            .stream()
                            .map(eq -> {
                                StringBuilder sb = new StringBuilder();
                                // Qualification
                                sb.append(eq.getQualificationName());
                                // College
                                if (eq.getInstitutionName() != null) {
                                    sb.append(" - ").append(eq.getInstitutionName());
                                }
                                // Year
                                if (eq.getCompletionYear()!= null) {
                                    sb.append(" (").append(eq.getCompletionYear()).append(")");
                                }
                                return sb.toString();
                            })
                            .toList()
            );
            List<MasOpdSession> sessions = masOpdSessionRepository.findByStatusIgnoreCase("y");
            List<SessionResponseList> sessionList = sessions.stream().map(session -> {
                SessionResponseList s = new SessionResponseList();
                s.setSessionId(session.getId());
                s.setStartTime(session.getFromTime().toString());
                s.setEndTime(session.getEndTime().toString());
                return s;
            }).toList();
           // doctor.setSessionResponseLists(sessionList);
            doctor.setWorkExperience(
                    employeeWorkExperienceRepository.findByEmployee(optionalUser.get().getEmployee())
                            .stream()
                            .map(EmployeeWorkExperience::getExperienceSummary)
                            .toList()
            );
            doctor.setSpecialtyInterests(
                    employeeSpecialtyInterestRepository.findByEmployee(optionalUser.get().getEmployee())
                            .stream()
                            .map(EmployeeSpecialtyInterest::getInterestSummary)
                            .toList()
            );
            doctor.setMemberships(
                    employeeMembershipRepository.findByEmployee(optionalUser.get().getEmployee())
                            .stream()
                            .map(EmployeeMembership::getMembershipSummary)
                            .toList()
            );
            doctor.setAwardsAndDistinctions(
                    employeeAwardRepository.findByEmployee(optionalUser.get().getEmployee())
                            .stream()
                            .map(EmployeeAward::getAwardSummary)
                            .toList()
            );

            Long empId = optionalUser.get().getEmployee().getEmployeeId();
            List<Long> langIds = masEmployeeLanguageMappingRepository.findByEmpId(empId)
                    .stream()
                    .map(MasEmployeeLanguageMapping::getLanguageId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();

            List<String> langNames = langIds.isEmpty()
                    ? List.of()
                    : masLanguageRepository.findAllById(langIds)
                    .stream()
                    .map(MasLanguage::getLanguageName)
                    .filter(Objects::nonNull)
                    .toList();

            doctor.setLanguages(langNames);
            doctor.setSpecialitiesResponseList(specialitiesList);
            return ResponseUtils.createSuccessResponse(doctor, new TypeReference<>() {}
            );

        } catch (Exception ex) {
            log.error("Error while fetching doctor details for doctorId={}", doctorId, ex);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }


    }

    @Override
    public ApiResponse<List<AppointmentBookingHistoryResponseDetails>> appointmentHistory(Long hospitalId, Long patientId, String mobileNo) {
        try {
            String normalizedMobileNo = (mobileNo == null) ? null : mobileNo.trim();

            Instant startOfToday = LocalDate.now()
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant();

            List<Visit> visits;

            // Case 1: hospitalId + patientId => status y,c,n
            if (patientId != null) {
                visits = visitRepository.findHistoryByHospitalAndPatient(
                        hospitalId, patientId
                );
            }
            // Case 2: only mobileNo => status n AND from today onwards
            else {
                // If hospitalId is mandatory for mobile search, you can validate here.
                visits = visitRepository.findUpcomingByHospitalAndMobile(
                        hospitalId, startOfToday, normalizedMobileNo
                );
            }
            List<AppointmentBookingHistoryResponseDetails> response = visits.stream()
                    .sorted(Comparator.comparing(Visit::getVisitDate))
                    .map(this::mapToDto)
                    .toList();

            return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {}, ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }


    @Override
    public ApiResponse<List<AppointmentBookingHistoryResponseDetails>>  appointmentHistory() {
        try {
            Instant startOfToday = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();

            List<Visit> visits = visitRepository.findByVisitStatusIgnoreCase("n");

            List<AppointmentBookingHistoryResponseDetails> response = visits.stream()
                    .filter(v -> v.getVisitDate() != null && !v.getVisitDate().isBefore(startOfToday))
                    .sorted(Comparator.comparing(Visit::getVisitDate))
                    .map(this::mapToDto)
                    .toList();

            return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});

        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }




    private AppointmentBookingHistoryResponseDetails mapToDto(Visit v) {
        AppointmentBookingHistoryResponseDetails dto = new AppointmentBookingHistoryResponseDetails();
        dto.setVisitId(v.getId());
        dto.setPatientId(v.getPatient()!= null?v.getPatient().getId():null);
        dto.setPatientName(v.getPatient().getPatientFn()+" "+v.getPatient().getPatientMn()+" "+v.getPatient().getPatientLn());
        dto.setMobileNumber(v.getPatient().getPatientMobileNumber());
        dto.setPatientAge(v.getPatient().getPatientAge());
        dto.setDoctorId(v.getDoctor()!=null?v.getDoctor().getUserId():null);
        dto.setDoctorName(v.getDoctorName());
        dto.setDepartmentId(v.getDepartment().getId());
        dto.setDepartmentName(v.getDepartment() != null ? v.getDepartment().getDepartmentName() : null);
        dto.setAppointmentDate(v.getVisitDate());
        dto.setAppointmentStartTime(v.getStartTime());
        dto.setAppointmentEndTime(v.getEndTime());
        dto.setVisitStatus(v.getVisitStatus());
        dto.setReason(v.getReason()!=null? v.getReason().getReasonName():null);
        return dto;
    }




    @Transactional(rollbackFor = Exception.class)
    @Override
    public ApiResponse<MasEmployee> createEmployee(MasEmployeeRequest req) {
        log.debug("Creating new Employee");
        Map<String, String> errors = validateEmployeeRequest(req);
        if (!errors.isEmpty()) {
            throw new RuntimeException(errors.values().iterator().next());
        }
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Current user not found");
        }
        MasEmployee employee = buildEmployeeFromRequest(req, currentUser);
        MasEmployee savedEmployee = masEmployeeRepository.save(employee);
        processQualifications(req.getQualification(), savedEmployee, currentUser);
        processSpecialtyCenter(req.getSpecialtyCenter(), savedEmployee, currentUser);
        processLanguages(req.getLanguages(), savedEmployee, currentUser);
        processWorkExperiences(req.getWorkExperiences(), savedEmployee, currentUser);
        processMemberships(req.getEmployeeMemberships(), savedEmployee, currentUser);
        processSpecialtyInterest(req.getEmployeeSpecialtyInterests(), savedEmployee, currentUser);
        processAwards(req.getEmployeeAwards(), savedEmployee, currentUser);
        processDocuments(req.getDocument(), savedEmployee, currentUser);

        log.info("Employee created with ID {}", savedEmployee.getEmployeeId());

        return ResponseUtils.createSuccessResponse(savedEmployee, new TypeReference<>() {});
    }

    private Map<String, String> validateEmployeeRequest(MasEmployeeRequest request) throws RuntimeException {
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
        validateField(errors, request.getEmployeeTypeId(), "employeeTypeId", "Employee type cannot be blank");
        validateField(errors, request.getEmploymentTypeId(), "employmentTypeId", "Employment type cannot be blank");
        validateField(errors, request.getRoleId(), "roleId", "Role cannot be blank");
        validateField(errors, request.getYearOfExperience(), "yearOfExperience", "Experience cannot be blank");
        validateField(errors, request.getFromDate(), "fromDate", "From date cannot be blank");
        validateField(errors, request.getMasDesignationId(),"designation","designation cannot be blank");

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

    private MasEmployee buildEmployeeFromRequest(MasEmployeeRequest request, User currentUser){

        Optional<MasEmployee> existingEmp = masEmployeeRepository.findByMobileNo(request.getMobileNo());
        if (existingEmp.isPresent()) {
            throw new EntityExistsException("Mobile number " + request.getMobileNo() + " already exists with Emp: " + existingEmp.get().getFirstName() + " " + existingEmp.get().getMiddleName() + " " + existingEmp.get().getLastName());
        }


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

        MasDesignation masDesignation = masDesignationRepository.findById(request.getMasDesignationId())
                .orElseThrow(() -> new EntityNotFoundException("Designation not found with ID: " + request.getIdentificationType()));


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
        employee.setProfileDescription(request.getProfileDescription());
        employee.setMasDesignationId(masDesignation);
        employee.setYearOfExperience(request.getYearOfExperience());
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
    private void processQualifications(List<EmployeeQualificationReq> qualifications, MasEmployee employee, User currentUser) {

        if (qualifications == null || qualifications.isEmpty()) {
            throw new QualificationProcessingException("Qualifications cannot be empty");
        }

        Path uploadDirPath = Paths.get(uploadDir, "MAS_EMPLOYEE");
        try {
            Files.createDirectories(uploadDirPath);
        } catch (IOException e) {
            throw new QualificationProcessingException("Unable to create upload directory");
        }

        for (EmployeeQualificationReq q : qualifications) {
            try {
                if (q.getFilePath() == null || q.getFilePath().isEmpty()) {
                    throw new QualificationProcessingException("Qualification file is missing");
                }

                String original = q.getFilePath().getOriginalFilename();
                if (original == null || original.isBlank()) {
                    throw new QualificationProcessingException("Invalid qualification file name");
                }

                String ext = getFileExtension(original);
                if (!isValidDocExtension(ext)) {
                    throw new QualificationProcessingException(
                            "Invalid qualification file type. Only PDF, JPG, JPEG and PNG are allowed"
                    );
                }

                String cleanName = original.trim().replaceAll("\\s+", "_");
                if (!cleanName.matches("^\\d+_.*")) {
                    cleanName = System.currentTimeMillis() + "_" + cleanName;
                }

                Path filePath = uploadDirPath.resolve(cleanName);
                Files.write(filePath, q.getFilePath().getBytes());

                EmployeeQualification eq = new EmployeeQualification();
                eq.setEmployee(employee);
                eq.setQualificationName(q.getQualificationName());
                eq.setCompletionYear(q.getCompletionYear());
                eq.setInstitutionName(q.getInstitutionName());
                eq.setFilePath(filePath.toString().replace("\\", "/"));
                eq.setLastChangedBy(currentUser.getUserId().toString());
                eq.setLastChangedDate(OffsetDateTime.now().toLocalDateTime());

                employeeQualificationRepository.save(eq);

            } catch (IOException e) {
                throw new QualificationProcessingException("Failed to upload qualification file");
            }
        }
    }
    private void processSpecialtyCenter(List<EmployeeSpecialtyCenterRequest> specialtyCenters, MasEmployee savedEmployee, User currentUser){
        if (specialtyCenters == null || specialtyCenters.isEmpty()) {
            return;
        }
        for (EmployeeSpecialtyCenterRequest request : specialtyCenters) {
            try {
                MasEmployeeCenterMapping masEmployeeCenterMapping = new MasEmployeeCenterMapping();
                masEmployeeCenterMapping.setEmpId(savedEmployee.getEmployeeId());
                masEmployeeCenterMapping.setCenterId(request.getCenterId());
                masEmployeeCenterMapping.setIsPrimary(false);
                masEmployeeCenterMapping.setLastUpdateDate(Instant.now());
                employeeSpecialtyCenterRepository.save(masEmployeeCenterMapping);
            } catch (Exception e) {
                throw new SpecialtyCenterProcessingException(
                        "Failed to process specialty center: " + request, e);
            }
        }
    }
    private void processLanguages(List<EmployeeLanguageRequest> languageRequests,MasEmployee savedEmployee, User currentUser) {
        if (languageRequests == null || languageRequests.isEmpty()) {
            log.debug("No languages provided for employee creation");
            return;
        }

        for (EmployeeLanguageRequest languageReq : languageRequests) {
            if (languageReq.getLanguageId() == null) {
                continue;
            }

            try {
                MasEmployeeLanguageMapping mapping = MasEmployeeLanguageMapping.builder()
                        .empId(savedEmployee.getEmployeeId())
                        .languageId(languageReq.getLanguageId())
                        .lastChgBy(currentUser.getUserId())
                        .build();

                masEmployeeLanguageMappingRepository.save(mapping);
                log.debug("Created language mapping: employeeId={}, languageId={}",
                        savedEmployee.getEmployeeId(), languageReq.getLanguageId());

            } catch (Exception e) {
                log.error("Failed to create language mapping for languageId: {}", languageReq.getLanguageId(), e);
                throw new RuntimeException("Failed to process language: " + languageReq.getLanguageId(), e);
            }
        }
    }    private void processWorkExperiences(List<EmployeeWorkExperienceRequest> workExperiences, MasEmployee savedEmployee, User currentUser){
        if (workExperiences == null || workExperiences.isEmpty()) {
            return;
        }
        for (EmployeeWorkExperienceRequest request : workExperiences) {
            try {
                EmployeeWorkExperience workExperience = new EmployeeWorkExperience();
                workExperience.setEmployee(savedEmployee);
                workExperience.setExperienceSummary(request.getExperienceSummary());
                workExperience.setOrderLevel(1);
                workExperience.setLastUpdateDate(Instant.now());
                employeeWorkExperienceRepository.save(workExperience);
            } catch (Exception e) {
                throw new WorkExperienceProcessingException(
                        "Failed to process work experience: " + request, e);
            }
        }
    }
    private void processMemberships(List<EmployeeMembershipRequest> memberships, MasEmployee savedEmployee, User currentUser){
        if (memberships == null || memberships.isEmpty()) {
            return;
        }
        for (EmployeeMembershipRequest request : memberships) {
            try {
                EmployeeMembership membership = new EmployeeMembership();
                membership.setEmployee(savedEmployee);
                membership.setMembershipSummary(request.getMembershipSummary());
                membership.setLastUpdateDate(Instant.now());
                membership.setOrderLevel(1);
                employeeMembershipRepository.save(membership);
            } catch (Exception e) {
                throw new MembershipProcessingException(
                        "Failed to process membership: " + request.getMembershipSummary(), e);
            }
        }
    }
    private void processSpecialtyInterest(List<EmployeeSpecialtyInterestRequest> specialtyInterests, MasEmployee savedEmployee, User currentUser){
        if (specialtyInterests == null || specialtyInterests.isEmpty()) {
            return;
        }
        for (EmployeeSpecialtyInterestRequest request : specialtyInterests) {
            try {
                EmployeeSpecialtyInterest specialtyInterest = new EmployeeSpecialtyInterest();
                specialtyInterest.setEmployee(savedEmployee);
                specialtyInterest.setInterestSummary(request.getInterestSummary());
                specialtyInterest.setLastUpdateDate(Instant.now());
                employeeSpecialtyInterestRepository.save(specialtyInterest);
            } catch (Exception e) {
                throw new SpecialtyInterestProcessingException(
                        "Failed to process specialty interest: " + request, e);
            }
        }
    }
    private void processAwards(List<EmployeeAwardRequest> awards, MasEmployee savedEmployee, User currentUser){
        if (awards == null || awards.isEmpty()) {
            return;
        }
        for (EmployeeAwardRequest request : awards) {
            try {
                EmployeeAward award = new EmployeeAward();
                award.setEmployee(savedEmployee);
                award.setAwardSummary(request.getAwardSummary());
                award.setLastUpdateDate(Instant.now());
                employeeAwardRepository.save(award);
            } catch (Exception e) {
                throw new AwardProcessingException(
                        "Failed to process award: " + request, e);
            }
        }
    }
    private void processDocuments(List<EmployeeDocumentReq> documents, MasEmployee employee, User currentUser) {
        if (documents == null || documents.isEmpty()) {
            throw new DocumentProcessingException("Documents cannot be empty");
        }
        Path uploadDirPath = Paths.get(uploadDir, "MAS_EMPLOYEE");
        try {
            Files.createDirectories(uploadDirPath);
        } catch (IOException e) {
            throw new DocumentProcessingException("Unable to create upload directory");
        }
        for (EmployeeDocumentReq d : documents) {
            try {
                if (d.getFilePath() == null || d.getFilePath().isEmpty()) {
                    throw new DocumentProcessingException("Document file is missing");
                }
                String original = d.getFilePath().getOriginalFilename();
                if (original == null || original.isBlank()) {
                    throw new DocumentProcessingException("Invalid document file name");
                }

                String ext = getFileExtension(original);
                if (!isValidDocExtension(ext)) {
                    throw new DocumentProcessingException(
                            "Employee document invalid file type. Only PDF, JPG, JPEG and PNG are allowed"
                    );
                }

                String cleanName = original.trim().replaceAll("\\s+", "_");
                if (!cleanName.matches("^\\d+_.*")) {
                    cleanName = System.currentTimeMillis() + "_" + cleanName;
                }

                Path filePath = uploadDirPath.resolve(cleanName);
                Files.write(filePath, d.getFilePath().getBytes());

                EmployeeDocument doc = new EmployeeDocument();
                doc.setEmployee(employee);
                doc.setDocumentName(d.getDocumentName());
                doc.setFilePath(filePath.toString().replace("\\", "/"));
                doc.setLastChangedBy(currentUser.getUserId().toString());
                doc.setLastChangedDate(OffsetDateTime.now().toLocalDateTime());

                employeeDocumentRepository.save(doc);

            } catch (IOException e) {
                throw new DocumentProcessingException("Failed to upload document file");
            }
        }
    }


    public static class FileProcessingException extends RuntimeException {
        public FileProcessingException(String message) {
            super(message);
        }
        public FileProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    public static class QualificationProcessingException extends RuntimeException {
        public QualificationProcessingException(String message) {
            super(message);
        }
        public QualificationProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    public static class WorkExperienceProcessingException extends RuntimeException {
        public WorkExperienceProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    public static class MembershipProcessingException extends RuntimeException {
        public MembershipProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    public static class SpecialtyInterestProcessingException extends RuntimeException {
        public SpecialtyInterestProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    public static class AwardProcessingException extends RuntimeException {
        public AwardProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    public static class SpecialtyCenterProcessingException extends RuntimeException {
        public SpecialtyCenterProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    public static class LanguageProcessingException extends RuntimeException {
        public LanguageProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    public static class DocumentProcessingException extends RuntimeException {
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