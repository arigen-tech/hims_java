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
import org.springframework.security.core.context.SecurityContextHolder;
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
        //{
        Long deapartmentId=authUtil.getCurrentDepartmentId();
        User currentUser = authUtil.getCurrentUser();
        if (currentUser == null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Current user not found", HttpStatus.UNAUTHORIZED.value());
        }
            AppsetupResponse res = new AppsetupResponse();
            // write logic here for save visit data & generate token
      //List<UserDepartment> departmentId=  userDepartmentRepository.findByUserId(currentUser.getUserId());

        Long existingTokens =visitRepository.countTokensForToday(currentUser.getHospital().getId(), deapartmentId);
        System.out.println("existingTokens:" + existingTokens);
       // currentUser.getHospital();
            Optional<Patient> patient = patientRepository.findById(labReq.getPatientId());
            Optional<MasHospital> masHospital= masHospitalRepository.findById(currentUser.getHospital().getId());
            Visit v=new Visit();
                v.setPatient(patient.get());
                v.setVisitStatus("p");
                v.setVisitStatus("n");
                v.setBillingStatus("p");
                v.setHospital(masHospital.get());
                v.setTokenNo(existingTokens+1);
                v.setPreConsultation("y");
                v.setVisitDate(Instant.now());
               Visit  vId = visitRepository.save(v);
            try {
                // for investigation  data save hd &dt
                //grouping same date  for header entry..
                Map<LocalDate, List<LabInvestigationReq>> grouped = labReq.getLabInvestigationReq().stream()
                        .collect(Collectors.groupingBy(LabInvestigationReq::getAppointmentDate));
                           //System.out.println("Duplicate appointmentDate found: " + grouped);
                    grouped.forEach((date, objects) -> {
                    DgOrderHd hd= new DgOrderHd();
                    //System.out.println("Date: " + date);
                   // header entry code  for date
                    hd.setAppointmentDate(date);
                    hd.setOrderDate(LocalDate.now());
                    String orderNum = createInvoice();
                    hd.setOrderNo(orderNum);
                    hd.setOrderStatus("p");
                    hd.setCollectionStatus("p");
                    hd.setPaymentStatus("p");
                    hd.setCreatedBy(Math.toIntExact(currentUser.getUserId()));
                    hd.setHospitalId(Math.toIntExact(currentUser.getHospital().getId()));
                    hd.setDiscountId(1);//null
                    hd.setPatientId(patient.get());
                    hd.setVisitId(vId);
                        DgOrderHd hdId = labHdRepository.save(hd);
                        BillingHeader headerId=BillingHeaderDataSave(hdId,vId,labReq,  currentUser);

                    for (LabInvestigationReq obj : objects) {
                        DgOrderDt dtInvesti = new DgOrderDt();
                        Optional<DgMasInvestigation> dgMasInvestigation = investigation.findById(obj.getInvestigationId());
                        dtInvesti.setInvestigationId(dgMasInvestigation.get());
                        dtInvesti.setOrderhdId(hdId);
                        dtInvesti.setCreatedBy(Math.toIntExact(currentUser.getUserId()));

                        DgMasInvestigation mainChargeCodeId= investigation.findByinvestigationId(obj.getInvestigationId());

                        dtInvesti.setMainChargecodeId(mainChargeCodeId.getMainChargeCodeID().getChargecodeId());
                        // ht.setPackageId(null);
                        dtInvesti.setAppointmentDate(obj.getAppointmentDate());
                        DgOrderDt dtId = labDtRepository.save(dtInvesti);
                        BillingDetaiDataSave(headerId,dtId);
                    }
                   // System.out.println();
                });
             //   / for Package data save hd &dt
                if(!labReq.getLabPackegReqs().isEmpty()) {
                    for (LabPackegReq key : labReq.getLabPackegReqs()) {
                        DgOrderHd hd1= new DgOrderHd();
                        hd1.setAppointmentDate(key.getAppointmentDate());
                        hd1.setOrderDate(LocalDate.now());
                        String formattedOrderNo =createInvoice();
                        hd1.setOrderNo(formattedOrderNo);
                        hd1.setOrderStatus("p");
                        hd1.setCollectionStatus("p");
                        hd1.setPaymentStatus("p");
                        hd1.setCreatedBy(Math.toIntExact(currentUser.getUserId()));
                        hd1.setHospitalId(Math.toIntExact(currentUser.getHospital().getId()));
                        hd1.setDiscountId(1);//null
                        hd1.setPatientId(patient.get());
                        hd1.setVisitId(vId);
                        DgOrderHd hdId1=labHdRepository.save(hd1);
                        BillingHeader headerId=BillingHeaderDataSave(hdId1,vId,labReq,  currentUser);

                        Long PackegId = key.getPackegId();
                        List<PackageInvestigationMapping> investi= packageInvestigationMappingRepository.findByPackageId(dgInvestigationPackageRepository.findById(PackegId).get());
                        for( int i=0;i<investi.size();i++){
                             DgOrderDt htPkg= new DgOrderDt();
                             DgMasInvestigation dg = investi.get(i).getInvestId();
                             htPkg.setOrderhdId(hdId1);
                             htPkg.setInvestigationId(dg);
                             htPkg.setCreatedBy(Math.toIntExact(currentUser.getUserId()));

                            DgMasInvestigation mainChargeCodeId= investigation.findByinvestigationId(dg.getInvestigationId());
                             htPkg.setMainChargecodeId(mainChargeCodeId.getMainChargeCodeID().getChargecodeId());
                             htPkg.setPackageId(dgInvestigationPackageRepository.findById(PackegId).get());
                             htPkg.setAppointmentDate(key.getAppointmentDate());
                             DgOrderDt dtId = labDtRepository.save(htPkg);
                            BillingDetaiDataSave(headerId,dtId);
                         }
                    }
                }

                res.setMsg("Success");
                return ResponseUtils.createSuccessResponse(res, new TypeReference<AppsetupResponse>() {});
            } catch (SDDException e) {
                return ResponseUtils.createFailureResponse(res, new TypeReference<AppsetupResponse>() {}, e.getMessage(), e.getStatus());
            } catch (Exception e) {
                return ResponseUtils.createFailureResponse(res, new TypeReference<AppsetupResponse>() {}, "Internal Server Error", 500);
            }
        }


    private BillingHeader BillingHeaderDataSave(DgOrderHd hdId, Visit vId, LabRegRequest labReq, User currentUser) {


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
            ///
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
            // investigation and ,one for Packeg so  ,both can be save

            //  billingHeader.setDiscount();//id is Pass
            billingHeader.setDiscountAmount(BigDecimal.valueOf(labReq.getDiscountAmount()));
            billingHeader.setCreatedBy(Long.toString(currentUser.getUserId()));
            billingHeader.setCreatedDt(Instant.now());
            billingHeader.setUpdatedDt(Instant.now());
           return  billingHeaderRepository.save(billingHeader);
//        }else{
//            return null;
//        }
    }
    private BillingDetail  BillingDetaiDataSave(BillingHeader bhdId, DgOrderDt dtId ){
        ///  Billing details
        BillingDetail billingDetail = new BillingDetail();
        billingDetail.setBillingHd(bhdId);
        billingDetail.setBillHd(bhdId);
         billingDetail.setServiceCategory(masServiceCategoryRepository.findByServiceCateCode(HelperUtils.SERVICECATEGORY));//pass from property file..

         billingDetail.setItemName(dtId.getInvestigationId().getInvestigationName()) ;  // investigation or packeg  name to be store
        ///  billingDetail.set
        billingDetail.setInvestigation(dtId.getInvestigationId());
        billingDetail.setPackageField(dtId.getPackageId());
        billingDetail.setCreatedDt(OffsetDateTime.now());
        billingDetail.setUpdatedDt(OffsetDateTime.now());
        //not null column
       // billingDetail.setDetailId();
       // billingDetail.setChargeCost();
        // billingDetail.setOpdService(getOpdService().getId());
        return  billingDetailRepository.save(billingDetail);
    }



}
