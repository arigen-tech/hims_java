package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.exception.SDDException;
import com.hims.request.LabInvestigationReq;
import com.hims.request.LabRegRequest;
import com.hims.request.PatientRequest;
import com.hims.response.ApiResponse;
import com.hims.response.AppsetupResponse;
import com.hims.response.RadiologyAppSetupResponse;
import com.hims.service.RadiologyService;
import com.hims.utils.AuthUtil;
import com.hims.utils.RandomNumGenerator;
import com.hims.utils.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
    @Value("${serviceCategoryLab}")
    private String serviceCategoryRad;
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

    public RadiologyServiceImpl(RandomNumGenerator randomNumGenerator) {
        this.randomNumGenerator = randomNumGenerator;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<RadiologyAppSetupResponse> registerPatientWithInv(PatientRequest patient, List<LabInvestigationReq> radInvestigationReq) {
        log.info("Starting lab registration process");
        RadiologyAppSetupResponse response=new RadiologyAppSetupResponse();
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
            visit.setVisitStatus("n");
            visit.setBillingStatus("n");
            visit.setHospital(masHospital);
            visit.setTokenNo(existingTokens + 1);
            visit.setVisitDate(Instant.now());
            visit.setLastChgDate(Instant.now());
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
                response.setBillinghdId(headerId.getId().toString());
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
                        dt.setOrderAccessionNo(randomNumGenerator.generateOrderNumber("RAD",true,true));
                        dt.setRadOrderhd(savedHd);
                        dt.setInvestigation(invEntity);
                        dt.setSubChargecode(invEntity.getSubChargeCodeId());
                        dt.setAppointmentDate(inv.getAppointmentDate());
                        dt.setLastChgBy(currentUser.getFirstName()+" "+currentUser.getLastName());
                        dt.setCreatedby(currentUser.getFirstName()+" "+currentUser.getLastName());
                        dt.setBillingStatus("n");
                        dt.setCreatedon(Instant.now());
                        dt.setLastChgDate(Instant.now());
                        dt.setBillingHd(headerId);
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
                            dt.setOrderAccessionNo(randomNumGenerator.generateOrderNumber("RAD",true,true));
                            dt.setSubChargecode(investId.getSubChargeCodeId());
                            dt.setPackageId(pkgObj);
                            dt.setAppointmentDate(inv.getAppointmentDate());
                            dt.setLastChgBy(currentUser.getFirstName()+" "+currentUser.getLastName());
                            dt.setCreatedby(currentUser.getFirstName()+" "+currentUser.getLastName());
                            dt.setLastChgDate(Instant.now());
                            dt.setBillingStatus("n");
                            dt.setCreatedon(Instant.now());
                            dt.setOrderStatus("y");
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

    private BillingHeader BillingHeaderDataSave(RadOrderHd hdId, Visit vId, User currentUser, BigDecimal sum, BigDecimal tax, BigDecimal disc) {
        BillingHeader billingHeader = new BillingHeader();
        String orderNum = labRegistrationServices.createInvoices();
        billingHeader.setBillNo(orderNum);// Auto generated
        billingHeader.setPatient(vId.getPatient());
        billingHeader.setVisit(vId);
        billingHeader.setPatientDisplayName(vId.getPatient().getPatientFn());
        LocalDate dob=  vId.getPatient().getPatientDob();//get DOB from Patient table and calculate age
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
//        billingHeader.setTotalAmount(sum);//.subtract(disc).add(tax)
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
        billingDetail.setServiceCategory(masServiceCategoryRepository.findByServiceCateCode(serviceCategoryRad));//pass from property file..

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
        billingDetail.setPaymentStatus("n");

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
        billingDetail.setServiceCategory(masServiceCategoryRepository.findByServiceCateCode(serviceCategoryRad));//pass from property file..

        billingDetail.setItemName(dtId.getPackName()) ;  // investigation or packeg  name to be store
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
        billingDetail.setPaymentStatus("n");

        //not null column
        // billingDetail.setDetailId();
        // billingDetail.setChargeCost();
        // billingDetail.setOpdService(getOpdService().getId());
        ///calculation
        return  billingDetailRepository.save(billingDetail);
    }


}
