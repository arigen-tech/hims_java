package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.exception.SDDException;
import com.hims.helperUtil.HelperUtils;
import com.hims.projection.RadiologyProjection;
import com.hims.request.*;
import com.hims.response.*;
import com.hims.service.BillingService;
import com.hims.service.RadiologyService;
import com.hims.service.TransactionSequenceService;
import com.hims.utils.AuthUtil;
import com.hims.utils.HMISTransaction;
import com.hims.utils.RandomNumGenerator;
import com.hims.utils.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.hims.helperUtil.ConverterUtils.ageCalculator;

@Service
public class RadiologyServiceImpl implements RadiologyService {
    @Autowired
    RandomNumGenerator randomNumGenerator;
    private static final Logger log = LoggerFactory.getLogger(LabRegistrationServicesImpl.class);
    @Autowired
    AuthUtil authUtil;
    @Autowired
    PatientServiceImpl patientService;
    @Autowired
    MasHospitalRepository masHospitalRepository;
    @Autowired
    VisitRepository visitRepository;
    @Autowired
    MasDepartmentRepository masDepartmentRepository;
    @Autowired
    MasServiceCategoryRepository masServiceCategoryRepository;
    @Autowired
    RadOrderHdRepository radOrderHdRepository;
    @Autowired
    RadOrderDtRepository radOrderDtRepository;

    @Autowired
    BillingService billingService;

    @Value("${serviceCategoryRad}")
    private String serviceCategoryRad;

    @Value("${app.radiologyDepartment}")
    private Long radiologyDepartment;

    @Autowired
    LabRegistrationServicesImpl labRegistrationServices;
    @Autowired
    BillingHeaderRepository billingHeaderRepository;
    @Autowired
    DgMasInvestigationRepository dgMasInvestigationRepository;
    @Autowired
    BillingDetailRepository billingDetailRepository;
    @Autowired
    PatientRepository patientRepository;
    @Autowired
    MasGenderRepository masGenderRepository;
    @Autowired
    MasRelationRepository masRelationRepository;
    @Autowired
    DgInvestigationPackageRepository dgInvestigationPackageRepository;
    @Autowired
    PackageInvestigationMappingRepository packageInvestigationMappingRepository;
    @Autowired
    PaymentDetailRepository paymentDetailRepository;
    @Autowired
    private RadStudyReportRepository radStudyReportRepository;
    @Autowired
    private TransactionSequenceService transactionSequenceService;

    @Autowired
    private HelperUtils helperUtils;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<LabRadiologyRegistrationResponse> registerPatientWithInv(PatientRequest patient, List<LabInvestigationReq> radInvestigationReq) {
        log.info("Starting lab registration process");
        LabRadiologyRegistrationResponse response=new LabRadiologyRegistrationResponse();
        User currentUser = authUtil.getCurrentUser();
        Optional<Patient> existingPatient = patientRepository.findByUniqueCombination(
                patient.getPatientFn(),
                patient.getPatientLn(),
                (masGenderRepository.findById(patient.getPatientGenderId())).get(),
                patient.getPatientDob() != null ? patient.getPatientDob() : null,
                patient.getPatientAge(),
                patient.getPatientMobileNumber(),
                (masRelationRepository.findById(patient.getPatientRelationId())).get());
        if (existingPatient.isPresent()) {
            response.setMsg("Patient already Registered");
            return ResponseUtils.createFailureResponse(response, new TypeReference<>() {
                    },
                    "Patient already Registered", 500);
        }
        Patient savedPatient = patientService.savePatient(patient,false);
        response.setPatientId(savedPatient.getId());
        Long departmentId = authUtil.getCurrentDepartmentId();
        MasHospital masHospital = masHospitalRepository
                .findById(currentUser.getHospital().getId())
                .orElseThrow(() -> {
                    log.error("Invalid hospital ID");
                    return new IllegalArgumentException("Invalid hospital ID");
                });
        Long existingTokens = visitRepository.countTokensForToday(currentUser.getHospital().getId(), departmentId);
        MasDepartment department = masDepartmentRepository.findById(departmentId)
                .orElseThrow(() -> {
                    log.error("Invalid department ID: {}", departmentId);
                    return new IllegalArgumentException("Invalid department ID: " + departmentId);
                });
        try {
            Visit visit = new Visit();
            visit.setPatient(savedPatient);
            visit.setVisitStatus(AppConstants.VISIT_STATUS_PENDING.toLowerCase());
            visit.setBillingStatus(AppConstants.PAYMENT_NOT_PAID.toLowerCase());
            visit.setHospital(masHospital);
            visit.setTokenNo(existingTokens + 1);
             Instant visitDate = Instant.now();
            visit.setVisitDate(visitDate);
            visit.setLastChgDate(visitDate);
            visit.setDepartment(department);
            visit.setDisplayPatientStatus("wp");
            Visit savedVisit = visitRepository.save(visit);
            log.info("Visit saved successfully, VisitId={}", savedVisit.getId());

            // Validate all investigation appointment dates
            for (LabInvestigationReq inv : radInvestigationReq) {
                if (inv.getAppointmentDate() == null) {
                    log.error("Appointment date missing for investigationId={}", inv.getId());
                    throw new IllegalArgumentException("Investigation appointment date must not be null for investigationId: " + inv.getId());
                }
            }
            // Group investigations by appointment date (safely)
            Map<LocalDate, List<LabInvestigationReq>> grouped = radInvestigationReq.stream()
                    .filter(req -> req.getAppointmentDate() != null)
                    .collect(Collectors.groupingBy(LabInvestigationReq::getAppointmentDate));
            log.info("Investigations grouped by appointment date");
            for (Map.Entry<LocalDate, List<LabInvestigationReq>> entry : grouped.entrySet()) {
                LocalDate date = entry.getKey();
                List<LabInvestigationReq> investigations = entry.getValue();
                log.info("Processing appointmentDate={}, investigationCount={}",
                        date, investigations.size());
                BigDecimal sum=BigDecimal.ZERO;
                BigDecimal tax=BigDecimal.ZERO;
                BigDecimal disc=BigDecimal.ZERO;

                MasServiceCategory servCat = masServiceCategoryRepository.findByServiceCateCode(serviceCategoryRad);
                for(LabInvestigationReq inves:investigations){
                    // if(inves.isCheckStatus()){
                    sum=sum.add(BigDecimal.valueOf(inves.getActualAmount()));
                    disc=disc.add(BigDecimal.valueOf(inves.getDiscountedAmount()));
                    if(servCat.getGstApplicable()){
                        tax=tax.add(BigDecimal.valueOf(servCat.getGstPercent()).multiply(BigDecimal.valueOf(inves.getActualAmount()).subtract(BigDecimal.valueOf(inves.getDiscountedAmount()))).divide(BigDecimal.valueOf(100)));
                    }
                    // }
                }
                log.debug("Calculated Amounts => Sum={}, Discount={}, Tax={}", sum, disc, tax);
                System.out.println();

                RadOrderHd hd = new RadOrderHd();
                hd.setAppointmentDate(date);
                hd.setPaymentStatus(AppConstants.PAYMENT_NOT_PAID.toLowerCase());
                hd.setOrderDate(LocalDate.now());
                hd.setOrderTime(Instant.now());
                hd.setPatient(savedPatient);
                hd.setVisit(savedVisit);
                hd.setDepartment(department);
                hd.setHospital(masHospital);
                hd.setLastChgBy(currentUser.getFirstName()+" "+currentUser.getLastName());
                hd.setLastChgDate(Instant.now());
                hd.setCreatedon(Instant.now());
                hd.setCreatedby(currentUser.getFirstName()+ " "+currentUser.getLastName());
                RadOrderHd savedHd = radOrderHdRepository.save(hd);
                log.info("Order Header saved, OrderHdId={}", savedHd.getId());
                BillingHeader headerId=new BillingHeader();
                headerId = BillingHeaderDataSave(savedHd, savedVisit, currentUser,sum,tax,disc);
                response.setBillinghdId(headerId.getId());
                savedVisit.setBillingHd(headerId);
                visitRepository.save(savedVisit);
                log.info("Billing Header created, BillingHdId={}", headerId.getId());


//                res.setBillinghdId(headerId.getId().toString());
                // }

                for (LabInvestigationReq inv : investigations) {
                    //check, type= "i"  for  investigation   and  "p"  for packeg to differenciate
                    if (inv.getType().equalsIgnoreCase("i")) {
                        if (inv.getId() == null) {
                            throw new IllegalArgumentException("Investigation ID must not be null");
                        }
                        DgMasInvestigation invEntity =
                                dgMasInvestigationRepository.findById(inv.getId())
                                        .orElseThrow(() -> {
                                            log.error("Invalid investigation ID={}", inv.getId());
                                            return new IllegalArgumentException(
                                                    "Invalid Investigation ID: " + inv.getId());
                                        });
                        RadOrderDt dt = new RadOrderDt();
                        dt.setOrderAccessionNo(transactionSequenceService.generateTransactionNumber(HMISTransaction.RADIOLOGY_NO, patient.getPatientHospitalId()));
                        dt.setRadOrderhd(savedHd);
                        dt.setInvestigation(invEntity);
                        dt.setSubChargecode(invEntity.getSubChargeCodeId());
                        dt.setAppointmentDate(inv.getAppointmentDate());
                        dt.setLastChgBy(currentUser.getFirstName()+" "+currentUser.getLastName());
                        dt.setCreatedby(currentUser.getFirstName()+" "+currentUser.getLastName());
                        dt.setBillingStatus(AppConstants.PAYMENT_NOT_PAID.toLowerCase());
                        dt.setCreatedon(Instant.now());
                        dt.setLastChgDate(Instant.now());
                        dt.setBillingHd(headerId);
                        dt.setStudyStatus(AppConstants.STATUS_N.toLowerCase());
                        dt.setReportStatus(AppConstants.STATUS_N.toLowerCase());
                        dt.setHl7MwlStatus(AppConstants.STATUS_N.toLowerCase());
                        dt.setPacsCompletionStatus(AppConstants.STATUS_N.toLowerCase());
                        dt.setOrderStatus(AppConstants.STATUS_Y.toLowerCase());
                        RadOrderDt savedDt = radOrderDtRepository.save(dt);
                        BillingDetaiDataSave(headerId, savedDt, inv);
//                        savedDt.setBillingHd();
                        log.debug("Investigation OrderDt saved, OrderDtId={}",
                                savedDt.getId());
                    }
                    else {
                        DgInvestigationPackage pkgObj = dgInvestigationPackageRepository.findById(inv.getId()).get();
                        List<PackageInvestigationMapping> mappings = packageInvestigationMappingRepository.findByPackageId(pkgObj);
                        for (PackageInvestigationMapping map : mappings) {
                            DgMasInvestigation investId = map.getInvestId();
                            RadOrderDt dt = new RadOrderDt();
                            dt.setRadOrderhd(savedHd);
                            dt.setInvestigation(investId);
                            dt.setOrderAccessionNo(transactionSequenceService.generateTransactionNumber(HMISTransaction.RADIOLOGY_NO, patient.getPatientHospitalId()));
                            dt.setSubChargecode(investId.getSubChargeCodeId());
                            dt.setPackageId(pkgObj);
                            dt.setAppointmentDate(inv.getAppointmentDate());
                            dt.setLastChgBy(currentUser.getFirstName()+" "+currentUser.getLastName());
                            dt.setCreatedby(currentUser.getFirstName()+" "+currentUser.getLastName());
                            dt.setLastChgDate(Instant.now());
                            dt.setBillingStatus(AppConstants.PAYMENT_NOT_PAID.toLowerCase());
                            dt.setCreatedon(Instant.now());
                            dt.setOrderStatus(AppConstants.STATUS_Y.toLowerCase());
                            dt.setBillingHd(headerId);
                            RadOrderDt savedDt = radOrderDtRepository.save(dt);
                        }
                        BillingDetailPackageSave(headerId, pkgObj, inv);
                    }
                }
            }
            response.setMsg("Success");

            return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});
        }
        catch (SDDException e) {
            log.error("SDDException occurred", e);
            e.printStackTrace();
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, e.getMessage(), e.getStatus());
        }
        catch (Exception e) {
            log.error("Unexpected error during lab registration", e);
            e.printStackTrace(); // log exception for debugging
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Internal Server Error", 500);
        }


    }


    private LabRadioCalculateAmountDTO calculateAmount(List<LabRadioInvestigationRequest> list, MasServiceCategory cat) {
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.ZERO;

        for (LabRadioInvestigationRequest i : list) {
            total = total.add(i.getActualAmount());
            discount = discount.add(i.getDiscountedAmount());

            if (cat != null && cat.getGstApplicable()) {
                BigDecimal net = i.getActualAmount()
                        .subtract(i.getDiscountedAmount());

                tax = tax.add(
                        net.multiply(BigDecimal.valueOf(cat.getGstPercent()))
                                .divide(BigDecimal.valueOf(100))
                );
            }
        }

        return new LabRadioCalculateAmountDTO(total, discount, tax);
    }

    private BillingHeader BillingHeaderDataSave(RadOrderHd hdId, Visit vId, User currentUser, BigDecimal sum, BigDecimal tax, BigDecimal disc) {
        BillingHeader billingHeader = new BillingHeader();
        billingHeader.setBillNo(transactionSequenceService.generateTransactionNumber(HMISTransaction.BILL_NO, vId.getHospital().getId()));
        billingHeader.setPatient(vId.getPatient());
        billingHeader.setVisit(vId);
        billingHeader.setPatientDisplayName(vId.getPatient().getFullName());
        LocalDate dob =  vId.getPatient().getPatientDob();//get DOB from Patient table and calculate age
        billingHeader.setPatientAge(ageCalculator(dob));
        billingHeader.setPatientGender(vId.getPatient().getPatientGender().getGenderName());
        billingHeader.setPatientAddress(vId.getPatient().getPatientAddress1());
        billingHeader.setHospital(currentUser.getHospital());
        billingHeader.setHospitalName(vId.getPatient().getPatientHospital().getHospitalName());
        billingHeader.setHospitalAddress(vId.getHospital().getAddress());
        billingHeader.setHospitalMobileNo(vId.getHospital().getContactNumber());  //column is not exist in Patient table
        billingHeader.setHospitalGstin(vId.getHospital().getGstnNo());  //column is not exist in Patient table
        billingHeader.setServiceCategory(masServiceCategoryRepository.findByServiceCateCode(serviceCategoryRad));  ///for which table
        billingHeader.setReferredBy(vId.getDoctorName());//few doute
        billingHeader.setBillingDate(Instant.now());
        billingHeader.setPaymentStatus("n");
        billingHeader.setVisit(vId);
        billingHeader.setRadOrderHd(hdId);
        billingHeader.setTotalAmount(sum);
        billingHeader.setDiscountAmount(disc);
        billingHeader.setNetAmount(sum.subtract(disc).add(tax));
        billingHeader.setTaxTotal(tax);
        //billingHeader.setDiscount();//id is Pass
        //billingHeader.setDiscountAmount(BigDecimal.valueOf(labReq.getDiscountAmount()));
        billingHeader.setCreatedBy(currentUser.getFirstName()+" "+currentUser.getLastName());
        billingHeader.setCreatedDt(Instant.now());
        billingHeader.setUpdatedDt(Instant.now());
        billingHeader.setBillDate(OffsetDateTime.now());
        billingHeader.setUpdatedAt(OffsetDateTime.now());
        return  billingHeaderRepository.save(billingHeader);
    }

    private BillingDetail  BillingDetaiDataSave(BillingHeader bhdId, RadOrderDt dtId, LabInvestigationReq investigation){
        ///  Billing details
        BillingDetail billingDetail = new BillingDetail();
        billingDetail.setBillingHd(bhdId);
        billingDetail.setBillHd(bhdId);
        billingDetail.setServiceCategory(masServiceCategoryRepository.findByServiceCateCode(serviceCategoryRad));//pass from property file

        billingDetail.setItemName(dtId.getInvestigation().getInvestigationName()) ;  // investigation or packeg  name to be store
        billingDetail.setQuantity(1);//default
        billingDetail.setInvestigation(dtId.getInvestigation());
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

        MasServiceCategory sevcat = masServiceCategoryRepository.findByServiceCateCode(serviceCategoryRad);
        BigDecimal tax=BigDecimal.ZERO;
        if(sevcat.getGstApplicable()){
            /// tax=BigDecimal.valueOf(sevcat.getGstPercent()).multiply(BigDecimal.valueOf(investigation.getActualAmount())).divide(BigDecimal.valueOf(100));
            tax=BigDecimal.valueOf(sevcat.getGstPercent()).multiply(BigDecimal.valueOf(investigation.getActualAmount()).subtract(BigDecimal.valueOf(investigation.getDiscountedAmount()))).divide(BigDecimal.valueOf(100));
        }
        billingDetail.setTaxAmount(tax);
        billingDetail.setTaxPercent(BigDecimal.valueOf(sevcat.getGstPercent()));
        billingDetail.setNetAmount(billingDetail.getAmountAfterDiscount().add(billingDetail.getTaxAmount()));
        billingDetail.setTotal(billingDetail.getNetAmount());
        billingDetail.setPaymentStatus(AppConstants.PAYMENT_NOT_PAID.toLowerCase());

        //not null column
        // billingDetail.setDetailId();
        // billingDetail.setChargeCost();
        // billingDetail.setOpdService(getOpdService().getId());
        ///calculation
        return  billingDetailRepository.save(billingDetail);
    }

    private BillingDetail  BillingDetailPackageSave(BillingHeader bhdId, DgInvestigationPackage dtId, LabInvestigationReq investigation){
        ///  Billing details
        BillingDetail billingDetail = new BillingDetail();
        billingDetail.setBillingHd(bhdId);
        billingDetail.setBillHd(bhdId);
        billingDetail.setServiceCategory(masServiceCategoryRepository.findByServiceCateCode(serviceCategoryRad));//pass from property file.

        billingDetail.setItemName(dtId.getPackName());
        billingDetail.setQuantity(1);//default
        billingDetail.setInvestigation(null);
        billingDetail.setPackageField(dtId);
        billingDetail.setCreatedDt(OffsetDateTime.now());
        billingDetail.setUpdatedDt(OffsetDateTime.now());
        billingDetail.setCreatedAt(Instant.now());
        billingDetail.setQuantity(1);
        billingDetail.setBasePrice(BigDecimal.valueOf(investigation.getActualAmount()));
        billingDetail.setDiscount(BigDecimal.valueOf(investigation.getDiscountedAmount()));
        billingDetail.setTariff(BigDecimal.valueOf(investigation.getActualAmount()));
        // billingDetail.setAmountAfterDiscount(BigDecimal.valueOf(investigation.getActualAmount()));
        billingDetail.setAmountAfterDiscount(BigDecimal.valueOf(investigation.getActualAmount()).subtract(BigDecimal.valueOf(investigation.getDiscountedAmount())));

        MasServiceCategory sevcat = masServiceCategoryRepository.findByServiceCateCode(serviceCategoryRad);
        BigDecimal tax=BigDecimal.ZERO;
        if(sevcat.getGstApplicable()){
            /// tax=BigDecimal.valueOf(sevcat.getGstPercent()).multiply(BigDecimal.valueOf(investigation.getActualAmount())).divide(BigDecimal.valueOf(100));
            tax=BigDecimal.valueOf(sevcat.getGstPercent()).multiply(BigDecimal.valueOf(investigation.getActualAmount()).subtract(BigDecimal.valueOf(investigation.getDiscountedAmount()))).divide(BigDecimal.valueOf(100));
        }
        billingDetail.setTaxAmount(tax);
        billingDetail.setTaxPercent(BigDecimal.valueOf(sevcat.getGstPercent()));
        billingDetail.setNetAmount(billingDetail.getAmountAfterDiscount().add(billingDetail.getTaxAmount()));
        billingDetail.setTotal(billingDetail.getNetAmount());
        billingDetail.setPaymentStatus(AppConstants.PAYMENT_NOT_PAID.toLowerCase());

        //not null column
        // billingDetail.setDetailId();
        // billingDetail.setChargeCost();
        // billingDetail.setOpdService(getOpdService().getId());
        ///calculation
        return  billingDetailRepository.save(billingDetail);
    }



    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<LabRadiologyRegistrationResponse> registerAndBookingRadiology(
            PatientRequest patient,
            List<LabRadioInvestigationRequest> investigationReq) {

        if (patient == null || investigationReq == null || investigationReq.isEmpty()) {
            throw new IllegalArgumentException("Patient and investigations are required");
        }
        log.info("Starting radiology registration for patient: {}", patient.getPatientFn());

        Optional<Patient> existingPatient = patientRepository.findByUniqueCombination(
                patient.getPatientFn(),
                patient.getPatientLn(),
                null,
                patient.getPatientDob(),
                patient.getPatientAge(),
                patient.getPatientMobileNumber(),
                null
        );
        if (existingPatient.isPresent()) {
            throw new SDDException("patient",409,"Patient already registered");
        }

        MasServiceCategory serviceCategory = masServiceCategoryRepository.findByServiceCateCode(serviceCategoryRad);
        if (serviceCategory == null) {
            throw new IllegalArgumentException("Invalid service category");
        }

        User currentUser = authUtil.getCurrentUser();
        String userName = currentUser.getFirstName() + " " + currentUser.getLastName();

        List<Long> investigationIds = new ArrayList<>();
        List<Long> packageIds = new ArrayList<>();

        for (LabRadioInvestigationRequest i : investigationReq) {
            if (AppConstants.INVESTIGATION.toLowerCase().equalsIgnoreCase(i.getType())) {
                investigationIds.add(i.getId());
            } else if (AppConstants.PACKAGE.toLowerCase().equalsIgnoreCase(i.getType())) {
                packageIds.add(i.getId());
            } else {
                throw new SDDException("type", 400, "Invalid investigation type");
            }
        }

        Map<Long, DgMasInvestigation> investigationsMap = investigationIds.isEmpty()
                ? new HashMap<>()
                : dgMasInvestigationRepository.findAllById(investigationIds)
                .stream()
                .collect(Collectors.toMap(DgMasInvestigation::getInvestigationId, Function.identity()));

        Map<Long, DgInvestigationPackage> packagesMap = packageIds.isEmpty()
                ? new HashMap<>()
                : dgInvestigationPackageRepository.findAllById(packageIds)
                .stream()
                .collect(Collectors.toMap(DgInvestigationPackage::getPackId, Function.identity()));

        Map<Long, List<PackageInvestigationMapping>> packageMappingsMap =
                packageIds.isEmpty()
                        ? new HashMap<>()
                        : packageInvestigationMappingRepository.findByPackageIdIn(new ArrayList<>(packagesMap.values()))
                        .stream()
                        .collect(Collectors.groupingBy(m -> m.getPackageId().getPackId()));


        try {
            LabRadiologyRegistrationResponse response = new LabRadiologyRegistrationResponse();
            Patient savedPatient = patientService.savePatient(patient, false);
            if (savedPatient == null) {
                throw new SDDException("patient",500,"Failed to save patient");
            }

            Visit visit = createVisitForLabRadio(savedPatient, radiologyDepartment);

            Map<LocalDate, List<LabRadioInvestigationRequest>> groupedByDate =
                    investigationReq.stream()
                            .filter(i -> i.getAppointmentDate() != null)
                            .collect(Collectors.groupingBy(LabRadioInvestigationRequest::getAppointmentDate));

            List<LabRadiologyRegistrationResponse.BillingDto> billingDtoList = new ArrayList<>();


            for (Map.Entry<LocalDate, List<LabRadioInvestigationRequest>> entry : groupedByDate.entrySet()) {

                LocalDate date = entry.getKey();
                List<LabRadioInvestigationRequest> investigations = entry.getValue();
                LabRadioCalculateAmountDTO amount = calculateAmount(investigations, serviceCategory);
                RadOrderHd orderHd = saveOrderHeader(savedPatient, visit, date, userName, currentUser);
                if (orderHd == null) {
                    throw new SDDException("RadOrderHeader",500,"Failed to create order header");
                }
                BillingHeader billing = billingService.saveBillingHeader(
                        orderHd, visit, currentUser,
                        amount.getTotal(), amount.getTax(),
                        amount.getDiscount(), serviceCategoryRad, true
                );
                if (billing == null) {
                    throw new SDDException("billing",500,"Failed to create billing");
                }
                Visit v = visitRepository.getReferenceById(visit.getId());
                visit.setBillingHd(billing);
                visitRepository.save(visit);

                response.setBillinghdId(billing.getId());

                collectBillingDtos(investigations, billing, billingDtoList);

                saveOrderDetailsOptimized(
                        orderHd, billing, investigations,
                        userName, currentUser,
                        investigationsMap, packagesMap, packageMappingsMap
                );
            }
            response.setPatientId(savedPatient.getId());
            response.setBillingHdIds(billingDtoList);
            response.setMsg("Success");
            return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});

        } catch (Exception e) {
            log.error("Unexpected error in radiology registration", e);
            throw new SDDException(500,"Error while processing radiology booking");
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
        visit.setDisplayPatientStatus("wp");

        String visitType = helperUtils.getVisitTypeForFollowUpOrNew(patient.getId());
        visit.setVisitType(visitType);


        return visitRepository.save(visit);
    }

    /**
     * Collects billing DTOs for investigations with check status true
     */
    private void collectBillingDtos(List<LabRadioInvestigationRequest> investigations,
            BillingHeader billing, List<LabRadiologyRegistrationResponse.BillingDto> billingDtoList) {
        investigations.stream()
                .filter(inv -> Boolean.TRUE.equals(inv.getCheckStatus()))
                .forEach(inv -> {
                    LabRadiologyRegistrationResponse.BillingDto dto = 
                            new LabRadiologyRegistrationResponse.BillingDto();
                    dto.setBillingHdId(billing.getId().toString());
                    dto.setInvestigationId(inv.getId().toString());
                    dto.setInvestigationAmount(inv.getActualAmount());
                    billingDtoList.add(dto);
                });
    }
    
    /**
     * Optimized order details save with batch operations and pre-fetched data
     */
    private void saveOrderDetailsOptimized(RadOrderHd hd, BillingHeader billingHd,
            List<LabRadioInvestigationRequest> investigations, String userName, User user,
            Map<Long, DgMasInvestigation> investigationsMap,
            Map<Long, DgInvestigationPackage> packagesMap,
            Map<Long, List<PackageInvestigationMapping>> packageMappingsMap) {
        
        List<RadOrderDt> orderDetailsToSave = new ArrayList<>();
        
        for (LabRadioInvestigationRequest inv : investigations) {
            if (AppConstants.INVESTIGATION.toLowerCase().equalsIgnoreCase(inv.getType())) {
                DgMasInvestigation investigation = investigationsMap.get(inv.getId());
                if (investigation == null) {
                    log.warn("Investigation not found with ID: {}", inv.getId());
                    continue;
                }
                
                RadOrderDt dt = buildRadOrderDt(hd, billingHd, inv, investigation.getSubChargeCodeId());
                dt.setInvestigation(investigation);
                orderDetailsToSave.add(dt);
                billingService.saveBillingDetail(billingHd, dt, inv.getActualAmount(),inv.getDiscountedAmount(), serviceCategoryRad, true);


            } else if (AppConstants.PACKAGE.toLowerCase().equalsIgnoreCase(inv.getType())) {
                DgInvestigationPackage pkg = packagesMap.get(inv.getId());
                if (pkg == null) {
                    log.warn("Package not found with ID: {}", inv.getId());
                    continue;
                }
                
                List<PackageInvestigationMapping> mappings = 
                        packageMappingsMap.getOrDefault(inv.getId(), new ArrayList<>());
                
                for (PackageInvestigationMapping map : mappings) {
                    DgMasInvestigation invest = map.getInvestId();
                    RadOrderDt dt = buildRadOrderDt(hd, billingHd, inv, invest.getSubChargeCodeId());
                    dt.setInvestigation(invest);
                    dt.setPackageId(pkg);
                    orderDetailsToSave.add(dt);
                    billingService.saveBillingDetailPackage(billingHd, pkg, inv, serviceCategoryRad);
                }
            }
        }
        
        // Batch save all order details
        if (!orderDetailsToSave.isEmpty()) {
            radOrderDtRepository.saveAll(orderDetailsToSave);
            log.debug("Batch saved {} order details", orderDetailsToSave.size());
        }
    }
    
    /**
     * Builds a RadOrderDt entity with common fields
     */
    private RadOrderDt buildRadOrderDt(RadOrderHd hd, BillingHeader billing,
            LabRadioInvestigationRequest inv, MasSubChargeCode subChargeCode) {

        RadOrderDt dt = new RadOrderDt();
        dt.setRadOrderhd(hd);
        dt.setSubChargecode(subChargeCode);
        dt.setOrderAccessionNo(transactionSequenceService.generateTransactionNumber(HMISTransaction.RADIOLOGY_NO, hd.getHospital().getId()));
        dt.setAppointmentDate(inv.getAppointmentDate());
        dt.setBillingHd(billing);
        dt.setOrderStatus(AppConstants.STATUS_Y.toLowerCase());
        dt.setBillingStatus(AppConstants.PAYMENT_NOT_PAID.toLowerCase());
        dt.setStudyStatus(AppConstants.STATUS_N.toLowerCase());
        dt.setReportStatus(AppConstants.STATUS_N.toLowerCase());
        dt.setHl7MwlStatus(AppConstants.STATUS_N.toLowerCase());
        dt.setPacsCompletionStatus(AppConstants.STATUS_N.toLowerCase());
        dt.setCreatedby(getCurrentUserName());
        dt.setCreatedon(Instant.now());
        dt.setLastChgBy(getCurrentUserName());
        dt.setLastChgDate(Instant.now());
        
        return dt;
    }
    
    /**
     * Gets current user full name
     */
    private String getCurrentUserName() {
        User user = authUtil.getCurrentUser();
        return user.getFirstName() + " " + user.getLastName();
    }

    
    private RadOrderHd saveOrderHeader(Patient patient, Visit visit, LocalDate date, String userName, User user) {
        RadOrderHd hd = new RadOrderHd();
        hd.setAppointmentDate(date);
        hd.setPaymentStatus(AppConstants.STATUS_N.toLowerCase());
        hd.setOrderDate(LocalDate.now());
        hd.setOrderTime(Instant.now());
        hd.setPatient(patient);
        hd.setVisit(visit);
        hd.setDepartment(visit.getDepartment());
        hd.setHospital(visit.getHospital());
        hd.setCreatedby(userName);
        hd.setCreatedon(Instant.now());
        hd.setLastChgBy(userName);
        hd.setLastChgDate(Instant.now());
        return radOrderHdRepository.save(hd);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public LabRadioUpdateResponse updatePatientDetailsAndBooking(LabRadioUpdateRequest request) {

        if (request == null || request.getPatient() == null) {
            throw new SDDException("patient", 400, "Patient data is required");
        }

        log.info("Starting patient update and radiology booking for patient ID: {}",
                request.getPatient().getId());

        Patient patient = patientService.updatePatientDetails(request.getPatient(), true);
        if (patient == null) {
            throw new SDDException("patient", 500, "Failed to update patient");
        }

        List<LabRadioInvestigationRequest> investigations = request.getInvestigationReq();

        if (investigations == null || investigations.isEmpty()) {
            return new LabRadioUpdateResponse(null, null, "Patient updated successfully");
        }

        Visit visit = createVisitForLabRadio(patient, radiologyDepartment);

        MasServiceCategory serviceCategory = masServiceCategoryRepository
                .findByServiceCateCode(serviceCategoryRad);

        if (serviceCategory == null) {
            throw new SDDException("serviceCategory", 400, "Invalid service category");
        }

        User currentUser = authUtil.getCurrentUser();
        String userName = getCurrentUserName();

        List<Long> investigationIds = new ArrayList<>();
        List<Long> packageIds = new ArrayList<>();

        for (LabRadioInvestigationRequest i : investigations) {
            if (AppConstants.INVESTIGATION.toLowerCase().equalsIgnoreCase(i.getType())) {
                investigationIds.add(i.getId());
            } else if (AppConstants.PACKAGE.toLowerCase().equalsIgnoreCase(i.getType())) {
                packageIds.add(i.getId());
            } else {
                throw new SDDException("type", 400, "Invalid investigation type");
            }
        }

        Map<Long, DgMasInvestigation> investigationsMap = investigationIds.isEmpty()
                ? new HashMap<>()
                : dgMasInvestigationRepository.findAllById(investigationIds)
                .stream()
                .collect(Collectors.toMap(DgMasInvestigation::getInvestigationId, Function.identity()));

        Map<Long, DgInvestigationPackage> packagesMap = packageIds.isEmpty()
                ? new HashMap<>()
                : dgInvestigationPackageRepository.findAllById(packageIds)
                .stream()
                .collect(Collectors.toMap(DgInvestigationPackage::getPackId, Function.identity()));

        Map<Long, List<PackageInvestigationMapping>> packageMappingsMap =
                packageIds.isEmpty()
                        ? new HashMap<>()
                        : packageInvestigationMappingRepository.findByPackageIdIn(new ArrayList<>(packagesMap.values()))
                        .stream()
                        .collect(Collectors.groupingBy(m -> m.getPackageId().getPackId()));

        Map<LocalDate, List<LabRadioInvestigationRequest>> groupedByDate =
                investigations.stream()
                        .filter(i -> i.getAppointmentDate() != null)
                        .collect(Collectors.groupingBy(LabRadioInvestigationRequest::getAppointmentDate));

        Long billingId = null;
        List<Long> billingHdIds = new ArrayList<>();

        try {
            for (Map.Entry<LocalDate, List<LabRadioInvestigationRequest>> entry : groupedByDate.entrySet()) {

                LocalDate date = entry.getKey();
                List<LabRadioInvestigationRequest> dateInvestigations = entry.getValue();

                LabRadioCalculateAmountDTO amount = calculateAmount(dateInvestigations, serviceCategory);

                RadOrderHd orderHd = saveOrderHeader(patient, visit, date, userName, currentUser);
                if (orderHd == null) {
                    throw new SDDException("order", 500, "Failed to create order");
                }

                BillingHeader billing = billingService.saveBillingHeader(
                        orderHd, visit, currentUser,
                        amount.getTotal(), amount.getTax(),
                        amount.getDiscount(), serviceCategoryRad, true
                );

                if (billing == null) {
                    throw new SDDException("billing", 500, "Failed to create billing");
                }
                Visit v = visitRepository.getReferenceById(visit.getId());
                v.setBillingHd(billing);
                visitRepository.save(v);

                billingId = billing.getId();
                billingHdIds.add(billingId);

                saveOrderDetailsOptimized(orderHd, billing, dateInvestigations,
                        userName, currentUser, investigationsMap, packagesMap, packageMappingsMap);
            }

        } catch (SDDException e) {
            log.error("Business error: {}", e.getMessage());
            throw e;

        } catch (Exception e) {
            log.error("Unexpected error", e);
            throw new SDDException("system", 500, "Error while processing booking");
        }

        log.info("Completed successfully, total billings={}", billingHdIds.size());

        return new LabRadioUpdateResponse(
                billingId,
                billingHdIds,
                AppConstants.PATIENT_UPDATED_BOOKING_SUCCESS
        );
    }


    @Override
    @Transactional
    public ApiResponse paymentStatusReq(PaymentUpdateRequest request) {
        PaymentResponse res = new PaymentResponse();
        log.info("Starting payment status update process");
        log.debug("Received PaymentUpdateRequest: {}", request);
        User currentUser = authUtil.getCurrentUser();
        try{

            //Payment table data inserted
            // User currentUser = authUtil.getCurrentUser();
            PaymentDetail paymentDetail = new PaymentDetail();
            paymentDetail.setPaymentMode(request.getMode());
            paymentDetail.setPaymentStatus("y");
            paymentDetail.setPaymentReferenceNo(request.getPaymentReferenceNo());
            paymentDetail.setPaymentDate(Instant.now());
            paymentDetail.setAmount(request.getAmount());
            paymentDetail.setCreatedBy(authUtil.getCurrentUser().getFirstName());
            paymentDetail.setCreatedAt(Instant.now());
            paymentDetail.setUpdatedAt(Instant.now());
            paymentDetail.setBillingHd(billingHeaderRepository.findById(request.getBillHeaderId()).get());
            PaymentDetail details = paymentDetailRepository.save(paymentDetail);
            log.info("PaymentDetail saved successfully, PaymentDetailId={}",
                    details.getId());

            for (InvestigationandPackegBillStatus invpkg : request.getInvestigationandPackegBillStatus()) {
                if (invpkg.getType().equalsIgnoreCase("i")) {
                    int investigationId = invpkg.getId();
                    int billHdId = request.getBillHeaderId();
                    log.debug("Updating payment status for InvestigationId={}, BillHdId={}",
                            investigationId, billHdId);
                    billingDetailRepository.updatePaymentStatusInvestigation("y",currentUser, investigationId, billHdId);
                    radOrderDtRepository.updatePaymentStatusInvestigationDt("y", investigationId, billHdId);
                } else {
                    int pkgId = invpkg.getId();
                    int billHdId = request.getBillHeaderId();
                    log.debug("Updating payment status for PackageId={}, BillHdId={}",
                            pkgId, billHdId);

                    //for package
                    billingDetailRepository.updatePaymentStatusPackage("y",currentUser, pkgId, billHdId);
                    radOrderDtRepository.updatePaymentStatusPackegDt("y",(long) pkgId,(long) billHdId);
                }
            }
            boolean fullyPaid = true;
            boolean partialPaid = false;
            List<RadOrderDt> dtList = radOrderDtRepository.findUnbilledByBillingHdId((long)request.getBillHeaderId());
            log.debug("Fetched OrderDt count={}", dtList.size());
            for (RadOrderDt orderDt : dtList) {
                if (orderDt.getBillingStatus().equalsIgnoreCase("n")) {
                    fullyPaid = false;
                    partialPaid = true;
                    break;
                }
            }
            BillingHeader billingHeader = billingHeaderRepository.findById(request.getBillHeaderId()).get();
            RadOrderHd hdorderObj = billingHeader.getRadOrderHd();
            Visit visit = visitRepository.findByBillingHd(billingHeader);
            res.setBillNo(billingHeader.getBillNo());
            res.setPaymentStatus(billingHeader.getPaymentStatus());

            log.info("Payment calculation => fullyPaid={}, partialPaid={}",
                    fullyPaid, partialPaid);

            if (fullyPaid) {
                hdorderObj.setPaymentStatus("y");
                visit.setBillingStatus("y");
                billingHeader.setPaymentStatus("y");
                res.setPaymentStatus("y");
                BigDecimal totalPaidDB = (billingHeader.getTotalPaid() != null) ? billingHeader.getTotalPaid() : BigDecimal.ZERO;
                BigDecimal totalPaidUi = (request.getAmount() != null) ? request.getAmount() : BigDecimal.ZERO;
                billingHeader.setTotalPaid(totalPaidDB.add(totalPaidUi));
                log.info("Fully paid. TotalPaid updated={}",
                        billingHeader.getTotalPaid());
            } else if (partialPaid) {
                hdorderObj.setPaymentStatus("p");
                visit.setBillingStatus("p");
                billingHeader.setPaymentStatus("p");
                res.setPaymentStatus("p");
                BigDecimal totalPaidDB = (billingHeader.getTotalPaid() != null) ? billingHeader.getTotalPaid() : BigDecimal.ZERO;
                BigDecimal totalPaidUi = (request.getAmount() != null) ? request.getAmount() : BigDecimal.ZERO;
                billingHeader.setTotalPaid(totalPaidDB.add(totalPaidUi));
                log.info("Partial payment. TotalPaid updated={}",
                        billingHeader.getTotalPaid());
            }
            log.info("Payment status updated successfully for BillHeaderId={}",
                    request.getBillHeaderId());
            radOrderHdRepository.save(hdorderObj);
            visitRepository.save(visit);
            billingHeaderRepository.save(billingHeader);
        } catch (SDDException e) {
            log.error("SDDException occurred during payment update", e);
            e.printStackTrace();
            return ResponseUtils.createFailureResponse(res, new TypeReference<>() {}, e.getMessage(), e.getStatus());
        } catch (Exception e) {
            log.error("Unexpected error during payment status update", e);

            e.printStackTrace();
            return ResponseUtils.createFailureResponse(res, new TypeReference<>() {}, "Internal Server Error", 500);
        }
        res.setMsg("Success");
        log.info("Payment status update completed successfully");
        return ResponseUtils.createSuccessResponse(res, new TypeReference<PaymentResponse>() {});
    }
    @Transactional
    @Override
    public ApiResponse<Page<RadiologyRequisitionResponse>> getPendingRadiology(Long modalityId, String patientName, String phoneNumber, int page, int size) {
        try {
            User currentUser = authUtil.getCurrentUser();
            MasHospital masHospital = masHospitalRepository.findById(currentUser.getHospital().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid hospital ID"));
            String patientLike = patientName == null ? null : "%" + patientName.toLowerCase() + "%";
            String phoneLike   = phoneNumber == null ? null : "%" + phoneNumber + "%";

            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdon"));
            Page<RadOrderDt> paged = radOrderDtRepository.findPendingRadiology(masHospital.getId(), "y", "n", modalityId, patientLike, phoneLike, pageable
            );

            Page<RadiologyRequisitionResponse> response= paged.map(this::mapToRadiologyDto);
            return ResponseUtils.createSuccessResponse(response, new TypeReference<Page<RadiologyRequisitionResponse>>() {});
        } catch (Exception e) {
            log.error("Error while fetching pending radiology data", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {}, "Internal Server Error", 500
            );
        }
    }

    @Override
    public ApiResponse<String> cancelOrCompleteInvestigationRadiology(Long id, String status) {
        try{
            log.info("pendingInvestigationRadiology called with id={}, status={}", id, status);
            Optional<RadOrderDt> radOrderDt = radOrderDtRepository.findById(id);
            if (radOrderDt.isEmpty()) {
                log.warn("Radiology order detail not found for id={}", id);
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {}, "Radiology order detail not found", 404
                );
            }

            RadOrderDt radDt = radOrderDt.get();
            radDt.setStudyStatus(status);
            radOrderDtRepository.save(radDt);

            if (AppConstants.STATUS_Y.equalsIgnoreCase(status)
                    || AppConstants.VISIT_STATUS_CANCELLED.equalsIgnoreCase(status)) {
                Visit visit = Optional.ofNullable(radDt.getRadOrderhd())
                        .map(RadOrderHd::getVisit)
                        .orElse(null);

                if (visit != null) {
                    if (AppConstants.STATUS_Y.equalsIgnoreCase(status)) {
                        visit.setVisitStatus(AppConstants.VISIT_STATUS_COMPLETED.toLowerCase());
                    } else {
                        visit.setVisitStatus(AppConstants.VISIT_STATUS_CANCELLED.toLowerCase());
                    }
                    visitRepository.save(visit);
                    log.info("Visit status updated successfully for visitId={} newStatus={}",
                            visit.getId(), visit.getVisitStatus());
                } else {
                    log.warn("No linked visit found for radiology order detail id={} while status={}", id, status);
                }
            }

            log.info("Study status updated successfully for id={} newStatus={}",
                    id, radDt.getStudyStatus());
            return ResponseUtils.createSuccessResponse("status change successfully", new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error while updating study status for id={}, status={}", id, status, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {}, "Internal Server Error", 500
            );
        }
    }
    @Override
    public ApiResponse<Page<RadiologyRequisitionResponse>> getPendingListForRadiologyReport(
            Long modality, String patientName, String phoneNumber, int page, int size) {
        try {
            User currentUser = authUtil.getCurrentUser();
            MasHospital masHospital = masHospitalRepository.findById(currentUser.getHospital().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid hospital ID"));
            String patientLike = patientName == null ? null : "%" + patientName.toLowerCase() + "%";
            String phoneLike   = phoneNumber == null ? null : "%" + phoneNumber + "%";

            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdon"));
            List<String> reportStatues = Stream.of(AppConstants.STATUS_N, AppConstants.STATUS_S).map(String::toLowerCase).toList();
            Page<RadiologyProjection> paged = radOrderDtRepository.getPendingReportRadiologyProjection(masHospital.getId(), AppConstants.STATUS_Y,reportStatues , modality, patientLike, phoneLike, pageable);
            Page<RadiologyRequisitionResponse> response = paged.map(this::toResponse);
            return ResponseUtils.createSuccessResponse(
                    response, new TypeReference<Page<RadiologyRequisitionResponse>>() {}
            );

        } catch (Exception e) {
            log.error("Error while fetching pending radiology data", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {}, "Internal Server Error", 500
            );
        }
    }
    @Transactional
    @Override
    public ApiResponse<String> saveDetailsReportForRadiology(RadiologyReportRequest request,String status) {
        try {
            User currentUser = authUtil.getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createNotFoundResponse("current user not found", 404
                );
            }
            RadOrderDt orderDt = radOrderDtRepository.findById(request.getRadOrderDtId()).orElse(null);
            if (orderDt == null) {
                return ResponseUtils.createNotFoundResponse(
                        "RadOrderDt not found for id: " + request.getRadOrderDtId(), 404
                );
            }
            RadStudyReport radStudyReport = radStudyReportRepository
                    .findTopByRadOrderDt_IdOrderByRadStudyReportIdDesc(request.getRadOrderDtId())
                    .orElseGet(RadStudyReport::new);

            radStudyReport.setRadOrderDt(orderDt);
            radStudyReport.setReportDesc(request.getReportDesc());
            radStudyReport.setReportStatus(status.toLowerCase().trim());
            radStudyReport.setLastChgBy(currentUser.getFullName());
            radStudyReport.setLastChgDate(LocalDateTime.now());
            if (radStudyReport.getRadStudyReportId() == null) {
                radStudyReport.setCreatedBy(currentUser.getUserId());
                radStudyReport.setCreatedOn(LocalDateTime.now());
            }
            // radStudyReport.setReportImagePath();
            radStudyReportRepository.save(radStudyReport);
            orderDt.setReportStatus(status.toLowerCase().trim());
            orderDt.setLastChgDate(Instant.now());
            orderDt.setLastChgBy(currentUser.getFullName());
            orderDt.setReportDate(LocalDate.now());
            return ResponseUtils.createSuccessResponse(
                    "Radiology result saved successfully", new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error while saving radiology report", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Internal Server Error", 500
            );
        }

    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<RadiologyReportResponse> getDetailsReportForRadiology(Long radOrderDtId) {
        try {
            RadOrderDt orderDt = radOrderDtRepository.findById(radOrderDtId).orElse(null);
            if (orderDt == null) {
                return ResponseUtils.createNotFoundResponse("RadOrderDt not found for id: " + radOrderDtId, 404);
            }

            RadStudyReport report = radStudyReportRepository
                    .findTopByRadOrderDt_IdOrderByRadStudyReportIdDesc(radOrderDtId)
                    .orElse(null);
            if (report == null) {
                return ResponseUtils.createNotFoundResponse("Radiology report not found for radOrderDtId: " + radOrderDtId, 404);
            }

            RadiologyReportResponse response = new RadiologyReportResponse();
            response.setRadStudyReportId(report.getRadStudyReportId());
            response.setRadOrderDtId(orderDt.getId());
            response.setReportDesc(report.getReportDesc());
            response.setReportImagePath(report.getReportImagePath());
            response.setReportStatus(orderDt.getReportStatus());

            return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error while fetching radiology report details for radOrderDtId={}", radOrderDtId, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {}, "Internal Server Error", 500
            );
        }
    }

    @Override
    public ApiResponse<Page<RadiologyRequisitionResponse>> getPACSStudyList(Long modality, String patientName, String phoneNumber, int page, int size) {
        try {
            User currentUser = authUtil.getCurrentUser();
            MasHospital masHospital = masHospitalRepository.findById(currentUser.getHospital().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid hospital ID"));
            String patientLike = patientName == null ? null : "%" + patientName.toLowerCase() + "%";
            String phoneLike   = phoneNumber == null ? null : "%" + phoneNumber + "%";

            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdon"));
            Page<RadiologyProjection> paged = radOrderDtRepository.getRadiologyPACSStudyList(masHospital.getId(),AppConstants.STATUS_Y, modality, patientLike, phoneLike, pageable);
            Page<RadiologyRequisitionResponse> response = paged.map(this::toResponse);
            return ResponseUtils.createSuccessResponse(
                    response, new TypeReference<Page<RadiologyRequisitionResponse>>() {}
            );

        } catch (Exception e) {
            log.error("Error while fetching pending radiology data", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {}, "Internal Server Error", 500
            );
        }
    }
    private RadiologyRequisitionResponse toResponse(RadiologyProjection p) {
        RadiologyRequisitionResponse r = new RadiologyRequisitionResponse();
        r.setAccessionNo(p.getOrderAccessionNo());
        r.setUhidNo(p.getUhid());
        r.setPatientName(p.getPatientName());
        r.setAge(p.getAge());
        r.setGender(p.getGender());
        r.setPhoneNumber(p.getMobileNo());
        r.setModality(p.getModalityName());
        r.setModalityId(p.getModalityId());
        r.setInvestigationName(p.getInvestigationName());
        r.setOrderDate(p.getOrderDate());
        r.setOrderTime(p.getOrderTime());
        r.setDepartment(p.getDepartment());
        r.setRadOrderDtId(p.getRadOrderdtId());
        r.setReportStatus(p.getReportStatus());
        r.setStudyStatus(p.getStudyStatus());
        r.setStudyDate(p.getStudyDatetime() != null ? p.getStudyDatetime().toLocalDate() : null);
        r.setStudyTime(p.getStudyDatetime());
        return r;
    }

    private RadiologyRequisitionResponse mapToRadiologyDto(RadOrderDt dt) {
        RadiologyRequisitionResponse dto = new RadiologyRequisitionResponse();
        dto.setRadOrderDtId(dt.getId());
        dto.setAccessionNo(dt.getOrderAccessionNo());
        RadOrderHd hd = dt.getRadOrderhd();
        Patient p =hd.getPatient();
        dto.setUhidNo(p.getUhidNo());
        dto.setPatientName(p.getFullName());
        dto.setAge(p.getPatientAge());
        dto.setPhoneNumber(p.getPatientMobileNumber());
        dto.setGender(p.getPatientGender() != null ? p.getPatientGender().getGenderName() : null);
        MasSubChargeCode sc = dt.getSubChargecode();
        dto.setModality(sc.getSubName());
        dto.setInvestigationName(dt.getInvestigation() != null ? dt.getInvestigation().getInvestigationName() : null);
        dto.setOrderDate(hd.getOrderDate());
        dto.setOrderTime(hd.getOrderTime());
        dto.setDepartment(hd.getDepartment() != null ? hd.getDepartment().getDepartmentName() : null);

        return dto;
    }
}
