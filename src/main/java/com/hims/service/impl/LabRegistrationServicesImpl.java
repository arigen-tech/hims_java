package com.hims.service.impl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.exception.SDDException;
import com.hims.request.LabInvestigationReq;
import com.hims.request.LabPackegReq;
import com.hims.request.LabRegRequest;
import com.hims.response.ApiResponse;
import com.hims.response.AppsetupResponse;
import com.hims.service.LabRegistrationServices;
import com.hims.utils.AuthUtil;
import com.hims.utils.HelperUtils;
import com.hims.utils.RandomNumGenerator;
import com.hims.utils.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    MasServiceCategoryRepository masServiceCategoryRepository;

    public  LabRegistrationServicesImpl(RandomNumGenerator randomNumGenerator,
                                        BillingDetailRepository billingDetailRepository) {
        this.randomNumGenerator = randomNumGenerator;
        this.billingDetailRepository = billingDetailRepository;
    }

    public String createInvoice() {
        return randomNumGenerator.generateOrderNumber("HIMS",true,true);
    }

    public String createInvoices() {
        return randomNumGenerator.generateOrderNumber("BILL",true,true);
    }
    @Override
    @Transactional
    public ApiResponse<AppsetupResponse> labReg(LabRegRequest labReq) {
        Long departmentId = authUtil.getCurrentDepartmentId();
        User currentUser = authUtil.getCurrentUser();

        AppsetupResponse res = new AppsetupResponse();

        if (currentUser == null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Current user not found", HttpStatus.UNAUTHORIZED.value());
        }

        if (labReq.getPatientId() == null) {
            return ResponseUtils.createFailureResponse(res, new TypeReference<>() {},
                    "Patient ID must not be null", HttpStatus.BAD_REQUEST.value());
        }

        if (departmentId == null) {
            return ResponseUtils.createFailureResponse(res, new TypeReference<>() {},
                    "Current department ID is null", HttpStatus.BAD_REQUEST.value());
        }

        MasDepartment department = masDepartmentRepository.findById(departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid department ID: " + departmentId));

        Patient patient = patientRepository.findById(labReq.getPatientId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid patient ID: " + labReq.getPatientId()));

        MasHospital masHospital = masHospitalRepository.findById(currentUser.getHospital().getId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid hospital ID"));

        Long existingTokens = visitRepository.countTokensForToday(currentUser.getHospital().getId(), departmentId);
        try {
        Visit visit = new Visit();
        visit.setPatient(patient);
        visit.setVisitStatus("n");
        visit.setBillingStatus("p");
        visit.setHospital(masHospital);
        visit.setTokenNo(existingTokens + 1);
       // visit.setPreConsultation("y");
        visit.setVisitDate(Instant.now());
        visit.setLastChgDate(Instant.now());
        visit.setDepartment(department);
        Visit savedVisit = visitRepository.save(visit);
            // Validate all investigation appointment dates
            for (LabInvestigationReq inv : labReq.getLabInvestigationReq()) {
                if (inv.getAppointmentDate() == null) {
                    throw new IllegalArgumentException("Investigation appointment date must not be null for investigationId: " + inv.getInvestigationId());
                }
            }
            // Group investigations by appointment date (safely)
            Map<LocalDate, List<LabInvestigationReq>> grouped = labReq.getLabInvestigationReq().stream()
                    .filter(req -> req.getAppointmentDate() != null)
                    .collect(Collectors.groupingBy(LabInvestigationReq::getAppointmentDate));
            for (Map.Entry<LocalDate, List<LabInvestigationReq>> entry : grouped.entrySet()) {
                LocalDate date = entry.getKey();
                List<LabInvestigationReq> investigations = entry.getValue();
                BigDecimal sum=BigDecimal.ZERO;
                BigDecimal tax=BigDecimal.ZERO;
                BigDecimal disc=BigDecimal.ZERO;
                MasServiceCategory servCat = masServiceCategoryRepository.findByServiceCateCode(HelperUtils.SERVICECATEGORY);
                for(LabInvestigationReq inves:investigations){
                    if(inves.isCheckStatus()){
                        sum=sum.add(BigDecimal.valueOf(inves.getActualAmount()));
                        disc=disc.add(BigDecimal.valueOf(inves.getDiscountedAmount()));
                        if(servCat.getGstApplicable()){
                            tax=tax.add(BigDecimal.valueOf(servCat.getGstPercent()).multiply(BigDecimal.valueOf(inves.getActualAmount()).subtract(BigDecimal.valueOf(inves.getDiscountedAmount()))).divide(BigDecimal.valueOf(100)));
                        }
                    }
                }

                DgOrderHd hd = new DgOrderHd();
                hd.setAppointmentDate(date);
                hd.setOrderDate(LocalDate.now());
                hd.setOrderNo(createInvoice());
                hd.setOrderStatus("p");
                hd.setCollectionStatus("p");
                hd.setPaymentStatus("p");
                hd.setCreatedBy(Math.toIntExact(currentUser.getUserId()));
                hd.setHospitalId(Math.toIntExact(currentUser.getHospital().getId()));
                hd.setDiscountId(1);
                hd.setPatientId(patient);
                hd.setVisitId(savedVisit);
                hd.setDepartmentId(departmentId.intValue());
                hd.setLastChgBy(String.valueOf(currentUser.getUserId()));

                DgOrderHd savedHd = labHdRepository.save(hd);

                boolean flag=false;
                for (LabInvestigationReq req:investigations){
                    if(req.isCheckStatus()){
                        flag=true;
                        break;
                    }
                }
                BillingHeader headerId=new BillingHeader();
                if(flag){
                     headerId = BillingHeaderDataSave(savedHd, savedVisit, labReq, currentUser,sum,tax,disc);
                }
                for (LabInvestigationReq inv : investigations) {
                    if (inv.getInvestigationId() == null) {
                        throw new IllegalArgumentException("Investigation ID must not be null");
                    }

                    DgMasInvestigation invEntity = investigation.findById(inv.getInvestigationId())
                            .orElseThrow(() -> new IllegalArgumentException("Invalid Investigation ID: " + inv.getInvestigationId()));

                    DgOrderDt dt = new DgOrderDt();
                    dt.setInvestigationId(invEntity);
                    dt.setOrderhdId(savedHd);
                    dt.setCreatedBy(Math.toIntExact(currentUser.getUserId()));
                    dt.setMainChargecodeId(invEntity.getMainChargeCodeID().getChargecodeId());
                    dt.setAppointmentDate(inv.getAppointmentDate());
                    dt.setLastChgBy(String.valueOf(currentUser.getUserId()));
                    dt.setLastChgDate(LocalDate.now());
                    dt.setBillingStatus("p");

                    DgOrderDt savedDt = labDtRepository.save(dt);
                    if(inv.isCheckStatus()){
                        BillingDetaiDataSave(headerId, savedDt,inv);
                    }

                }
            }

            // Handle Lab Packages
            Map<LocalDate, List<LabPackegReq>> groupPkg = labReq.getLabPackegReqs().stream()
                    .filter(req -> req.getAppointmentDate() != null)
                    .collect(Collectors.groupingBy(LabPackegReq::getAppointmentDate));
             for (Map.Entry<LocalDate, List<LabPackegReq>> entry : groupPkg.entrySet()) {
                 LocalDate getDate = entry.getKey();
                 List<LabPackegReq> getPkg = entry.getValue();
                 Long pkgId=0L;
                 LocalDate date = null;
                boolean check = false;
                 for (LabPackegReq req:getPkg) {
                       pkgId= req.getPackegId();
                       date= req.getAppointmentDate();
                       check=req.isCheckStatus();
               if(req.isCheckStatus()){
                  break;
                   }
                 }
                if (pkgId==null) {
                    throw new IllegalArgumentException("Package ID must not be null");
                }
                if (date == null) {
                    throw new IllegalArgumentException("Package appointment date must not be null");
                }
                 Long finalPkgId = pkgId;
                 DgInvestigationPackage pack = dgInvestigationPackageRepository.findById(pkgId)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid package ID: " + finalPkgId));

                DgOrderHd hd = new DgOrderHd();
                hd.setAppointmentDate(getDate);
                hd.setOrderDate(LocalDate.now());
                hd.setOrderNo(createInvoice());
                hd.setOrderStatus("p");
                hd.setCollectionStatus("p");
                hd.setPaymentStatus("p");
                hd.setCreatedBy(Math.toIntExact(currentUser.getUserId()));
                hd.setHospitalId(Math.toIntExact(currentUser.getHospital().getId()));
                hd.setDiscountId(1);
                hd.setPatientId(patient);
                hd.setVisitId(savedVisit);
                hd.setDepartmentId(departmentId.intValue());
                hd.setLastChgBy(String.valueOf(currentUser.getUserId()));

                DgOrderHd savedHd = labHdRepository.save(hd);
                BillingHeader headerId =new BillingHeader() ;
                 BigDecimal sum=BigDecimal.ZERO;
                 BigDecimal tax=BigDecimal.ZERO;
                 BigDecimal disc=BigDecimal.ZERO;
                 MasServiceCategory servCat = masServiceCategoryRepository.findByServiceCateCode(HelperUtils.SERVICECATEGORY);
                 for(LabPackegReq packag:getPkg){
                     if(packag.isCheckStatus()){
                         sum=sum.add(BigDecimal.valueOf(packag.getActualAmount()));
                         disc=disc.add(BigDecimal.valueOf(packag.getDiscountedAmount()));
                         if(servCat.getGstApplicable()){
                             tax=tax.add(BigDecimal.valueOf(servCat.getGstPercent()).multiply(BigDecimal.valueOf(packag.getActualAmount()).subtract(BigDecimal.valueOf(packag.getDiscountedAmount()))).divide(BigDecimal.valueOf(100)));
                         }
                     }
                 }
                if(check){
                     headerId = BillingHeaderDataSave(savedHd, savedVisit, labReq, currentUser, sum, tax, disc);
                }
                 for (LabPackegReq req:getPkg) {
                  DgInvestigationPackage pkgObj =  dgInvestigationPackageRepository.findById(req.getPackegId()).get();

                     List<PackageInvestigationMapping> mappings = packageInvestigationMappingRepository.findByPackageId(pkgObj);
                     for (PackageInvestigationMapping map : mappings) {
                         DgMasInvestigation inv = map.getInvestId();
                         DgOrderDt dt = new DgOrderDt();
                         dt.setOrderhdId(savedHd);
                         dt.setInvestigationId(inv);
                         dt.setCreatedBy(Math.toIntExact(currentUser.getUserId()));
                         dt.setMainChargecodeId(inv.getMainChargeCodeID().getChargecodeId());
                         dt.setPackageId(pkgObj);
                         dt.setAppointmentDate(req.getAppointmentDate());
                         dt.setLastChgDate(LocalDate.now());
                         dt.setBillingStatus("p");
                         dt.setLastChgBy(String.valueOf(currentUser.getUserId()));
                         DgOrderDt savedDt = labDtRepository.save(dt);
                       }
                     if(req.isCheckStatus()){
                         BillingDetaiDataSavePackage(headerId,pkgObj,req);
                      }
                 }
            }
        } catch (SDDException e) {
            return ResponseUtils.createFailureResponse(res, new TypeReference<>() {}, e.getMessage(), e.getStatus());
        } catch (Exception e) {
            e.printStackTrace(); // log exception for debugging
            return ResponseUtils.createFailureResponse(res, new TypeReference<>() {}, "Internal Server Error", 500);
        }
        res.setMsg("Success");
        return ResponseUtils.createSuccessResponse(res, new TypeReference<AppsetupResponse>() {});
    }
    private BillingHeader BillingHeaderDataSave(DgOrderHd hdId, Visit vId, LabRegRequest labReq, User currentUser, BigDecimal sum, BigDecimal tax, BigDecimal disc) {
      //  if(!labReq.getLabPackegReqs().isEmpty()) {
            BillingHeader billingHeader = new BillingHeader();
            String orderNum = createInvoices();
            billingHeader.setBillNo(orderNum);// Auto generated
            billingHeader.setPatient(vId.getPatient());
            //  Optional<Patient> patientDetails= patientRepository.findById(labReq.getPatientId());
            // billingHeader.setPatientId(Math.toIntExact(labReq.getPatientId()));
            billingHeader.setVisit(vId);
            billingHeader.setPatientDisplayName(vId.getPatient().getPatientFn());
            billingHeader.setPatientAge(Integer.parseInt(vId.getPatient().getPatientAge()));
            billingHeader.setPatientGender(vId.getPatient().getPatientGender().getGenderName());
            billingHeader.setPatientAddress(vId.getPatient().getPatientAddress1());
            billingHeader.setHospital(currentUser.getHospital());
            billingHeader.setHospitalName(vId.getPatient().getPatientHospital().getHospitalName());
            //billingHeader.setHospital_mobile_no(patientDetails.get);  column is not exist in Patient table
            //billingHeader.setHospital_gstin(patientDetails.get);  column is not exist in Patient table
             billingHeader.setServiceCategory(masServiceCategoryRepository.findByServiceCateCode(HelperUtils.SERVICECATEGORY));  ///for which table
            billingHeader.setReferredBy(vId.getDoctorName());//few doute
            //billingHeader.setGstn_bill_no("");
            billingHeader.setBillingDate(Instant.now());// what date will Pass  , I am Passing currentdate
            billingHeader.setTotalAmount(BigDecimal.valueOf(labReq.getTotalAmount()));//
            billingHeader.setPaymentStatus("p");
            billingHeader.setVisit(vId);
            billingHeader.setHdorder(hdId);///two Hd id is there  one for
            billingHeader.setBillingHdId(hdId.getId());
            billingHeader.setTotalAmount(sum.subtract(disc).add(tax));
            billingHeader.setDiscountAmount(disc);
            billingHeader.setNetAmount(sum.subtract(disc).add(tax));
            billingHeader.setTaxTotal(tax);

            // investigation and ,one for Packeg so  ,both can be save

            //  billingHeader.setDiscount();//id is Pass
          //  billingHeader.setDiscountAmount(BigDecimal.valueOf(labReq.getDiscountAmount()));
            billingHeader.setCreatedBy(Long.toString(currentUser.getUserId()));
            billingHeader.setCreatedDt(Instant.now());
            billingHeader.setUpdatedDt(Instant.now());
           return  billingHeaderRepository.save(billingHeader);
    }
    private BillingDetail  BillingDetaiDataSave(BillingHeader bhdId, DgOrderDt dtId, LabInvestigationReq investigation){
        ///  Billing details
        BillingDetail billingDetail = new BillingDetail();
        billingDetail.setBillingHd(bhdId);
        billingDetail.setBillHd(bhdId);
         billingDetail.setServiceCategory(masServiceCategoryRepository.findByServiceCateCode(HelperUtils.SERVICECATEGORY));//pass from property file..

         billingDetail.setItemName(dtId.getInvestigationId().getInvestigationName()) ;  // investigation or packeg  name to be store
          billingDetail.setQuantity(1);//default
        billingDetail.setInvestigation(dtId.getInvestigationId());
        billingDetail.setPackageField(dtId.getPackageId());
        billingDetail.setCreatedDt(OffsetDateTime.now());
        billingDetail.setUpdatedDt(OffsetDateTime.now());
        billingDetail.setQuantity(1);
        billingDetail.setBasePrice(BigDecimal.valueOf(investigation.getActualAmount()));
        billingDetail.setDiscount(BigDecimal.valueOf(investigation.getDiscountedAmount()));
        billingDetail.setTariff(BigDecimal.valueOf(investigation.getActualAmount()));
        billingDetail.setAmountAfterDiscount(BigDecimal.valueOf(investigation.getActualAmount()));
        MasServiceCategory sevcat = masServiceCategoryRepository.findByServiceCateCode(HelperUtils.SERVICECATEGORY);
        BigDecimal tax=BigDecimal.ZERO;
        if(sevcat.getGstApplicable()){
            tax=BigDecimal.valueOf(sevcat.getGstPercent()).multiply(BigDecimal.valueOf(investigation.getActualAmount())).divide(BigDecimal.valueOf(100));
        }
        billingDetail.setTaxAmount(tax);
        billingDetail.setTaxPercent(BigDecimal.valueOf(sevcat.getGstPercent()));
        billingDetail.setNetAmount(billingDetail.getAmountAfterDiscount().add(billingDetail.getTaxAmount()));
        billingDetail.setTotal(billingDetail.getNetAmount());

        //not null column
       // billingDetail.setDetailId();
       // billingDetail.setChargeCost();
        // billingDetail.setOpdService(getOpdService().getId());
        ///calculation
        return  billingDetailRepository.save(billingDetail);
    }


    private BillingDetail  BillingDetaiDataSavePackage(BillingHeader bhdId, DgInvestigationPackage pack, LabPackegReq req){
        ///  Billing details
        BillingDetail billingDetail = new BillingDetail();
        billingDetail.setBillingHd(bhdId);
        billingDetail.setBillHd(bhdId);
        billingDetail.setServiceCategory(masServiceCategoryRepository.findByServiceCateCode(HelperUtils.SERVICECATEGORY));//pass from property file..

        billingDetail.setItemName(pack.getPackName()) ;  // investigation or packeg  name to be store
        ///  billingDetail.set
        //billingDetail.setInvestigation(dtId.getInvestigationId());
        billingDetail.setPackageField(pack);
        billingDetail.setCreatedDt(OffsetDateTime.now());
        billingDetail.setUpdatedDt(OffsetDateTime.now());


        billingDetail.setQuantity(1);
        billingDetail.setBasePrice(BigDecimal.valueOf(req.getActualAmount()));
        billingDetail.setDiscount(BigDecimal.valueOf(req.getDiscountedAmount()));
        billingDetail.setTariff(BigDecimal.valueOf(req.getActualAmount()));
        billingDetail.setAmountAfterDiscount(BigDecimal.valueOf(req.getActualAmount()));
        MasServiceCategory sevcat = masServiceCategoryRepository.findByServiceCateCode(HelperUtils.SERVICECATEGORY);
        BigDecimal tax=BigDecimal.ZERO;
        if(sevcat.getGstApplicable()){
            tax=BigDecimal.valueOf(sevcat.getGstPercent()).multiply(BigDecimal.valueOf(req.getActualAmount())).divide(BigDecimal.valueOf(100));
        }
        billingDetail.setTaxAmount(tax);
        billingDetail.setTaxPercent(BigDecimal.valueOf(sevcat.getGstPercent()));
        billingDetail.setNetAmount(billingDetail.getAmountAfterDiscount().add(billingDetail.getTaxAmount()));
        billingDetail.setTotal(billingDetail.getNetAmount());

        //not null column
        // billingDetail.setDetailId();
        // billingDetail.setChargeCost();
        // billingDetail.setOpdService(getOpdService().getId());
        return  billingDetailRepository.save(billingDetail);
    }



}
