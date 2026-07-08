package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.exception.SDDException;
import com.hims.helperUtil.HelperUtils;
import com.hims.request.*;
import com.hims.response.*;
import com.hims.service.BillingService;
import com.hims.service.LabRegistrationServices;
import com.hims.utils.AuthUtil;
import com.hims.utils.RandomNumGenerator;
import com.hims.utils.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.hims.helperUtil.ConverterUtils.ageCalculator;

@Service

public class LabRegistrationServicesImpl implements LabRegistrationServices {
    private static final Logger log = LoggerFactory.getLogger(LabRegistrationServicesImpl.class);
    @Autowired
    LabHdRepository labHdRepository;
    @Autowired
    DgMasInvestigationRepository investigation;
    @Autowired
    LabDtRepository labDtRepository;
    @Autowired
    PackageInvestigationMappingRepository packageInvestigationMappingRepository;
    @Autowired
    DgInvestigationPackageRepository dgInvestigationPackageRepository;
    @Autowired
    PatientRepository patientRepository;
    @Autowired
    VisitRepository visitRepository;
    @Autowired
    private MasSubChargeCodeRepository masSubChargeCodeRepository;
    @Autowired
    UserRepo userRepo;
    @Autowired
    MasHospitalRepository masHospitalRepository;
    private final RandomNumGenerator randomNumGenerator;
    @Autowired
    BillingHeaderRepository billingHeaderRepository;
    @Autowired
    AuthUtil authUtil;
    @Autowired
    UserDepartmentRepository userDepartmentRepository;
    private final BillingDetailRepository billingDetailRepository;
    @Autowired
    MasDepartmentRepository masDepartmentRepository;
    @Autowired
    private DgMasInvestigationRepository dgMasInvestigationRepository;

    @Autowired
    MasServiceCategoryRepository masServiceCategoryRepository;
    @Autowired
    PaymentDetailRepository paymentDetailRepository;
    @Value("${serviceCategoryLab}")
    private String serviceCategoryLab;
    @Autowired
    private DgSampleCollectionHeaderRepository dgSampleCollectionHeaderRepository;
    @Autowired
    private DgSampleCollectionDetailsRepository dgSampleCollectionDetailsRepository;
    @Autowired
    private DgMasSampleRepository dgMasSampleRepository;
    @Autowired
    private DgMasCollectionRepository dgMasCollectionRepository;
    @Autowired
    private MasMainChargeCodeRepository masMainChargeCodeRepository;
    @Autowired
    private LabTurnAroundTimeRepository labTurnAroundTimeRepository;

    @Autowired
    private LabOrderTrackingStatusRepository labOrderTrackingStatusRepository;

    @Autowired
    MasGenderRepository masGenderRepository;
    @Autowired
    MasRelationRepository masRelationRepository;

    @Autowired
    PatientServiceImpl patientService;

    @Autowired
    BillingService billingService;

    @Autowired
    HelperUtils helperUtils;

    @Value("${app.laboratoryDepartment}")
    private Long laboratoryDepartment;

    @Value("${lab.track-order-status-reg.ordered}")
    private Long orderedStatusId;

    @Value("${lab.track-order-status-sample.collect}")
    private Long collectedStatusId;

    @Value("${sample.collection.display.days}")
    private int pendingDays;

    private MasServiceCategory cachedServiceCategory;
    private LabOrderTrackingStatus cachedOrderedStatus;


    private LabOrderTrackingStatus getOrderedStatus() {
        if (cachedOrderedStatus == null) {
                cachedOrderedStatus = labOrderTrackingStatusRepository.findById(orderedStatusId)
                        .orElseThrow(() -> new SDDException("status", 500,
                                "Ordered status not found with id: " + orderedStatusId));
        }
        return cachedOrderedStatus;
    }


    private String getCurrentTimeFormatted(Instant instant) {
        LocalTime time = instant
                .atZone(ZoneId.systemDefault())
                .toLocalTime();

        return time.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }


    /**
     * Create order detail entity for investigation
     */
    private DgOrderDt createOrderDetailForInvestigation(
            DgOrderHd savedHd,
            DgMasInvestigation invEntity,
            LabRadioInvestigationRequest inv,
            User currentUser,
            LocalDate today) {
        DgOrderDt dt = new DgOrderDt();
        dt.setInvestigationId(invEntity);
        dt.setOrderhdId(savedHd);
        dt.setAppointmentDate(inv.getAppointmentDate());
        dt.setOrderQty(1);
        dt.setOrderStatus(AppConstants.STATUS_N.toLowerCase());
        dt.setBillingStatus(AppConstants.PAYMENT_NOT_PAID.toLowerCase());
        dt.setCreatedBy(currentUser.getFullName());
        dt.setLastChgBy(currentUser.getFullName());
        dt.setLastChgDate(today);
        dt.setMainChargecodeId(invEntity.getMainChargeCodeId().getChargecodeId());
        dt.setSubChargeid(invEntity.getSubChargeCodeId().getSubId());
        dt.setOrderTrackingStatus(getOrderedStatus());
        dt.setCreatedon(Instant.now());
        dt.setLastChgTime(LocalTime.now().toString());

        return dt;
    }

    /**
     * Calculate billing amounts for a set of investigations
     */
    private BigDecimal[] calculateBillingAmounts(List<? extends Object> investigations) {
        BigDecimal sum = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.ZERO;
        BigDecimal disc = BigDecimal.ZERO;

        if (cachedServiceCategory == null) {
            log.warn("Service category not cached, fetching from DB");
            cachedServiceCategory = masServiceCategoryRepository.findByServiceCateCode(serviceCategoryLab);
        }

        for (Object inv : investigations) {
            double actualAmount = 0;
            double discountedAmount = 0;

            // Handle both LabInvestigationReq and LabRadioInvestigationRequest
            if (inv instanceof LabRadioInvestigationRequest) {
                actualAmount = ((LabRadioInvestigationRequest) inv).getActualAmount();
                discountedAmount = ((LabRadioInvestigationRequest) inv).getDiscountedAmount();
            } else if (inv instanceof LabInvestigationReq) {
                actualAmount = ((LabInvestigationReq) inv).getActualAmount();
                discountedAmount = ((LabInvestigationReq) inv).getDiscountedAmount();
            }

            sum = sum.add(BigDecimal.valueOf(actualAmount));
            disc = disc.add(BigDecimal.valueOf(discountedAmount));

            if (cachedServiceCategory != null && cachedServiceCategory.getGstApplicable()) {
                tax = tax.add(BigDecimal.valueOf(cachedServiceCategory.getGstPercent())
                        .multiply(BigDecimal.valueOf(actualAmount)
                                .subtract(BigDecimal.valueOf(discountedAmount)))
                        .divide(BigDecimal.valueOf(100)));
            }
        }

        return new BigDecimal[]{sum, tax, disc};
    }


    public LabRegistrationServicesImpl(RandomNumGenerator randomNumGenerator, BillingDetailRepository billingDetailRepository) {
        this.randomNumGenerator = randomNumGenerator;
        this.billingDetailRepository = billingDetailRepository;
    }


    public String createInvoices() {
        return randomNumGenerator.generateOrderNumber("BILL", true, true);
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public ApiResponse<LabRadiologyRegistrationResponse> registerAndBookingLaboratory(
            LabRadioRegistrationRequest registrationRequest) {

        log.info("Starting lab registration process");

        PatientRequest patient = registrationRequest.getPatient();
        if (patient == null) {
            throw new SDDException("patient", 400, "Patient data is required");
        }
        User currentUser = authUtil.getCurrentUser();

        Optional<Patient> existingPatient = patientRepository.findByUniqueCombination(
                patient.getPatientFn(),
                patient.getPatientLn(),
                masGenderRepository.findById(patient.getPatientGenderId())
                        .orElseThrow(() -> new SDDException("gender", 400, "Invalid gender")),
                patient.getPatientDob(),
                patient.getPatientAge(),
                patient.getPatientMobileNumber(),
                masRelationRepository.findById(patient.getPatientRelationId())
                        .orElseThrow(() -> new SDDException("relation", 400, "Invalid relation"))
        );

        if (existingPatient.isPresent()) {
            throw new SDDException("patient", 409, "Patient already registered");
        }

        try {
            Patient savedPatient = patientService.savePatient(patient, false);
            if (savedPatient == null) {
                throw new SDDException("patient", 500, "Failed to save patient");
            }

            Visit savedVisit = createVisitForLabRadio(savedPatient, laboratoryDepartment);

            List<LabRadioInvestigationRequest> invList = registrationRequest.getInvestigationReq();
            if (invList == null || invList.isEmpty()) {
                throw new SDDException("investigation", 400, "Investigation list cannot be empty");
            }

            invList.forEach(inv -> {
                if (inv.getAppointmentDate() == null) {
                    throw new SDDException("appointmentDate", 400,
                            "Appointment date required for investigationId: " + inv.getId());
                }
            });

            Map<LocalDate, List<LabRadioInvestigationRequest>> grouped =
                    invList.stream().collect(Collectors.groupingBy(LabRadioInvestigationRequest::getAppointmentDate));

            String currentUsername = currentUser.getFullName();
            LocalDate today = LocalDate.now();
            LocalTime now = LocalTime.now();

            LabRadiologyRegistrationResponse response = new LabRadiologyRegistrationResponse();
            response.setPatientId(savedPatient.getId());

            for (Map.Entry<LocalDate, List<LabRadioInvestigationRequest>> entry : grouped.entrySet()) {
                LocalDate date = entry.getKey();
                List<LabRadioInvestigationRequest> investigations = entry.getValue();
                BigDecimal[] amounts = calculateBillingAmounts(investigations);
                DgOrderHd savedHd = labHdRepository.save(buildOrderHd(
                        savedPatient, savedVisit, currentUser, date, today, now
                ));

                if (savedHd == null) {
                    throw new SDDException("order", 500, "Failed to create lab order");
                }

                BillingHeader billingHeader = billingService.saveBillingHeader(
                        savedHd, savedVisit, currentUser,
                        amounts[0], amounts[1], amounts[2],
                        serviceCategoryLab, false
                );

                if (billingHeader == null) {
                    throw new SDDException("billing", 500, "Failed to create billing");
                }

                savedVisit.setBillingHd(billingHeader);

                visitRepository.save(savedVisit);

                for (LabRadioInvestigationRequest inv : investigations) {

                    if (AppConstants.INVESTIGATION.equalsIgnoreCase(inv.getType())) {

                        DgMasInvestigation invEntity = investigation.findById(inv.getId())
                                .orElseThrow(() -> new SDDException("investigation", 400,
                                        "Invalid investigation ID: " + inv.getId()));

                        DgOrderDt dt = createOrderDetailForInvestigation(savedHd, invEntity, inv, currentUser, today);
                        DgOrderDt savedDt = labDtRepository.save(dt);

                        savedDt.setBillingHd(billingHeader);
                        labDtRepository.save(savedDt);

                        billingService.saveBillingDetail(billingHeader, savedDt, inv, serviceCategoryLab, false);

                    } else if (AppConstants.STATUS_P.equalsIgnoreCase(inv.getType())) {

                        DgInvestigationPackage pkgObj = dgInvestigationPackageRepository.findById(inv.getId())
                                .orElseThrow(() -> new SDDException("package", 400,
                                        "Invalid package ID: " + inv.getId()));

                        List<PackageInvestigationMapping> mappings =
                                packageInvestigationMappingRepository.findByPackageId(pkgObj);

                        for (PackageInvestigationMapping map : mappings) {
                            DgMasInvestigation investId = map.getInvestId();

                            DgOrderDt dt = buildOrderDetailForPackage(
                                    savedHd,
                                    investId,
                                    pkgObj,
                                    inv,
                                    currentUsername,
                                    today,
                                    now
                            );

                            DgOrderDt savedDt = labDtRepository.save(dt);
                            savedDt.setBillingHd(billingHeader);
                            labDtRepository.save(savedDt);
                        }

                        billingService.saveBillingDetailPackage(billingHeader, pkgObj, inv, serviceCategoryLab);

                    } else {
                        throw new SDDException("Investigation type", 400, "Invalid investigation type");
                    }
                }

                response.setBillinghdId(billingHeader.getId());
            }

            response.setMsg("Success");
            return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});

        } catch (SDDException e) {
            log.error("Business error: {}", e.getMessage());
            throw e; // rollback

        } catch (Exception e) {
            log.error("Unexpected error", e);
            throw new SDDException("system", 500, "Error while processing lab booking");
        }
    }

    public Visit createVisitForLabRadio(Patient patient,Long department) {
        User user = authUtil.getCurrentUser();
        MasHospital hospital = masHospitalRepository.findById(user.getHospital().getId()).orElseThrow(() -> new RuntimeException("Invalid hospital"));
        MasDepartment dept = masDepartmentRepository.findById(department).orElseThrow(() -> new RuntimeException("Invalid department"));
        Long token = visitRepository.countTokensForToday(hospital.getId(), dept.getId());
        Visit visit = new Visit();
        visit.setPatient(patient);
        visit.setVisitStatus(AppConstants.VISIT_STATUS_PENDING.toLowerCase());
        visit.setBillingStatus(AppConstants.PAYMENT_NOT_PAID.toLowerCase());
        visit.setHospital(hospital);
        visit.setTokenNo(token + 1);
        visit.setDepartment(dept);
        visit.setVisitDate(Instant.now());
        visit.setLastChgDate(Instant.now());
        visit.setDisplayPatientStatus(AppConstants.DISPLAY_PATIENT_STATUS);

        return visitRepository.save(visit);
    }

    private DgOrderHd buildOrderHd(Patient patient, Visit visit, User currentUser, LocalDate appointmentDate, LocalDate today, LocalTime now) {

        if (patient == null || visit == null || currentUser == null) {
            throw new SDDException("order", 400, "Invalid data for creating order header");
        }

        try {
            DgOrderHd hd = new DgOrderHd();

            hd.setAppointmentDate(appointmentDate);
            hd.setOrderDate(today);
            hd.setOrderTime(Instant.now());
            hd.setOrderNo(helperUtils.createInvoiceNumber());
            hd.setOrderStatus(AppConstants.STATUS_N.toLowerCase());
            hd.setCollectionStatus(AppConstants.STATUS_N.toLowerCase());
            hd.setPaymentStatus(AppConstants.PAYMENT_NOT_PAID.toLowerCase());
            hd.setHospitalId(currentUser.getHospital().getId());
            hd.setDepartmentId(visit.getDepartment().getId());
            hd.setPatientId(patient);
            hd.setVisitId(visit);
            hd.setSource("lab source");
            hd.setDiscountId(1);
            hd.setCreatedBy(currentUser.getFullName());
            hd.setLastChgBy(currentUser.getFullName());
            hd.setCreatedOn(today);
            hd.setLastChgDate(today);
            hd.setLastChgTime(now.toString());

            return hd;

        } catch (Exception e) {
            throw new SDDException("order", 500, "Error while building order header");
        }
    }

    private DgOrderDt buildOrderDetailForPackage(DgOrderHd hd, DgMasInvestigation invest, DgInvestigationPackage pkg, LabRadioInvestigationRequest inv, String userFullName, LocalDate today, LocalTime now) {

        if (hd == null || invest == null || pkg == null) {
            throw new SDDException("orderDetail", 400, "Invalid data for package order detail");
        }

        try {
            DgOrderDt dt = new DgOrderDt();

            dt.setOrderhdId(hd);
            dt.setInvestigationId(invest);

            dt.setMainChargecodeId(invest.getMainChargeCodeId().getChargecodeId());
            dt.setSubChargeid(invest.getSubChargeCodeId().getSubId());

            dt.setPackageId(pkg);
            dt.setAppointmentDate(inv.getAppointmentDate());

            dt.setOrderQty(1);

            dt.setOrderStatus(AppConstants.STATUS_N.toLowerCase());
            dt.setBillingStatus(AppConstants.PAYMENT_NOT_PAID.toLowerCase());
            dt.setOrderTrackingStatus(getOrderedStatus());

            dt.setCreatedBy(userFullName);
            dt.setLastChgBy(userFullName);

            dt.setCreatedon(Instant.now());
            dt.setLastChgDate(today);
            dt.setLastChgTime(now.toString());

            return dt;

        } catch (Exception e) {
            throw new SDDException("orderDetail", 500, "Error while creating package order detail");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ApiResponse<AppsetupResponse> updateDetailsAndBookingLaboratory(LabRadioUpdateRequest labReq) {

        log.info("Starting lab update + booking");

        User currentUser = authUtil.getCurrentUser();
        Long departmentId = laboratoryDepartment;

        if (currentUser == null) {
            throw new SDDException("user", 401, "Current user not found");
        }

        if (labReq == null || labReq.getPatient() == null || labReq.getPatient().getId() == null) {
            throw new SDDException("patient", 400, "Patient ID must not be null");
        }

        if (departmentId == null) {
            throw new SDDException("department", 400, "Department ID is required");
        }

        try {
            Patient patient = patientService.updatePatientDetails(labReq.getPatient(), true);
            if (patient == null) {
                throw new SDDException("patient", 500, "Failed to update patient");
            }
            Visit savedVisit = createVisitForLabRadio(patient, laboratoryDepartment);
            List<LabRadioInvestigationRequest> invList = labReq.getInvestigationReq();

            if (invList == null || invList.isEmpty()) {
                throw new SDDException("investigation", 400, "Investigation list cannot be empty");
            }

            invList.forEach(inv -> {
                if (inv.getAppointmentDate() == null) {
                    throw new SDDException("appointmentDate", 400,
                            "Appointment date required for investigationId: " + inv.getId());
                }
            });

            Map<LocalDate, List<LabRadioInvestigationRequest>> grouped =
                    invList.stream().collect(Collectors.groupingBy(LabRadioInvestigationRequest::getAppointmentDate));

            AppsetupResponse res = new AppsetupResponse();

            for (Map.Entry<LocalDate, List<LabRadioInvestigationRequest>> entry : grouped.entrySet()) {

                LocalDate date = entry.getKey();
                List<LabRadioInvestigationRequest> investigations = entry.getValue();

                MasServiceCategory servCat = masServiceCategoryRepository.findByServiceCateCode(serviceCategoryLab);

                BigDecimal sum = BigDecimal.ZERO;
                BigDecimal tax = BigDecimal.ZERO;
                BigDecimal disc = BigDecimal.ZERO;

                for (LabRadioInvestigationRequest inv : investigations) {
                    sum = sum.add(BigDecimal.valueOf(inv.getActualAmount()));
                    disc = disc.add(BigDecimal.valueOf(inv.getDiscountedAmount()));

                    if (servCat.getGstApplicable()) {
                        BigDecimal net = BigDecimal.valueOf(inv.getActualAmount())
                                .subtract(BigDecimal.valueOf(inv.getDiscountedAmount()));

                        tax = tax.add(net.multiply(BigDecimal.valueOf(servCat.getGstPercent()))
                                .divide(BigDecimal.valueOf(100)));
                    }
                }

                DgOrderHd savedHd = labHdRepository.save(
                        buildOrderHd(patient, savedVisit, currentUser,
                                date, LocalDate.now(), LocalTime.now())
                );

                if (savedHd == null) {
                    throw new SDDException("order", 500, "Failed to create order");
                }

                BillingHeader billingHeader = billingService.saveBillingHeader(
                        savedHd, savedVisit, currentUser, sum, tax, disc, serviceCategoryLab, false
                );

                if (billingHeader == null) {
                    throw new SDDException("billing", 500, "Failed to create billing");
                }

                savedVisit.setBillingHd(billingHeader);
                visitRepository.save(savedVisit);

                res.setBillinghdId(billingHeader.getId().toString());

                for (LabRadioInvestigationRequest inv : investigations) {

                    if ("i".equalsIgnoreCase(inv.getType())) {

                        DgMasInvestigation invEntity = investigation.findById(inv.getId())
                                .orElseThrow(() -> new SDDException("investigation", 400,
                                        "Invalid investigation ID: " + inv.getId()));

                        DgOrderDt dt = createOrderDetailForInvestigation(
                                savedHd, invEntity, inv, currentUser, LocalDate.now()
                        );

                        dt.setBillingHd(billingHeader);
                        labDtRepository.save(dt);

                        billingService.saveBillingDetail(billingHeader, dt, inv, serviceCategoryLab, false);

                    } else if (AppConstants.STATUS_P.equalsIgnoreCase(inv.getType())) {

                        DgInvestigationPackage pkg = dgInvestigationPackageRepository.findById(inv.getId())
                                .orElseThrow(() -> new SDDException("package", 400,
                                        "Invalid package ID: " + inv.getId()));

                        List<PackageInvestigationMapping> mappings =
                                packageInvestigationMappingRepository.findByPackageId(pkg);

                        for (PackageInvestigationMapping map : mappings) {

                            DgOrderDt dt = buildOrderDetailForPackage(
                                    savedHd,
                                    map.getInvestId(),
                                    pkg,
                                    inv,
                                    currentUser.getFullName(),
                                    LocalDate.now(),
                                    LocalTime.now()
                            );

                            dt.setBillingHd(billingHeader);
                            labDtRepository.save(dt);
                        }

                        billingService.saveBillingDetailPackage(billingHeader, pkg, inv, serviceCategoryLab);

                    } else {
                        throw new SDDException("type", 400, "Invalid investigation type");
                    }
                }
            }

            res.setMsg("Success");
            return ResponseUtils.createSuccessResponse(res, new TypeReference<>() {});

        } catch (SDDException e) {
            log.error("Business error: {}", e.getMessage());
            throw e;

        } catch (Exception e) {
            log.error("Unexpected error", e);
            throw new SDDException("system", 500, "Error while updating lab booking");
        }
    }


    private BillingHeader BillingHeaderDataSave(DgOrderHd hdId, Visit vId, LabRegRequest labReq, User currentUser, BigDecimal sum, BigDecimal tax, BigDecimal disc) {
        BillingHeader billingHeader = new BillingHeader();
        String orderNum = helperUtils.createInvoices();
        billingHeader.setBillNo(orderNum);// Auto generated
        billingHeader.setPatient(vId.getPatient());
        billingHeader.setVisit(vId);
        billingHeader.setPatientDisplayName(vId.getPatient().getPatientFn());
        LocalDate dob = vId.getPatient().getPatientDob();//get DOB from Patient table and calculate age
        billingHeader.setPatientAge(ageCalculator(dob));
        billingHeader.setPatientGender(vId.getPatient().getPatientGender().getGenderName());
        billingHeader.setPatientAddress(vId.getPatient().getPatientAddress1());
        billingHeader.setHospital(currentUser.getHospital());
        billingHeader.setHospitalName(vId.getPatient().getPatientHospital().getHospitalName());
        billingHeader.setHospitalAddress(vId.getHospital().getAddress());
        billingHeader.setHospitalMobileNo(vId.getHospital().getContactNumber());  //column is not exist in Patient table
        billingHeader.setHospitalGstin(vId.getHospital().getGstnNo());  //column is not exist in Patient table
        billingHeader.setServiceCategory(masServiceCategoryRepository.findByServiceCateCode(serviceCategoryLab));  ///for which table
        billingHeader.setReferredBy(vId.getDoctorName());//few doute
        billingHeader.setBillingDate(Instant.now());
        billingHeader.setPaymentStatus("n");
        billingHeader.setVisit(vId);
        billingHeader.setHdorder(hdId);
        // billingHeader.setBillingHdId(hdId.getId());
        billingHeader.setTotalAmount(sum);//.subtract(disc).add(tax)
        billingHeader.setDiscountAmount(disc);
        billingHeader.setNetAmount(sum.subtract(disc).add(tax));
        billingHeader.setTaxTotal(tax);
        //billingHeader.setDiscount();//id is Pass
        //billingHeader.setDiscountAmount(BigDecimal.valueOf(labReq.getDiscountAmount()));
        billingHeader.setCreatedBy(currentUser.getFirstName() + " " + currentUser.getLastName());
        billingHeader.setCreatedDt(Instant.now());
        billingHeader.setUpdatedDt(Instant.now());
        billingHeader.setBillDate(OffsetDateTime.now());
        billingHeader.setUpdatedAt(OffsetDateTime.now());
        return billingHeaderRepository.save(billingHeader);
    }
    private BillingDetail BillingDetaiDataSave(BillingHeader bhdId, DgOrderDt dtId, LabInvestigationReq investigation) {
        ///  Billing details
        BillingDetail billingDetail = new BillingDetail();
        billingDetail.setBillingHd(bhdId);
        billingDetail.setBillHd(bhdId);
        billingDetail.setServiceCategory(masServiceCategoryRepository.findByServiceCateCode(serviceCategoryLab));//pass from property file..

        billingDetail.setItemName(dtId.getInvestigationId().getInvestigationName());  // investigation or packeg  name to be store
        // billingDetail.setQuantity(1);//default
        billingDetail.setInvestigation(dtId.getInvestigationId());
        billingDetail.setPackageField(dtId.getPackageId());
        billingDetail.setCreatedDt(OffsetDateTime.now());
        billingDetail.setUpdatedDt(OffsetDateTime.now());
        billingDetail.setCreatedAt(Instant.now());
        billingDetail.setQuantity(1);
        billingDetail.setBasePrice(BigDecimal.valueOf(investigation.getActualAmount()));
        billingDetail.setDiscount(BigDecimal.valueOf(investigation.getDiscountedAmount()));
        billingDetail.setTariff(BigDecimal.valueOf(investigation.getActualAmount()));
        // billingDetail.setAmountAfterDiscount(BigDecimal.valueOf(investigation.getActualAmount()));
        billingDetail.setAmountAfterDiscount(BigDecimal.valueOf(investigation.getActualAmount()).subtract(BigDecimal.valueOf(investigation.getDiscountedAmount())));

        MasServiceCategory sevcat = masServiceCategoryRepository.findByServiceCateCode(serviceCategoryLab);
        BigDecimal tax = BigDecimal.ZERO;
        if (sevcat.getGstApplicable()) {
            /// tax=BigDecimal.valueOf(sevcat.getGstPercent()).multiply(BigDecimal.valueOf(investigation.getActualAmount())).divide(BigDecimal.valueOf(100));
            tax = BigDecimal.valueOf(sevcat.getGstPercent()).multiply(BigDecimal.valueOf(investigation.getActualAmount()).subtract(BigDecimal.valueOf(investigation.getDiscountedAmount()))).divide(BigDecimal.valueOf(100));
        }
        billingDetail.setTaxAmount(tax);
        billingDetail.setTaxPercent(BigDecimal.valueOf(sevcat.getGstPercent()));
        billingDetail.setNetAmount(billingDetail.getAmountAfterDiscount().add(billingDetail.getTaxAmount()));
        billingDetail.setTotal(billingDetail.getNetAmount());
        billingDetail.setPaymentStatus("n");

        //not null column
        // billingDetail.setDetailId();
        // billingDetail.setChargeCost();
        // billingDetail.setOpdService(getOpdService().getId());
        ///calculation
        return billingDetailRepository.save(billingDetail);
    }
    private BillingDetail BillingDetaiDataSavePackage(BillingHeader bhdId, DgInvestigationPackage pack, LabInvestigationReq req) {
        ///  Billing details
        BillingDetail billingDetail = new BillingDetail();
        billingDetail.setBillingHd(bhdId);
        billingDetail.setBillHd(bhdId);
        billingDetail.setServiceCategory(masServiceCategoryRepository.findByServiceCateCode(serviceCategoryLab));//pass from property file..

        billingDetail.setItemName(pack.getPackName());  // investigation or packeg  name to be store
        ///  billingDetail.set
        //billingDetail.setInvestigation(dtId.getInvestigationId());
        billingDetail.setPackageField(pack);
        billingDetail.setCreatedDt(OffsetDateTime.now());
        billingDetail.setUpdatedDt(OffsetDateTime.now());
        billingDetail.setCreatedAt(Instant.now());

        billingDetail.setQuantity(1);
        billingDetail.setBasePrice(BigDecimal.valueOf(req.getActualAmount()));
        billingDetail.setDiscount(BigDecimal.valueOf(req.getDiscountedAmount()));
        billingDetail.setTariff(BigDecimal.valueOf(req.getActualAmount()));
        billingDetail.setAmountAfterDiscount(BigDecimal.valueOf(req.getActualAmount()).subtract(BigDecimal.valueOf(req.getDiscountedAmount())));
        MasServiceCategory sevcat = masServiceCategoryRepository.findByServiceCateCode(serviceCategoryLab);
        BigDecimal tax = BigDecimal.ZERO;
        if (sevcat.getGstApplicable()) {
            tax = BigDecimal.valueOf(sevcat.getGstPercent()).multiply(BigDecimal.valueOf(req.getActualAmount()).subtract(BigDecimal.valueOf(req.getDiscountedAmount()))).divide(BigDecimal.valueOf(100));
        }
        billingDetail.setTaxAmount(tax);
        billingDetail.setTaxPercent(BigDecimal.valueOf(sevcat.getGstPercent()));
        billingDetail.setNetAmount(billingDetail.getAmountAfterDiscount().add(billingDetail.getTaxAmount()));
        billingDetail.setTotal(billingDetail.getNetAmount());
        billingDetail.setPaymentStatus("n");


        //not null column
        // billingDetail.setDetailId();
        // billingDetail.setChargeCost();
        // billingDetail.setOpdService(getOpdService().getId());
        return billingDetailRepository.save(billingDetail);
    }

    @Transactional
    @Override
    public ApiResponse<AppsetupResponse> labRegForExistingOrder(LabBillingOnlyRequest labReq) {
        log.info("Starting lab billing for existing order. OrderHdId={}", labReq.getOrderhdid());

        User currentUser = authUtil.getCurrentUser();
        AppsetupResponse res = new AppsetupResponse();

        if (currentUser == null) {
            log.warn("Current user not found during lab billing");
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    },
                    "Current user not found", HttpStatus.UNAUTHORIZED.value());
        }

        if (labReq.getPatientId() == null) {
            log.warn("Patient ID is null in billing request");
            return ResponseUtils.createFailureResponse(res, new TypeReference<>() {
                    },
                    "Patient ID must not be null", HttpStatus.BAD_REQUEST.value());
        }

        if (labReq.getOrderhdid() == null) {
            log.warn("OrderHdId is null in billing request");
            return ResponseUtils.createFailureResponse(res, new TypeReference<>() {
                    },
                    "Orderhdid must not be null for billing flow", HttpStatus.BAD_REQUEST.value());
        }

        Patient patient = patientRepository.findById(labReq.getPatientId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid patient ID: " + labReq.getPatientId()));

        DgOrderHd existingOrderHd = labHdRepository.findById(labReq.getOrderhdid());
        if (existingOrderHd == null) {
            log.error("Invalid OrderHdId={}", labReq.getOrderhdid());
            throw new IllegalArgumentException("Invalid orderhdid: " + labReq.getOrderhdid());
        }

        Visit visit = existingOrderHd.getVisitId();
        if (visit == null) {
            throw new IllegalArgumentException("No visit linked to this orderhdid: " + labReq.getOrderhdid());
        }

        try {
            log.debug("Calculating billing amounts");

            //  Calculate sum, discount, tax based on ALL items in request
            BigDecimal sum = BigDecimal.ZERO;
            BigDecimal tax = BigDecimal.ZERO;
            BigDecimal disc = BigDecimal.ZERO;
            MasServiceCategory servCat = masServiceCategoryRepository.findByServiceCateCode(serviceCategoryLab);

            for (LabInvestigationReq inv : labReq.getLabInvestigationReq()) {
                sum = sum.add(BigDecimal.valueOf(inv.getActualAmount()));
                disc = disc.add(BigDecimal.valueOf(inv.getDiscountedAmount()));
                if (servCat.getGstApplicable()) {
                    tax = tax.add(
                            BigDecimal.valueOf(servCat.getGstPercent())
                                    .multiply(BigDecimal.valueOf(inv.getActualAmount())
                                            .subtract(BigDecimal.valueOf(inv.getDiscountedAmount())))
                                    .divide(BigDecimal.valueOf(100))
                    );
                }
            }
            log.info("Billing calculation done. Sum={}, Discount={}, Tax={}", sum, disc, tax);

            //  Create BillingHeader
            BillingHeader billingHeader = BillingHeaderDataSave(
                    existingOrderHd,
                    visit,
                    null,
                    currentUser,
                    sum,
                    tax,
                    disc
            );

            res.setBillinghdId(billingHeader.getId().toString());
            log.info("Billing header created. BillingHdId={}", billingHeader.getId());


            //  Get ALL existing order details for this order
            List<DgOrderDt> allOrderDetails = labDtRepository.findByOrderhdId(existingOrderHd);
            log.info("Found {} order details for OrderHdId={}",
                    allOrderDetails.size(), existingOrderHd.getId());
            System.out.println("Found " + allOrderDetails.size() + " existing order details for orderhdid: " + existingOrderHd.getId());

            //  Link ALL order details to billing header
            for (DgOrderDt orderDetail : allOrderDetails) {
                orderDetail.setBillingHd(billingHeader);
                labDtRepository.save(orderDetail);
                System.out.println("✓ Linked order detail ID: " + orderDetail.getId() + " to billing header");
            }

            //  Create BillingDetail rows ONLY for items that exist in order details
            // This is the KEY FIX - match request items with existing order details
            for (LabInvestigationReq inv : labReq.getLabInvestigationReq()) {
                if (inv.getType().equalsIgnoreCase("i")) {
                    // Investigation type
                    DgMasInvestigation invEntity = investigation.findById(inv.getId())
                            .orElseThrow(() -> new IllegalArgumentException("Invalid Investigation ID: " + inv.getId()));

                    // Find matching order detail in the existing order
                    DgOrderDt matchingOrderDt = allOrderDetails.stream()
                            .filter(dt -> dt.getInvestigationId() != null &&
                                    dt.getInvestigationId().getInvestigationId() == invEntity.getInvestigationId() &&
                                    dt.getPackageId() == null) // Investigation, not package
                            .findFirst()
                            .orElse(null);

                    if (matchingOrderDt == null) {
                        System.err.println("WARNING: No order detail found for investigation ID: " + inv.getId());
                        continue;
                    }

                    System.out.println("✓ Creating billing detail for investigation: " + invEntity.getInvestigationName() + " (OrderDt ID: " + matchingOrderDt.getId() + ")");
                    BillingDetaiDataSave(billingHeader, matchingOrderDt, inv);

                } else {
                    // Package type
                    DgInvestigationPackage pkgObj = dgInvestigationPackageRepository.findById(inv.getId())
                            .orElseThrow(() -> new IllegalArgumentException("Invalid package ID: " + inv.getId()));

                    // Find matching order detail in the existing order
                    DgOrderDt matchingOrderDt = allOrderDetails.stream()
                            .filter(dt -> dt.getPackageId() != null &&
                                    dt.getPackageId().getPackId() == pkgObj.getPackId())
                            .findFirst()
                            .orElse(null);

                    if (matchingOrderDt == null) {
                        System.err.println("WARNING: No order detail found for package ID: " + inv.getId());
                        continue;
                    }

                    System.out.println("✓ Creating billing detail for package: " + pkgObj.getPackName() + " (OrderDt ID: " + matchingOrderDt.getId() + ")");
                    BillingDetaiDataSavePackage(billingHeader, pkgObj, inv);
                }
            }

            System.out.println("✓ Successfully created billing for existing order. Billing ID: " + billingHeader.getId());
            System.out.println("✓ Linked " + allOrderDetails.size() + " order details to billing header");
            System.out.println("✓ Created billing details for " + labReq.getLabInvestigationReq().size() + " items");
            log.info("Billing completed successfully. BillingHdId={}, TotalItems={}",
                    billingHeader.getId(), labReq.getLabInvestigationReq().size());

            res.setMsg("Success");
            return ResponseUtils.createSuccessResponse(res, new TypeReference<AppsetupResponse>() {
            });

        } catch (Exception e) {
            log.error("Exception occurred during lab billing for OrderHdId={}",
                    labReq.getOrderhdid(), e);

            e.printStackTrace();
            return ResponseUtils.createFailureResponse(res, new TypeReference<>() {
            }, "Internal Server Error: " + e.getMessage(), 500);
        }
    }

}
