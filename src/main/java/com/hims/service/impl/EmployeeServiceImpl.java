package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.helperUtil.HelperUtils;
import com.hims.projection.AppointmentHistoryProjection;
import com.hims.request.*;
import com.hims.response.*;
import com.hims.service.EmployeeService;
import com.hims.utils.ResponseUtils;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class EmployeeServiceImpl implements EmployeeService {

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
    private Long opdDepartmentTypeId;

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
    }

    private void processWorkExperiences(List<EmployeeWorkExperienceRequest> workExperiences, MasEmployee savedEmployee, User currentUser){
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
