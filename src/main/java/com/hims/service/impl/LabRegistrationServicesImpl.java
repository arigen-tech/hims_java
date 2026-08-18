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
import com.hims.service.TransactionSequenceService;
import com.hims.utils.*;
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
    private BillingService billingService;

    @Autowired
    HelperUtils helperUtils;

    @Autowired
    TransactionSequenceService transactionSequenceService;

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
            boolean labBillingEnabled) {
        DgOrderDt dt = new DgOrderDt();
        dt.setInvestigation(invEntity);
        dt.setOrderHd(savedHd);
        dt.setAppointmentDate(inv.getAppointmentDate());
        dt.setOrderQty(1);
        dt.setOrderStatus(AppConstants.STATUS_N.toLowerCase());
        if(labBillingEnabled){
            dt.setBillingStatus(AppConstants.PAYMENT_NOT_PAID.toLowerCase());
        }else{
            dt.setBillingStatus(AppConstants.PAYMENT_PAID.toLowerCase());
        }

        dt.setCreatedBy(currentUser.getFullName());
        dt.setLastChgBy(currentUser.getFullName());
        dt.setLastChgDate(LocalDate.now());
        dt.setMainChargeCodeId(invEntity.getMainChargeCodeId().getChargecodeId());
        dt.setSubChargeCodeId(invEntity.getSubChargeCodeId().getSubId());
        dt.setOrderTrackingStatus(getOrderedStatus());
        dt.setCreatedOn(HMISUtil.getCurrentLocalDateTime());
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
            BigDecimal actualAmount = BigDecimal.ZERO;
            BigDecimal discountedAmount = BigDecimal.ZERO;

            // Handle both LabInvestigationReq and LabRadioInvestigationRequest
            if (inv instanceof LabRadioInvestigationRequest) {
                actualAmount = ((LabRadioInvestigationRequest) inv).getActualAmount();
                discountedAmount = ((LabRadioInvestigationRequest) inv).getDiscountedAmount();
            } else if (inv instanceof LabInvestigationReq) {
                actualAmount = ((LabInvestigationReq) inv).getActualAmount();
                discountedAmount = ((LabInvestigationReq) inv).getDiscountedAmount();
            }

            sum = sum.add(actualAmount);
            disc = disc.add(discountedAmount);

            if (cachedServiceCategory != null && cachedServiceCategory.getGstApplicable()) {
                tax = tax.add(BigDecimal.valueOf(cachedServiceCategory.getGstPercent())
                        .multiply(actualAmount)
                                .subtract(discountedAmount))
                        .divide(BigDecimal.valueOf(100));
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
                patient.getPatientFn(), patient.getPatientLn(),
                masGenderRepository.findById(patient.getPatientGenderId())
                        .orElseThrow(() -> new SDDException("gender", 400, "Invalid gender")),
                patient.getPatientDob(), patient.getPatientAge(),
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
                    throw new SDDException("appointmentDate", 400, "Appointment date required for investigationId: " + inv.getId());
                }
            });

            Map<LocalDate, List<LabRadioInvestigationRequest>> grouped =
                    invList.stream().collect(Collectors.groupingBy(LabRadioInvestigationRequest::getAppointmentDate));

            LabRadiologyRegistrationResponse response = new LabRadiologyRegistrationResponse();
            response.setPatientId(savedPatient.getId());

            boolean labBillingEnabled = AppConstants.STATUS_Y.equalsIgnoreCase(savedPatient.getPatientHospital().getLabBilling());

            for (Map.Entry<LocalDate, List<LabRadioInvestigationRequest>> entry : grouped.entrySet()) {

                LocalDate date = entry.getKey();
                List<LabRadioInvestigationRequest> investigations = entry.getValue();

                BigDecimal[] amounts = calculateBillingAmounts(investigations);

                DgOrderHd savedHd = saveLabOrderHeader(savedPatient, savedVisit, currentUser, date, labBillingEnabled);

                if (savedHd == null) {
                    throw new SDDException("order", 500, "Failed to create lab order");
                }

                BillingHeader billingHeader = billingService.saveBillingHeaderIfEnabled(
                        labBillingEnabled, savedHd, savedVisit, currentUser,
                        amounts[0], amounts[1], amounts[2],
                        serviceCategoryLab, false
                );

                for (LabRadioInvestigationRequest inv : investigations) {

                    if (AppConstants.INVESTIGATION.equalsIgnoreCase(inv.getType())) {

                        DgMasInvestigation invEntity = investigation.findById(inv.getId())
                                .orElseThrow(() -> new SDDException("investigation", 400, "Invalid investigation ID: " + inv.getId()));

                       saveLabOrderDetail(savedHd, billingHeader, inv, invEntity, currentUser, serviceCategoryLab);

                    } else if (AppConstants.STATUS_P.equalsIgnoreCase(inv.getType())) {

                        DgInvestigationPackage pkgObj = dgInvestigationPackageRepository.findById(inv.getId())
                                .orElseThrow(() -> new SDDException("package", 400, "Invalid package ID: " + inv.getId()));

                        List<PackageInvestigationMapping> mappings = packageInvestigationMappingRepository.findByPackageId(pkgObj);

                        for (PackageInvestigationMapping map : mappings) {
                            saveLabOrderDetailForPackage(
                                    savedHd, billingHeader, inv, map.getInvestId(), pkgObj, currentUser
                            );
                        }

                        if (labBillingEnabled) {
                            billingService.saveBillingDetailPackage(billingHeader, pkgObj, inv, serviceCategoryLab);
                        }

                    } else {
                        throw new SDDException("Investigation type", 400, "Invalid investigation type");
                    }
                }

                if (labBillingEnabled) {
                    response.setBillinghdId(billingHeader.getId());
                }
            }

            response.setMsg("success");

            return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});

        } catch (SDDException e) {
            log.error("Business error: {}", e.getMessage());
            throw e;
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
        Instant visitDate = Instant.now();
        visit.setVisitDate(visitDate);
        visit.setLastChgDate(visitDate);
        visit.setDisplayPatientStatus(AppConstants.DISPLAY_PATIENT_STATUS);
        String visitType = helperUtils.getVisitTypeForFollowUpOrNew(patient.getId());
        visit.setVisitType(visitType);

        return visitRepository.save(visit);
    }

    private DgOrderHd buildOrderHd(Patient patient, Visit visit, User currentUser, LocalDate appointmentDate) {
        if (patient == null || visit == null || currentUser == null) {
            throw new SDDException("order", 400, "Invalid data for creating order header");
        }
        try {
            DgOrderHd hd = new DgOrderHd();

            hd.setAppointmentDate(appointmentDate);
            hd.setOrderDate(LocalDate.now());
            hd.setOrderTime(HMISUtil.getCurrentLocalDateTime());
            hd.setOrderNo(transactionSequenceService.generateTransactionNumber(HMISTransaction.LAB_NO, currentUser.getHospital().getId()));
            hd.setOrderStatus(AppConstants.STATUS_N.toLowerCase());
            hd.setCollectionStatus(AppConstants.STATUS_N.toLowerCase());
            if(AppConstants.STATUS_Y.equalsIgnoreCase(patient.getPatientHospital().getLabBilling())){
                hd.setPaymentStatus(AppConstants.PAYMENT_NOT_PAID.toLowerCase());
            }else{
                hd.setPaymentStatus(AppConstants.PAYMENT_PAID.toLowerCase());
            }
            hd.setHospitalId(currentUser.getHospital().getId());
            hd.setDepartmentId(visit.getDepartment().getId());
            hd.setPatientId(patient);
            hd.setVisitId(visit);
            hd.setSource(AppConstants.SOURCE_LAB.toLowerCase());
            hd.setDiscountId(1);
            hd.setCreatedBy(currentUser.getFullName());
            hd.setLastChgBy(currentUser.getFullName());
            hd.setCreatedOn(LocalDate.now());
            hd.setLastChgDate(LocalDate.now());
            hd.setLastChgTime(LocalTime.now().toString());
            return hd;
        } catch (Exception e) {
            throw new SDDException("order", 500, "Error while building order header");
        }
    }

    private DgOrderDt buildOrderDetailForPackage(DgOrderHd hd, DgMasInvestigation invest, DgInvestigationPackage pkg, LabRadioInvestigationRequest inv,Patient patient) {
        if (hd == null || invest == null || pkg == null) {
            throw new SDDException("orderDetail", 400, "Invalid data for package order detail");
        }
        try {
            DgOrderDt dt = new DgOrderDt();
            dt.setOrderHd(hd);
            dt.setInvestigation(invest);
            dt.setMainChargeCodeId(invest.getMainChargeCodeId().getChargecodeId());
            dt.setSubChargeCodeId(invest.getSubChargeCodeId().getSubId());
            dt.setInvestigationPackage(pkg);
            dt.setAppointmentDate(inv.getAppointmentDate());
            dt.setOrderQty(1);
            dt.setOrderStatus(AppConstants.STATUS_N.toLowerCase());
            if(AppConstants.STATUS_Y.equalsIgnoreCase(patient.getPatientHospital().getLabBilling())){
                dt.setBillingStatus(AppConstants.PAYMENT_NOT_PAID.toLowerCase());
            }else{
                dt.setBillingStatus(AppConstants.PAYMENT_PAID);
            }

            dt.setOrderTrackingStatus(getOrderedStatus());
            String currentUserFullName = authUtil.getCurrentUser().getFullName();
            dt.setCreatedBy(currentUserFullName);
            dt.setLastChgBy(currentUserFullName);
            dt.setCreatedOn(HMISUtil.getCurrentLocalDateTime());
            dt.setLastChgDate(LocalDate.now());
            dt.setLastChgTime(LocalTime.now().toString());
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

            boolean labBillingEnabled =
                    patient.getPatientHospital() != null
                            && AppConstants.STATUS_Y.equalsIgnoreCase(patient.getPatientHospital().getLabBilling());

            Visit savedVisit = createVisitForLabRadio(patient, laboratoryDepartment);

            List<LabRadioInvestigationRequest> invList = labReq.getInvestigationReq();

            if (invList == null || invList.isEmpty()) {
                throw new SDDException("investigation", 400, "Investigation list cannot be empty");
            }

            invList.forEach(inv -> {
                if (inv.getAppointmentDate() == null) {
                    throw new SDDException("appointmentDate", 400, "Appointment date required for investigationId: " + inv.getId());
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
                    sum = sum.add(inv.getActualAmount());
                    disc = disc.add(inv.getDiscountedAmount());

                    if (servCat.getGstApplicable()) {
                        BigDecimal net = inv.getActualAmount()
                                .subtract(inv.getDiscountedAmount());
                        tax = tax.add(net.multiply(BigDecimal.valueOf(servCat.getGstPercent())).divide(BigDecimal.valueOf(100)));
                    }
                }

                DgOrderHd savedHd = saveLabOrderHeader(patient, savedVisit, currentUser, date, labBillingEnabled);

                if (savedHd == null) {
                    throw new SDDException("order", 500, "Failed to create order");
                }

                BillingHeader billingHeader = billingService.saveBillingHeaderIfEnabled(
                        labBillingEnabled, savedHd, savedVisit, currentUser,
                        sum, tax, disc,
                        serviceCategoryLab, false
                );

                if (labBillingEnabled) {
                    res.setBillinghdId(billingHeader.getId().toString());
                }

                for (LabRadioInvestigationRequest inv : investigations) {

                    if (AppConstants.INVESTIGATION.equalsIgnoreCase(inv.getType())) {

                        DgMasInvestigation invEntity = investigation.findById(inv.getId())
                                .orElseThrow(() -> new SDDException("investigation", 400, "Invalid investigation ID: " + inv.getId()));

                        saveLabOrderDetail(savedHd, billingHeader, inv, invEntity, currentUser, serviceCategoryLab);

                    } else if (AppConstants.PACKAGE.equalsIgnoreCase(inv.getType())) {

                        DgInvestigationPackage pkg = dgInvestigationPackageRepository.findById(inv.getId())
                                .orElseThrow(() -> new SDDException("package", 400, "Invalid package ID: " + inv.getId()));

                        List<PackageInvestigationMapping> mappings = packageInvestigationMappingRepository.findByPackageId(pkg);

                        for (PackageInvestigationMapping map : mappings) {
                         saveLabOrderDetailForPackage(
                                    savedHd, billingHeader, inv, map.getInvestId(), pkg, currentUser
                            );
                        }

                        if (labBillingEnabled) {
                            billingService.saveBillingDetailPackage(billingHeader, pkg, inv, serviceCategoryLab);
                        }

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
        billingHeader.setBillNo(transactionSequenceService.generateTransactionNumber(HMISTransaction.BILL_NO, currentUser.getHospital().getId()));
        billingHeader.setPatient(vId.getPatient());
        billingHeader.setVisit(vId);
        billingHeader.setPatientDisplayName(vId.getPatient().getPatientFn());
        LocalDate dob = vId.getPatient().getPatientDob();
        billingHeader.setPatientAge(ageCalculator(dob));
        billingHeader.setPatientGender(vId.getPatient().getPatientGender().getGenderName());
        billingHeader.setPatientAddress(vId.getPatient().getPatientAddress1());
        billingHeader.setHospital(currentUser.getHospital());
        billingHeader.setHospitalName(vId.getPatient().getPatientHospital().getHospitalName());
        billingHeader.setHospitalAddress(vId.getHospital().getAddress());
        billingHeader.setHospitalMobileNo(vId.getHospital().getContactNumber());
        billingHeader.setHospitalGstin(vId.getHospital().getGstnNo());
        billingHeader.setServiceCategory(masServiceCategoryRepository.findByServiceCateCode(serviceCategoryLab));  ///for which table
        billingHeader.setReferredBy(vId.getDoctorName());
        billingHeader.setBillingDate(Instant.now());
        billingHeader.setPaymentStatus(AppConstants.PAYMENT_NOT_PAID.toLowerCase());
        billingHeader.setVisit(vId);
        billingHeader.setHdorder(hdId);
        billingHeader.setTotalAmount(sum);
        billingHeader.setDiscountAmount(disc);
        billingHeader.setNetAmount(sum.subtract(disc).add(tax));
        billingHeader.setTaxTotal(tax);
        //billingHeader.setDiscount();
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
        billingDetail.setServiceCategory(masServiceCategoryRepository.findByServiceCateCode(serviceCategoryLab));
        billingDetail.setItemName(dtId.getInvestigation().getInvestigationName());
        billingDetail.setInvestigation(dtId.getInvestigation());
        billingDetail.setPackageField(dtId.getInvestigationPackage());
        billingDetail.setCreatedDt(OffsetDateTime.now());
        billingDetail.setUpdatedDt(OffsetDateTime.now());
        billingDetail.setCreatedAt(Instant.now());
        billingDetail.setQuantity(1);
        billingDetail.setBasePrice(investigation.getActualAmount());
        billingDetail.setDiscount(investigation.getDiscountedAmount());
        billingDetail.setTariff(investigation.getActualAmount());
        billingDetail.setAmountAfterDiscount(investigation.getActualAmount().subtract(investigation.getDiscountedAmount()));

        MasServiceCategory sevcat = masServiceCategoryRepository.findByServiceCateCode(serviceCategoryLab);
        BigDecimal tax = BigDecimal.ZERO;
        if (sevcat.getGstApplicable()) {
            tax = BigDecimal.valueOf(sevcat.getGstPercent()).multiply(investigation.getActualAmount()).subtract(investigation.getDiscountedAmount()).divide(BigDecimal.valueOf(100));
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
        BillingDetail billingDetail = new BillingDetail();
        billingDetail.setBillingHd(bhdId);
        billingDetail.setBillHd(bhdId);
        billingDetail.setServiceCategory(masServiceCategoryRepository.findByServiceCateCode(serviceCategoryLab));
        billingDetail.setItemName(pack.getPackName());
        billingDetail.setPackageField(pack);
        billingDetail.setCreatedDt(OffsetDateTime.now());
        billingDetail.setUpdatedDt(OffsetDateTime.now());
        billingDetail.setCreatedAt(Instant.now());
        billingDetail.setQuantity(1);
        billingDetail.setBasePrice(req.getActualAmount());
        billingDetail.setDiscount(req.getDiscountedAmount());
        billingDetail.setTariff(req.getActualAmount());
        billingDetail.setAmountAfterDiscount(req.getActualAmount().subtract(req.getDiscountedAmount()));
        MasServiceCategory sevcat = masServiceCategoryRepository.findByServiceCateCode(serviceCategoryLab);
        BigDecimal tax = BigDecimal.ZERO;
        if (sevcat.getGstApplicable()) {
            tax = BigDecimal.valueOf(sevcat.getGstPercent()).multiply(req.getActualAmount()).subtract(req.getDiscountedAmount()).divide(BigDecimal.valueOf(100));
        }
        billingDetail.setTaxAmount(tax);
        billingDetail.setTaxPercent(BigDecimal.valueOf(sevcat.getGstPercent()));
        billingDetail.setNetAmount(billingDetail.getAmountAfterDiscount().add(billingDetail.getTaxAmount()));
        billingDetail.setTotal(billingDetail.getNetAmount());
        billingDetail.setPaymentStatus(AppConstants.PAYMENT_NOT_PAID.toLowerCase());
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

        Optional<DgOrderHd> orderHdOpt = labHdRepository.findById(labReq.getOrderhdid());
        if (orderHdOpt .isEmpty()) {
            log.error("Invalid OrderHdId={}", labReq.getOrderhdid());
            throw new IllegalArgumentException("Invalid orderhdid: " + labReq.getOrderhdid());
        }
        DgOrderHd existingOrderHd = orderHdOpt.get();

        Visit visit = existingOrderHd.getVisitId();
        if (visit == null) {
            throw new IllegalArgumentException("No visit linked to this orderhdid: " + labReq.getOrderhdid());
        }

        try {
            log.debug("Calculating billing amounts");

            BigDecimal sum = BigDecimal.ZERO;
            BigDecimal tax = BigDecimal.ZERO;
            BigDecimal disc = BigDecimal.ZERO;
            MasServiceCategory servCat = masServiceCategoryRepository.findByServiceCateCode(serviceCategoryLab);

            for (LabInvestigationReq inv : labReq.getLabInvestigationReq()) {
                sum = sum.add(inv.getActualAmount());
                disc = disc.add(inv.getDiscountedAmount());
                if (servCat.getGstApplicable()) {
                    tax = tax.add(
                            BigDecimal.valueOf(servCat.getGstPercent())
                                    .multiply(inv.getActualAmount())
                                            .subtract(inv.getDiscountedAmount())
                                    .divide(BigDecimal.valueOf(100))
                    );
                }
            }
            log.info("Billing calculation done. Sum={}, Discount={}, Tax={}", sum, disc, tax);

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


            List<DgOrderDt> allOrderDetails = labDtRepository.findByOrderHd(existingOrderHd);
            log.info("Found {} order details for OrderHdId={}",
                    allOrderDetails.size(), existingOrderHd.getId());
            System.out.println("Found " + allOrderDetails.size() + " existing order details for orderhdid: " + existingOrderHd.getId());

            for (DgOrderDt orderDetail : allOrderDetails) {
                orderDetail.setBillingHd(billingHeader);
                labDtRepository.save(orderDetail);
                System.out.println("✓ Linked order detail ID: " + orderDetail.getId() + " to billing header");
            }

            for (LabInvestigationReq inv : labReq.getLabInvestigationReq()) {
                if (inv.getType().equalsIgnoreCase("i")) {
                    // Investigation type
                    DgMasInvestigation invEntity = investigation.findById(inv.getId())
                            .orElseThrow(() -> new IllegalArgumentException("Invalid Investigation ID: " + inv.getId()));

                    DgOrderDt matchingOrderDt = allOrderDetails.stream()
                            .filter(dt -> dt.getInvestigation() != null &&
                                    dt.getInvestigation().getInvestigationId() == invEntity.getInvestigationId() &&
                                    dt.getInvestigationPackage() == null)
                            .findFirst()
                            .orElse(null);

                    if (matchingOrderDt == null) {
                        System.err.println("WARNING: No order detail found for investigation ID: " + inv.getId());
                        continue;
                    }
                    System.out.println("Creating billing detail for investigation: " + invEntity.getInvestigationName() + " (OrderDt ID: " + matchingOrderDt.getId() + ")");
                    BillingDetaiDataSave(billingHeader, matchingOrderDt, inv);
                } else {
                    DgInvestigationPackage pkgObj = dgInvestigationPackageRepository.findById(inv.getId())
                            .orElseThrow(() -> new IllegalArgumentException("Invalid package ID: " + inv.getId()));

                    DgOrderDt matchingOrderDt = allOrderDetails.stream()
                            .filter(dt -> dt.getInvestigationPackage() != null &&
                                    dt.getInvestigationPackage().getPackId() == pkgObj.getPackId())
                            .findFirst()
                            .orElse(null);

                    if (matchingOrderDt == null) {
                        System.err.println("WARNING: No order detail found for package ID: " + inv.getId());
                        continue;
                    }

                    System.out.println("Creating billing detail for package: " + pkgObj.getPackName() + " (OrderDt ID: " + matchingOrderDt.getId() + ")");
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

    @Override
    public DgOrderDt saveLabOrderDetailForPackage(DgOrderHd hd, BillingHeader billing, LabRadioInvestigationRequest inv,
                                                  DgMasInvestigation investEntity, DgInvestigationPackage pkg,
                                                  User currentUser) {
        if (hd == null || investEntity == null || pkg == null) {
            throw new SDDException("orderDetail", 400, "Invalid data for package order detail");
        }
        try {
            DgOrderDt dt = new DgOrderDt();
            dt.setOrderHd(hd);
            dt.setInvestigation(investEntity);
            dt.setMainChargeCodeId(investEntity.getMainChargeCodeId().getChargecodeId());
            dt.setSubChargeCodeId(investEntity.getSubChargeCodeId().getSubId());
            dt.setInvestigationPackage(pkg);
            dt.setAppointmentDate(inv.getAppointmentDate());
            dt.setOrderQty(1);
            dt.setOrderStatus(AppConstants.STATUS_N.toLowerCase());
            dt.setBillingStatus(billing != null
                    ? AppConstants.PAYMENT_NOT_PAID.toLowerCase()
                    : AppConstants.PAYMENT_PAID.toLowerCase());
            dt.setBillingHd(billing);
            dt.setOrderTrackingStatus(getOrderedStatus());
            dt.setCreatedBy(currentUser.getFullName());
            dt.setLastChgBy(currentUser.getFullName());
            dt.setCreatedOn(HMISUtil.getCurrentLocalDateTime());
            dt.setLastChgDate(LocalDate.now());
            dt.setLastChgTime(HMISUtil.getCurrentLocalTime().toString());

            // billingDetailPackage is invoked ONCE by the caller after its mapping loop, not here
            return labDtRepository.save(dt);
        } catch (Exception e) {
            throw new SDDException("orderDetail", 500, "Error while creating package order detail");
        }


    }

    @Override
    public DgOrderDt saveLabOrderDetail(DgOrderHd hd, BillingHeader billing, LabRadioInvestigationRequest inv,
                                        DgMasInvestigation entity, User currentUser, String serviceCategoryCode) {

        DgOrderDt dt = new DgOrderDt();
        dt.setInvestigation(entity);
        dt.setOrderHd(hd);
        dt.setAppointmentDate(inv.getAppointmentDate());
        dt.setOrderQty(1);
        dt.setOrderStatus(AppConstants.STATUS_N.toLowerCase());
        dt.setBillingStatus(billing != null
                ? AppConstants.PAYMENT_NOT_PAID.toLowerCase()
                : AppConstants.PAYMENT_PAID.toLowerCase());
        dt.setBillingHd(billing);
        dt.setCreatedBy(currentUser.getFullName());
        dt.setLastChgBy(currentUser.getFullName());
        dt.setLastChgDate(LocalDate.now());
        dt.setMainChargeCodeId(entity.getMainChargeCodeId().getChargecodeId());
        dt.setSubChargeCodeId(entity.getSubChargeCodeId().getSubId());
        dt.setOrderTrackingStatus(getOrderedStatus());
        dt.setCreatedOn(HMISUtil.getCurrentLocalDateTime());
        dt.setLastChgTime(HMISUtil.getCurrentLocalTime().toString());

        DgOrderDt saved = labDtRepository.save(dt);

        if (billing != null) {
            billingService.saveBillingDetail(billing, saved, inv.getActualAmount(),BigDecimal.ZERO, serviceCategoryCode, false);
        }

        return saved;
    }

    @Override
    public DgOrderHd saveLabOrderHeader(Patient patient, Visit visit, User currentUser, LocalDate appointmentDate, boolean billingEnabled) {
        if (patient == null || visit == null || currentUser == null) {
            throw new SDDException("order", 400, "Invalid data for creating order header");
        }
        try {
            DgOrderHd hd = new DgOrderHd();
            hd.setAppointmentDate(appointmentDate);
            hd.setOrderDate(LocalDate.now());
            hd.setOrderTime(HMISUtil.getCurrentLocalDateTime());
            hd.setOrderNo(transactionSequenceService.generateTransactionNumber(HMISTransaction.LAB_NO, currentUser.getHospital().getId()));
            hd.setOrderStatus(AppConstants.STATUS_N.toLowerCase());
            hd.setCollectionStatus(AppConstants.STATUS_N.toLowerCase());
            hd.setPaymentStatus(billingEnabled
                    ? AppConstants.PAYMENT_NOT_PAID.toLowerCase()
                    : AppConstants.PAYMENT_PAID.toLowerCase());
            hd.setHospitalId(currentUser.getHospital().getId());
            hd.setDepartmentId(visit.getDepartment().getId());
            hd.setPatientId(patient);
            hd.setVisitId(visit);
            hd.setSource(AppConstants.SOURCE_LAB.toLowerCase());
            hd.setDiscountId(1);
            hd.setCreatedBy(currentUser.getFullName());
            hd.setLastChgBy(currentUser.getFullName());
            hd.setCreatedOn(LocalDate.now());
            hd.setLastChgDate(LocalDate.now());
            hd.setLastChgTime(HMISUtil.getCurrentLocalTime().toString());
            return labHdRepository.save(hd);
        } catch (Exception e) {
            throw new SDDException("order", 500, "Error while building order header");
        }
    }


}
