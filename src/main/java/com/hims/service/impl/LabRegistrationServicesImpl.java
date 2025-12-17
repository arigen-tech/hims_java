package com.hims.service.impl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.exception.SDDException;
import com.hims.helperUtil.ConverterUtils;
import com.hims.request.*;
import com.hims.response.*;
import com.hims.service.LabRegistrationServices;
import com.hims.utils.AuthUtil;
import com.hims.utils.RandomNumGenerator;
import com.hims.utils.ResponseUtils;
import com.lowagie.text.Header;
import jakarta.persistence.criteria.Root;
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

    @Value("${app.pending.days}")
    private int pendingDays;
    private String getCurrentTimeFormatted(Instant instant) {
        LocalTime time = instant
                .atZone(ZoneId.systemDefault())
                .toLocalTime();

        return time.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }



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
        visit.setBillingStatus("n");
        visit.setHospital(masHospital);
        visit.setTokenNo(existingTokens + 1);
        visit.setVisitDate(Instant.now());
        visit.setLastChgDate(Instant.now());
        visit.setDepartment(department);
        visit.setDisplayPatientStatus("wp");
        Visit savedVisit = visitRepository.save(visit);
            // Validate all investigation appointment dates
            for (LabInvestigationReq inv : labReq.getLabInvestigationReq()) {
                if (inv.getAppointmentDate() == null) {
                    throw new IllegalArgumentException("Investigation appointment date must not be null for investigationId: " + inv.getId());
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
                MasServiceCategory servCat = masServiceCategoryRepository.findByServiceCateCode(serviceCategoryLab);
                for(LabInvestigationReq inves:investigations){
                   // if(inves.isCheckStatus()){
                        sum=sum.add(BigDecimal.valueOf(inves.getActualAmount()));
                        disc=disc.add(BigDecimal.valueOf(inves.getDiscountedAmount()));
                    if(servCat.getGstApplicable()){
                        tax=tax.add(BigDecimal.valueOf(servCat.getGstPercent()).multiply(BigDecimal.valueOf(inves.getActualAmount()).subtract(BigDecimal.valueOf(inves.getDiscountedAmount()))).divide(BigDecimal.valueOf(100)));
                    }
                   // }
                }
                DgOrderHd hd = new DgOrderHd();
                hd.setAppointmentDate(date);
                hd.setOrderDate(LocalDate.now());
                hd.setOrderNo(createInvoice());
                hd.setOrderTime(Instant.now());
                hd.setOrderStatus("n");
                hd.setCollectionStatus("n");
                hd.setPaymentStatus("n");
                hd.setHospitalId(Math.toIntExact(currentUser.getHospital().getId()));
                hd.setDiscountId(1);
                hd.setPatientId(patient);
                hd.setVisitId(savedVisit);
                hd.setSource("lab source");
                hd.setDepartmentId(departmentId.intValue());
                hd.setLastChgBy(currentUser.getFirstName()+" "+currentUser.getLastName());
                hd.setCreatedBy(currentUser.getFirstName()+" "+currentUser.getLastName());
                hd.setCreatedOn(LocalDate.now());
                hd.setLastChgDate(LocalDate.now());
                hd.setLastChgTime(LocalTime.now().toString());
                DgOrderHd savedHd = labHdRepository.save(hd);
//                boolean flag=false;  //flag
//                for (LabInvestigationReq req:investigations){
//                    if(req.isCheckStatus()){
//                        flag=true;
//                        break;
//                    }
//                }//flag
                BillingHeader headerId=new BillingHeader();
               // if(flag){//flag
                     headerId = BillingHeaderDataSave(savedHd, savedVisit, labReq, currentUser,sum,tax,disc);
                    res.setBillinghdId(headerId.getId().toString());
                    savedVisit.setBillingHd(headerId);
                    visitRepository.save(savedVisit);
               // }
                for (LabInvestigationReq inv : investigations) {
                    //check, type= "i"  for  investigation   and  "p"  for packeg to differenciate
                    if (inv.getType().equalsIgnoreCase("i")) {
                        if (inv.getId() == null) {
                            throw new IllegalArgumentException("Investigation ID must not be null");
                        }
                        DgMasInvestigation invEntity = investigation.findById(inv.getId())
                                .orElseThrow(() -> new IllegalArgumentException("Invalid Investigation ID: " + inv.getId()));
                        DgOrderDt dt = new DgOrderDt();
                        dt.setInvestigationId(invEntity);
                        dt.setOrderhdId(savedHd);
                        dt.setMainChargecodeId(invEntity.getMainChargeCodeId().getChargecodeId());
                        dt.setSubChargeid(invEntity.getSubChargeCodeId().getSubId());
                        dt.setAppointmentDate(inv.getAppointmentDate());

                        dt.setLastChgBy(currentUser.getFirstName()+" "+currentUser.getLastName());
                        dt.setCreatedBy(currentUser.getFirstName()+" "+currentUser.getLastName());
                        dt.setLastChgDate(LocalDate.now());
                        dt.setBillingStatus("n");
                        dt.setOrderStatus("n");
                        dt.setOrderQty(1);
                        dt.setCreatedon(Instant.now());
                        dt.setLastChgTime(LocalTime.now().toString());

                        DgOrderDt savedDt = labDtRepository.save(dt);
                      //  if(flag){//flag
                            savedDt.setBillingHd(headerId);
                            labDtRepository.save(savedDt);
                       // }
                       // if (inv.isCheckStatus()) {//flag
                            BillingDetaiDataSave(headerId, savedDt, inv);
                       // }
                    } else {
                        DgInvestigationPackage pkgObj = dgInvestigationPackageRepository.findById(inv.getId()).get();
                        List<PackageInvestigationMapping> mappings = packageInvestigationMappingRepository.findByPackageId(pkgObj);
                        for (PackageInvestigationMapping map : mappings) {
                            DgMasInvestigation investId = map.getInvestId();
                            DgOrderDt dt = new DgOrderDt();
                            dt.setOrderhdId(savedHd);
                            dt.setInvestigationId(investId);
                            dt.setMainChargecodeId(investId.getMainChargeCodeId().getChargecodeId());
                            dt.setSubChargeid(investId.getSubChargeCodeId().getSubId());
                            dt.setPackageId(pkgObj);
                            dt.setAppointmentDate(inv.getAppointmentDate());

                            dt.setLastChgBy(currentUser.getFirstName()+" "+currentUser.getLastName());
                            dt.setCreatedBy(currentUser.getFirstName()+" "+currentUser.getLastName());
                            dt.setLastChgDate(LocalDate.now());
                            dt.setBillingStatus("n");
                            dt.setOrderStatus("n");
                            dt.setOrderQty(1);
                            dt.setCreatedon(Instant.now());
                            dt.setLastChgTime(LocalTime.now().toString());
                            DgOrderDt savedDt = labDtRepository.save(dt);
                            //if(flag) {
                                savedDt.setBillingHd(headerId);
                                labDtRepository.save(savedDt);
                           // }
                        }
                      //  if (inv.isCheckStatus()) {//flag
                            BillingDetaiDataSavePackage(headerId, pkgObj, inv);
//}
                   }
                }
            }
            // Handle Lab Packages
//            Map<LocalDate, List<LabPackegReq>> groupPkg = labReq.getLabPackegReqs().stream()
//                    .filter(req -> req.getAppointmentDate() != null)
//                    .collect(Collectors.groupingBy(LabPackegReq::getAppointmentDate));
//             for (Map.Entry<LocalDate, List<LabPackegReq>> entry : groupPkg.entrySet()) {
//                 LocalDate getDate = entry.getKey();
//                 List<LabPackegReq> getPkg = entry.getValue();
//                 Long pkgId=0L;
//                 LocalDate date = null;
//                boolean check = false;
//                 for (LabPackegReq req:getPkg) {
//                       pkgId= req.getPackegId();
//                       date= req.getAppointmentDate();
//                       check=req.isCheckStatus();
//               if(req.isCheckStatus()){
//                  break;
//                   }
//                 }
//                if (pkgId==null) {
//                    throw new IllegalArgumentException("Package ID must not be null");
//                }
//                if (date == null) {
//                    throw new IllegalArgumentException("Package appointment date must not be null");
//                }
//                 Long finalPkgId = pkgId;
//                 DgInvestigationPackage pack = dgInvestigationPackageRepository.findById(pkgId)
//                        .orElseThrow(() -> new IllegalArgumentException("Invalid package ID: " + finalPkgId));
//
//                DgOrderHd hd = new DgOrderHd();
//                hd.setAppointmentDate(getDate);
//                hd.setOrderDate(LocalDate.now());
//                hd.setOrderNo(createInvoice());
//                hd.setOrderStatus("p");
//                hd.setCollectionStatus("p");
//                hd.setPaymentStatus("n");
//                hd.setCreatedBy(Math.toIntExact(currentUser.getUserId()));
//                hd.setHospitalId(Math.toIntExact(currentUser.getHospital().getId()));
//                hd.setDiscountId(1);
//                hd.setPatientId(patient);
//                hd.setVisitId(savedVisit);
//                hd.setDepartmentId(departmentId.intValue());
//                hd.setLastChgBy(String.valueOf(currentUser.getUserId()));
//
//                DgOrderHd savedHd = labHdRepository.save(hd);
//                BillingHeader headerId =new BillingHeader() ;
//                 BigDecimal sum=BigDecimal.ZERO;
//                 BigDecimal tax=BigDecimal.ZERO;
//                 BigDecimal disc=BigDecimal.ZERO;
//                 MasServiceCategory servCat = masServiceCategoryRepository.findByServiceCateCode(HelperUtils.SERVICECATEGORY);
//                 for(LabPackegReq packag:getPkg){
//                     if(packag.isCheckStatus()){
//                         sum=sum.add(BigDecimal.valueOf(packag.getActualAmount()));
//                         disc=disc.add(BigDecimal.valueOf(packag.getDiscountedAmount()));
//                         if(servCat.getGstApplicable()){
//                             tax=tax.add(BigDecimal.valueOf(servCat.getGstPercent()).multiply(BigDecimal.valueOf(packag.getActualAmount()).subtract(BigDecimal.valueOf(packag.getDiscountedAmount()))).divide(BigDecimal.valueOf(100)));
//                         }
//                     }
//                 }
//                if(check){
//                     headerId = BillingHeaderDataSave(savedHd, savedVisit, labReq, currentUser, sum, tax, disc);
//                }
//                 for (LabPackegReq req:getPkg) {
//                  DgInvestigationPackage pkgObj =  dgInvestigationPackageRepository.findById(req.getPackegId()).get();
//
//                     List<PackageInvestigationMapping> mappings = packageInvestigationMappingRepository.findByPackageId(pkgObj);
//                     for (PackageInvestigationMapping map : mappings) {
//                         DgMasInvestigation inv = map.getInvestId();
//                         DgOrderDt dt = new DgOrderDt();
//                         dt.setOrderhdId(savedHd);
//                         dt.setInvestigationId(inv);
//                         dt.setCreatedBy(Math.toIntExact(currentUser.getUserId()));
//                         dt.setMainChargecodeId(inv.getMainChargeCodeID().getChargecodeId());
//                         dt.setPackageId(pkgObj);
//                         dt.setAppointmentDate(req.getAppointmentDate());
//                         dt.setLastChgDate(LocalDate.now());
//                         dt.setBillingStatus("n");
//                         dt.setLastChgBy(String.valueOf(currentUser.getUserId()));
//                         DgOrderDt savedDt = labDtRepository.save(dt);
//                       }
//                     if(req.isCheckStatus()){
//                         BillingDetaiDataSavePackage(headerId,pkgObj,req);
//                      }
//                 }
//            }
        } catch (SDDException e) {
            return ResponseUtils.createFailureResponse(res, new TypeReference<>() {}, e.getMessage(), e.getStatus());
        } catch (Exception e) {
            e.printStackTrace(); // log exception for debugging
            return ResponseUtils.createFailureResponse(res, new TypeReference<>() {}, "Internal Server Error", 500);
        }
        res.setMsg("Success");
        return ResponseUtils.createSuccessResponse(res, new TypeReference<AppsetupResponse>() {});
    }

    @Override
    @Transactional
    public ApiResponse paymentStatusReq(PaymentUpdateRequest request) {
        PaymentResponse res = new PaymentResponse();
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

                for (InvestigationandPackegBillStatus invpkg : request.getInvestigationandPackegBillStatus()) {
                    if (invpkg.getType().equalsIgnoreCase("i")) {
                        int investigationId = invpkg.getId();
                        int billHdId = request.getBillHeaderId();
                        billingDetailRepository.updatePaymentStatusInvestigation("y", investigationId, billHdId);
                        labDtRepository.updatePaymentStatusInvestigationDt("y", investigationId, billHdId);
                    } else {
                        int pkgId = invpkg.getId();
                        int billHdId = request.getBillHeaderId();
                        billingDetailRepository.updatePaymentStatuPackeg("y", pkgId, billHdId);
                        labDtRepository.updatePaymentStatusPackegDt("y", pkgId, billHdId);
                    }
                }
                boolean fullyPaid = true;
                boolean partialPaid = false;
                List<DgOrderDt> dtList = labDtRepository.findByStatus(request.getBillHeaderId());
                for (DgOrderDt orderDt : dtList) {
                    if (orderDt.getBillingStatus().equalsIgnoreCase("n")) {
                        fullyPaid = false;
                        partialPaid = true;
                        break;
                    }
//              else{
//                  partialPaid=false;
//                  fullyPaid=true;
//              }
                }
                BillingHeader billingHeader = billingHeaderRepository.findById(request.getBillHeaderId()).get();
                DgOrderHd hdorderObj = billingHeader.getHdorder();
                Visit visit = visitRepository.findByBillingHd(billingHeader);
                res.setBillNo(billingHeader.getBillNo());
                res.setPaymentStatus(billingHeader.getPaymentStatus());

                if (fullyPaid) {
                    hdorderObj.setPaymentStatus("y");
                    visit.setBillingStatus("y");
                    billingHeader.setPaymentStatus("y");
                    res.setPaymentStatus("y");
                    BigDecimal totalPaidDB = (billingHeader.getTotalPaid() != null) ? billingHeader.getTotalPaid() : BigDecimal.ZERO;
                    BigDecimal totalPaidUi = (request.getAmount() != null) ? request.getAmount() : BigDecimal.ZERO;
                    billingHeader.setTotalPaid(totalPaidDB.add(totalPaidUi));
                } else if (partialPaid) {
                    hdorderObj.setPaymentStatus("p");
                    visit.setBillingStatus("p");
                    billingHeader.setPaymentStatus("p");
                    res.setPaymentStatus("p");
                    BigDecimal totalPaidDB = (billingHeader.getTotalPaid() != null) ? billingHeader.getTotalPaid() : BigDecimal.ZERO;
                    BigDecimal totalPaidUi = (request.getAmount() != null) ? request.getAmount() : BigDecimal.ZERO;
                    billingHeader.setTotalPaid(totalPaidDB.add(totalPaidUi));
                }
                labHdRepository.save(hdorderObj);
                visitRepository.save(visit);
                billingHeaderRepository.save(billingHeader);

//            BillingHeader billingHeader=billingHeaderRepository.findById(request.getBillHeaderId()).get();
//            DgOrderHd hdorderObj = billingHeader.getHdorder();
//            List<DgOrderDt> dtList= labDtRepository.findByOrderhdId(hdorderObj);
//            List<BillingDetail> billDtList= billingDetailRepository.findByBillingHd(billingHeader);
//           boolean fullyPaid=true;
//            boolean partialPaid=false;
//            for(DgOrderDt orderDt:dtList){
//
//            for(BillingDetail billDt:billDtList) {
//
//                if (orderDt.getPackageId() != null && billDt.getPackageField()!=null) {
//                    if (billDt.getPackageField().getPackId() == orderDt.getPackageId().getPackId()) {
//                        orderDt.setBillingStatus("y");
//
//                    }
//                } else if (billDt.getInvestigation()!=null&& orderDt.getPackageId() == null ) {
//                if (billDt.getInvestigation().getInvestigationId()
//                        == orderDt.getInvestigationId().getInvestigationId()) {
//                    orderDt.setBillingStatus("y");
//                }
//               }
//              }
//            if(orderDt.getBillingStatus().equalsIgnoreCase("n")){
//                fullyPaid=false;
//            }
//            if(partialPaid!=true && orderDt.getBillingStatus().equalsIgnoreCase("y") ){
//                partialPaid=true;
//            }
//            //labDtRepository.save(orderDt);  //comments
//            }
//            /// Visit Payment status
//            Visit visit=visitRepository.findByBillingHd(billingHeader);
//            if(fullyPaid){
//                hdorderObj.setPaymentStatus("y");
//                visit.setBillingStatus("y");
//                billingHeader.setPaymentStatus("y");
//            }else if(partialPaid){
//                hdorderObj.setPaymentStatus("p");
//                visit.setBillingStatus("p");
//                billingHeader.setPaymentStatus("p");
//            }
//            labHdRepository.save(hdorderObj);
//            visitRepository.save(visit);
//            billingHeaderRepository.save(billingHeader);
        } catch (SDDException e) {
            return ResponseUtils.createFailureResponse(res, new TypeReference<>() {}, e.getMessage(), e.getStatus());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtils.createFailureResponse(res, new TypeReference<>() {}, "Internal Server Error", 500);
        }
        res.setMsg("Success");
        return ResponseUtils.createSuccessResponse(res, new TypeReference<PaymentResponse>() {});
    }

    private BillingHeader BillingHeaderDataSave(DgOrderHd hdId, Visit vId, LabRegRequest labReq, User currentUser, BigDecimal sum, BigDecimal tax, BigDecimal disc) {
            BillingHeader billingHeader = new BillingHeader();
            String orderNum = createInvoices();
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
            billingHeader.setCreatedBy(currentUser.getFirstName()+" "+currentUser.getLastName());
            billingHeader.setCreatedDt(Instant.now());
            billingHeader.setUpdatedDt(Instant.now());
            billingHeader.setBillDate(OffsetDateTime.now());
            billingHeader.setUpdatedAt(OffsetDateTime.now());
           return  billingHeaderRepository.save(billingHeader);
    }
    private BillingDetail  BillingDetaiDataSave(BillingHeader bhdId, DgOrderDt dtId, LabInvestigationReq investigation){
        ///  Billing details
        BillingDetail billingDetail = new BillingDetail();
        billingDetail.setBillingHd(bhdId);
        billingDetail.setBillHd(bhdId);
         billingDetail.setServiceCategory(masServiceCategoryRepository.findByServiceCateCode(serviceCategoryLab));//pass from property file..

         billingDetail.setItemName(dtId.getInvestigationId().getInvestigationName()) ;  // investigation or packeg  name to be store
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
    private BillingDetail  BillingDetaiDataSavePackage(BillingHeader bhdId, DgInvestigationPackage pack, LabInvestigationReq req){
        ///  Billing details
        BillingDetail billingDetail = new BillingDetail();
        billingDetail.setBillingHd(bhdId);
        billingDetail.setBillHd(bhdId);
        billingDetail.setServiceCategory(masServiceCategoryRepository.findByServiceCateCode(serviceCategoryLab));//pass from property file..

        billingDetail.setItemName(pack.getPackName()) ;  // investigation or packeg  name to be store
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
        BigDecimal tax=BigDecimal.ZERO;
        if(sevcat.getGstApplicable()){
            tax=BigDecimal.valueOf(sevcat.getGstPercent()).multiply(BigDecimal.valueOf(req.getActualAmount()).subtract(BigDecimal.valueOf(req.getDiscountedAmount()))).divide(BigDecimal.valueOf(100));
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
        return  billingDetailRepository.save(billingDetail);
    }


    @Override
    public List<PendingSampleResponse> getPendingSamples() {
        Long departmentId = authUtil.getCurrentDepartmentId();
        if (departmentId == null) {
            throw new IllegalArgumentException("Current department ID is null");
        }

        // Include both 'p' and 'y' payment statuses
        List<String> paymentStatuses = Arrays.asList("p", "y");
        List<String> orderStatusFilter = Arrays.asList("p", "n");
//        String orderStatusFilter = "n";

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(pendingDays);


        // Fetch only records matching both filters in DB
//        List<DgOrderHd> orderHdList = labHdRepository
//                .findPendingOrdersByDateRange(paymentStatuses, orderStatusFilter,startDate,endDate);
        List<DgOrderHd> orderHdList = labHdRepository
                .findPendingOrdersByDateRange(paymentStatuses, orderStatusFilter, startDate, endDate);

        List<PendingSampleResponse> responseList = new ArrayList<>();

        MasDepartment department = masDepartmentRepository.findById(departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid department ID: " + departmentId));

        for (DgOrderHd orderHd : orderHdList) {
            // Filter details by billingStatus (y) and orderStatus (n)
            List<DgOrderDt> orderDtList = labDtRepository
                    .findByOrderhdIdAndBillingStatusAndOrderStatus(orderHd, "y", "n");

            for (DgOrderDt orderDt : orderDtList) {
                Patient patient = orderHd.getPatientId();
                Visit visit = orderHd.getVisitId();
                DgMasInvestigation investigation = orderDt.getInvestigationId();

                PendingSampleResponse response = new PendingSampleResponse();
                response.setReqDate(orderHd.getOrderDate());
                response.setVistId(visit != null ? visit.getId() : null);
                String fullName = "";
                if (patient != null) {
                 fullName = Stream.of(
                                    patient.getPatientFn(),
                                    patient.getPatientMn(),
                                    patient.getPatientLn()
                            )
                            .filter(name -> name != null && !name.trim().isEmpty())
                            .collect(Collectors.joining(" "));
                }

                response.setPatientName(fullName);

//                response.setPatientName(
//                        patient != null
//                                ? (patient.getPatientFn() != null ? patient.getPatientFn() : "") +
//                                " " +
//                                (patient.getPatientLn() != null ? patient.getPatientLn() : "")
//                                : ""
//                );

                response.setRelation(
                        patient != null && patient.getPatientRelation() != null
                                ? patient.getPatientRelation().getRelationName()
                                : ""
                );

                response.setAge(
                        patient != null && patient.getPatientDob() != null
                                ? ageCalculator(patient.getPatientDob())
                                : ""
                );

                response.setGender(
                        patient != null && patient.getPatientGender() != null
                                ? patient.getPatientGender().getGenderName()
                                : ""
                );

                response.setMobile(patient != null ? patient.getPatientMobileNumber() : "");
                response.setDepartment(department.getDepartmentName());
                response.setDoctorName(visit != null ? visit.getDoctorName() : "");
                response.setOrderhdId(Long.valueOf(orderHd.getId()));
                response.setOrderNo(orderHd.getOrderNo());

                response.setOrderTime(
                        orderHd.getOrderTime() != null
                                ? getCurrentTimeFormatted(orderHd.getOrderTime())
                                : null
                );

                response.setInvestigation(investigation != null ? investigation.getInvestigationName() : "");
                response.setInvestigationId(investigation != null ? investigation.getInvestigationId() : null);

                response.setSample(
                        investigation != null && investigation.getSampleId() != null
                                ? investigation.getSampleId().getSampleDescription()
                                : ""
                );

                response.setSampleId(
                        investigation != null && investigation.getSampleId() != null
                                ? investigation.getSampleId().getId()
                                : null
                );

                response.setMainChargcodeId(
                        investigation != null && investigation.getMainChargeCodeId() != null
                                ? investigation.getMainChargeCodeId().getChargecodeId()
                                : null
                );

                response.setCollection(
                        investigation != null && investigation.getCollectionId() != null
                                ? investigation.getCollectionId().getCollectionName()
                                : ""
                );

                response.setCollectionId(
                        investigation != null && investigation.getCollectionId() != null
                                ? investigation.getCollectionId().getCollectionId()
                                : null
                );

                response.setSubChargeCode(
                        investigation != null && investigation.getSubChargeCodeId() != null
                                ? investigation.getSubChargeCodeId().getSubName()
                                : ""
                );

                response.setSubChargeCodeId(
                        investigation != null && investigation.getSubChargeCodeId() != null
                                ? investigation.getSubChargeCodeId().getSubId()
                                : null
                );

                responseList.add(response);
            }
        }
        return responseList;
    }




//    @Override
//    @Transactional
//    public ApiResponse<AppsetupResponse>  savesample(SampleCollectionRequest sampleReq) {
//        AppsetupResponse res = new AppsetupResponse();
//        try {
//            User currentUser = authUtil.getCurrentUser();
//            if (currentUser == null) {
//                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
//                        },
//                        "HospitalId not found", HttpStatus.UNAUTHORIZED.value());
//            }
//            Long departmentId = authUtil.getCurrentDepartmentId();
//            if (departmentId == null) {
//                return ResponseUtils.createFailureResponse(res, new TypeReference<>() {
//                        },
//                        "Current department ID not found", HttpStatus.BAD_REQUEST.value());
//            }
//
//            Map<Integer, List<SampleCollectionInvestigationReq>> groupedData =
//                    sampleReq.getSampleCollectionReq().stream()
//                            .collect(Collectors.groupingBy(SampleCollectionInvestigationReq::getSubChargeCodeId));
//
//            for (Map.Entry<Integer, List<SampleCollectionInvestigationReq>> entry : groupedData.entrySet()) {
//                Integer subChargeCodeId = entry.getKey();
//                List<SampleCollectionInvestigationReq> detailsList = entry.getValue();
//
//                // Save Header data
//                DgSampleCollectionHeader header = new DgSampleCollectionHeader();
//                //  header.setPatientType(sampleReq.getPatientType());
//                Optional<Visit> visit = visitRepository.findById((long) sampleReq.getVisitId());
//                if (visit.isEmpty()) {
//                    return ResponseUtils.createNotFoundResponse("visit not found", 404);
//                }
//                header.setVisitId(visit.get());
//                header.setPatient_id(visit.get().getPatient());
//                Optional<DgOrderHd> dgOrderHd = labHdRepository.findById(sampleReq.getOrderHdId());
//                if (dgOrderHd.isEmpty()) {
//                    return ResponseUtils.createNotFoundResponse("DgOrderHd not found", 404);
//                }
//                //header.setOrderHdId(dgOrderHd.get());
//                header.setHospitalId(currentUser.getHospital());
//                Optional<MasDepartment> depObj = masDepartmentRepository.findById(departmentId);
//                header.setDepartmentId(depObj.get());
//                String fName = currentUser.getFirstName() + " " + currentUser.getMiddleName() + " " + currentUser.getLastName();
//                header.setLastChgBy(fName);
////                header.setSample_order_status("Pending");
//                header.setLastChgDate(LocalDateTime.now());
//                header.setLastChgTime(LocalDateTime.now());
//                header.setHospitalId(currentUser.getHospital());
//                Optional<MasSubChargeCode> masSubChargeCode = masSubChargeCodeRepository.findById((long) subChargeCodeId);
//                header.setSubChargeCode(masSubChargeCode.isPresent()?masSubChargeCode.get():null);
////                header.setPriority("Routine");
//                header.setCollection_by(fName);
//                header.setCollection_time(LocalDateTime.now());
//                header.setResult_entry_status("p");
//                header.setSample_order_status("p");   // p = Pending
//                header.setPriority("r");              // r = Routine
////                header.setResult_entry_status("p");   // p = Pending
//
//                dgSampleCollectionHeaderRepository.save(header);
//
//
//                //order_by_department
//               // collection_center_id
//                //collection_center_modified_id
//                //inpatient_id,
//                // validation_date
//
//                //  Save Details data
//                for (SampleCollectionInvestigationReq detailReq : detailsList) {
//                    DgSampleCollectionDetails detail = new DgSampleCollectionDetails();
//                    detail.setRemarks(detailReq.getRemarks());
//
//                    Optional<DgMasInvestigation> masInvestigation = dgMasInvestigationRepository.findById((long) detailReq.getInvestigationId());
////
//                    detail.setInvestigationId(masInvestigation.isPresent()?masInvestigation.get():null);
//                    detail.setEmpanelledStatus(detailReq.getEmpanelledStatus());
//                    Optional<DgMasSample> dgMasSample = dgMasSampleRepository.findById((long) detailReq.getSampleId());
////
//                    detail.setSampleId(dgMasSample.isPresent()?dgMasSample.get():null);
//                    //Optional<DgMasCollection> dgMasCollection = dgMasCollectionRepository.findById((long) detailReq.getCollectionId());
////
//                    //detail.setCollectionId(dgMasCollection.isPresent() ? dgMasCollection.get().getCollectionId():null);
//                    Optional<MasMainChargeCode> masMainChargeCode = masMainChargeCodeRepository.findById((long) detailReq.getMainChargeCodeId());
////
//                   // detail.setMainChargecodeId(masMainChargeCode.isPresent() ? masMainChargeCode.get() : null);
//                    detail.setSampleCollectionHeaderId(header);
//                    detail.setRemarks(detailReq.getRemarks());
//                    detail.setOrderStatus("n");/// what??
//                    detail.setSampleCollDatetime(LocalDateTime.now());
//                   // collected_by
//                    //quantity
//                    //rejected_reason
//                    detail.setValidated("n");
//                    detail.setResult_status("p");
//                    detail.setEmpanelledStatus(detailReq.getEmpanelledStatus());
//                    dgSampleCollectionDetailsRepository.save(detail);
//                }
//            }
//            boolean oderStatusFully=true;
//            boolean oderStatusFullypartial=false;
//            List<DgOrderDt> orderDetails = labDtRepository.findByOrderhdIdId(sampleReq.getOrderHdId());
//
//            for (DgOrderDt d : orderDetails) {
//                for (SampleCollectionInvestigationReq req : sampleReq.getSampleCollectionReq()) {
//                    if (d.getInvestigationId() != null &&d.getInvestigationId().getInvestigationId()==(req.getInvestigationId())) {
//
//                        if ("y".equalsIgnoreCase(d.getBillingStatus())) {
//                            d.setOrderStatus("y");
//                            labDtRepository.save(d);
//                        }
//                    }
//                }
//                if (d.getOrderStatus().equalsIgnoreCase("n")) {
//                    oderStatusFully = false;
//                    oderStatusFullypartial = true;
//                    break;
//                }
//            }
//
//// 🔍 Fetch order header again by ID
//            Optional<DgOrderHd> dgOrderHdOpt = labHdRepository.findById(sampleReq.getOrderHdId());
//
//            if (dgOrderHdOpt.isPresent()) {
//                DgOrderHd hdorderObj = dgOrderHdOpt.get();
//
//                if (oderStatusFully) {
//                    hdorderObj.setOrderStatus("y");  // fully completed
//                } else {
//                    hdorderObj.setOrderStatus("n");  // partially completed
//                }
//                labHdRepository.save(hdorderObj);
//            }
//        }
//        catch (SDDException e) {
//            return ResponseUtils.createFailureResponse(res, new TypeReference<>() {}, e.getMessage(), e.getStatus());
//        }
//        catch (Exception e) {
//            e.printStackTrace(); // log exception for debugging
//            return ResponseUtils.createFailureResponse(res, new TypeReference<>() {}, "Internal Server Error", 500);
//        }
//        res.setMsg("Success");
//        return ResponseUtils.createSuccessResponse(res, new TypeReference<AppsetupResponse>() {});
//
//    }


@Override
@Transactional
public ApiResponse<AppsetupResponse> savesample(SampleCollectionRequest sampleReq) {
//    AppsetupResponse res = new AppsetupResponse();
//    try {
//        User currentUser = authUtil.getCurrentUser();
//        if (currentUser == null) {
//            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
//                    "HospitalId not found", HttpStatus.UNAUTHORIZED.value());
//        }
//        Long departmentId = authUtil.getCurrentDepartmentId();
//        if (departmentId == null) {
//            return ResponseUtils.createFailureResponse(res, new TypeReference<>() {},
//                    "Current department ID not found", HttpStatus.BAD_REQUEST.value());
//        }
//
//        Map<Integer, List<SampleCollectionInvestigationReq>> groupedData =
//                sampleReq.getSampleCollectionReq().stream()
//                        .collect(Collectors.groupingBy(SampleCollectionInvestigationReq::getSubChargeCodeId));
//
//        for (Map.Entry<Integer, List<SampleCollectionInvestigationReq>> entry : groupedData.entrySet()) {
//            Integer subChargeCodeId = entry.getKey();
//            List<SampleCollectionInvestigationReq> detailsList = entry.getValue();
//
//            // Save Header data
//            DgSampleCollectionHeader header = new DgSampleCollectionHeader();
//            Optional<Visit> visit = visitRepository.findById((long) sampleReq.getVisitId());
//            if (visit.isEmpty()) {
//                return ResponseUtils.createNotFoundResponse("visit not found", 404);
//            }
//            header.setVisitId(visit.get());
//            header.setPatientId(visit.get().getPatient());
//            Optional<DgOrderHd> dgOrderHd = labHdRepository.findById(sampleReq.getOrderHdId());
//            if (dgOrderHd.isEmpty()) {
//                return ResponseUtils.createNotFoundResponse("DgOrderHd not found", 404);
//            }
//
//            header.setHospitalId(currentUser.getHospital());
//            Optional<MasDepartment> depObj = masDepartmentRepository.findById(departmentId);
//            header.setDepartmentId(depObj.get());
//            String fName = currentUser.getFirstName() + " " + currentUser.getMiddleName() + " " + currentUser.getLastName();
//            header.setLastChgBy(fName);
//            header.setLastChgDate(LocalDateTime.now());
//            header.setLastChgTime(LocalDateTime.now());
//            header.setHospitalId(currentUser.getHospital());
//            Optional<MasSubChargeCode> masSubChargeCode = masSubChargeCodeRepository.findById((long) subChargeCodeId);
//            header.setSubChargeCode(masSubChargeCode.isPresent() ? masSubChargeCode.get() : null);
//            header.setCollection_by(fName);
//            header.setCollection_time(LocalDateTime.now());
//            header.setResult_entry_status("n");
//            header.setSampleOrderStatus("n");   // p = Pending
//            header.setPriority("r");
//            header.setValidated("n");// r = Routine
//
//            dgSampleCollectionHeaderRepository.save(header);
//
//            // Save Details data
//            for (SampleCollectionInvestigationReq detailReq : detailsList) {
//                DgSampleCollectionDetails detail = new DgSampleCollectionDetails();
//                detail.setRemarks(detailReq.getRemarks());
//
//                Optional<DgMasInvestigation> masInvestigation = dgMasInvestigationRepository.findById((long) detailReq.getInvestigationId());
//                detail.setInvestigationId(masInvestigation.isPresent() ? masInvestigation.get() : null);
//                detail.setEmpanelledStatus(detailReq.getEmpanelledStatus());
//                Optional<DgMasSample> dgMasSample = dgMasSampleRepository.findById((long) detailReq.getSampleId());
//                detail.setSampleId(dgMasSample.isPresent() ? dgMasSample.get() : null);
//
//                Optional<MasMainChargeCode> masMainChargeCode = masMainChargeCodeRepository.findById((long) detailReq.getMainChargeCodeId());
//                detail.setSampleCollectionHeader(header);
//                detail.setRemarks(detailReq.getRemarks());
//                detail.setOrderStatus("n");
//                detail.setSampleCollDatetime(LocalDateTime.now());
//                detail.setValidated("n");
//                detail.setResult_status("n");
//                detail.setEmpanelledStatus(detailReq.getEmpanelledStatus());
//                dgSampleCollectionDetailsRepository.save(detail);
//            }
//        }
//
//        // CORRECTED LOGIC: Check order status and update accordingly
//        List<DgOrderDt> orderDetails = labDtRepository.findByOrderhdIdId(sampleReq.getOrderHdId());
//
//        // Create a set of investigation IDs from the request for faster lookup
//        Set<Long> requestedInvestigationIds = sampleReq.getSampleCollectionReq().stream()
//                .map(req -> (long) req.getInvestigationId())
//                .collect(Collectors.toSet());
//
//        // Update order status for matching investigations
//        for (DgOrderDt orderDetail : orderDetails) {
//            if (orderDetail.getInvestigationId() != null &&
//                    requestedInvestigationIds.contains(orderDetail.getInvestigationId().getInvestigationId())) {
//
//                // If billing status is 'y', set order status to 'y'
//                if ("y".equalsIgnoreCase(orderDetail.getBillingStatus())) {
//                    orderDetail.setOrderStatus("y");
//                    labDtRepository.save(orderDetail);
//                }
//            }
//        }
//
//        // Refresh the order details after updates
//        orderDetails = labDtRepository.findByOrderhdIdId(sampleReq.getOrderHdId());
//
//        // Check if all order details are completed
//        boolean allCompleted = true;
//        boolean anyCompleted = false;
//
//        for (DgOrderDt orderDetail : orderDetails) {
//            if ("y".equalsIgnoreCase(orderDetail.getOrderStatus())) {
//                anyCompleted = true;
//            } else {
//                allCompleted = false;
//            }
//        }
//
//        // Update order header status
//        Optional<DgOrderHd> dgOrderHdOpt = labHdRepository.findById(sampleReq.getOrderHdId());
//        if (dgOrderHdOpt.isPresent()) {
//            DgOrderHd hdorderObj = dgOrderHdOpt.get();
//
//            if (allCompleted) {
//                hdorderObj.setOrderStatus("y");  // fully completed
//            } else if (anyCompleted) {
//                hdorderObj.setOrderStatus("p");  // partially completed
//            } else {
//                hdorderObj.setOrderStatus("n");  // not started/pending
//            }
//            labHdRepository.save(hdorderObj);
//        }
//
//    } catch (SDDException e) {
//
//        return ResponseUtils.createFailureResponse(res, new TypeReference<>() {}, e.getMessage(), e.getStatus());
//    } catch (Exception e) {
//        e.printStackTrace(); // log exception for debugging
//        return ResponseUtils.createFailureResponse(res, new TypeReference<>() {}, "Internal Server Error", 500);
//    }
//
//    res.setMsg("Success");
//    return ResponseUtils.createSuccessResponse(res, new TypeReference<AppsetupResponse>() {});


    AppsetupResponse res = new AppsetupResponse();

    try {
        User currentUser = authUtil.getCurrentUser();
        if (currentUser == null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "HospitalId not found", HttpStatus.UNAUTHORIZED.value());
        }

        Long departmentId = authUtil.getCurrentDepartmentId();
        if (departmentId == null) {
            return ResponseUtils.createFailureResponse(res, new TypeReference<>() {},
                    "Current department ID not found", HttpStatus.BAD_REQUEST.value());
        }

        // GROUP BY SUBCHARGECODE ID
        Map<Integer, List<SampleCollectionInvestigationReq>> groupedData =
                sampleReq.getSampleCollectionReq().stream()
                        .collect(Collectors.groupingBy(SampleCollectionInvestigationReq::getSubChargeCodeId));

        for (Map.Entry<Integer, List<SampleCollectionInvestigationReq>> entry : groupedData.entrySet()) {

            Integer subChargeCodeId = entry.getKey();
            List<SampleCollectionInvestigationReq> detailsList = entry.getValue();

            // FETCH EXISTING HEADER ONLY BY visitId and subCharegeCode and validated status

            Optional<DgSampleCollectionHeader> existingHeaderOpt =
                    dgSampleCollectionHeaderRepository
                            .findByVisitIdAndSubChargeCodeAndValidateStatusN(
                                    (long) sampleReq.getVisitId(),
                                    Long.valueOf(subChargeCodeId)
                            );
            DgSampleCollectionHeader header;
            Optional<Visit> visit = visitRepository.findById((long) sampleReq.getVisitId());
            if (visit.isEmpty()) {
                return ResponseUtils.createNotFoundResponse("Visit not found", 404);
            }
            if (existingHeaderOpt.isPresent()) {
                // REUSE EXISTING HEADER (MERGE)
                header = existingHeaderOpt.get();
            } else {
                // CREATE NEW HEADER
                header = new DgSampleCollectionHeader();



                header.setVisitId(visit.get());
                header.setPatientId(visit.get().getPatient());
                header.setHospitalId(currentUser.getHospital());

                Optional<MasDepartment> depObj = masDepartmentRepository.findById(departmentId);
                header.setDepartmentId(depObj.get());

                String fName = currentUser.getFirstName() + " " +
                        currentUser.getMiddleName() + " " +
                        currentUser.getLastName();

                header.setLastChgBy(fName);
                header.setLastChgDate(LocalDateTime.now());
                header.setLastChgTime(LocalDateTime.now());

                Optional<MasSubChargeCode> masSubChargeCode =
                        masSubChargeCodeRepository.findById(subChargeCodeId.longValue());
                header.setSubChargeCode(masSubChargeCode.orElse(null));

                header.setCollection_by(fName);
                header.setCollection_time(LocalDateTime.now());
                header.setResult_entry_status("n");
                header.setSampleOrderStatus("n");
                header.setPriority("r");
                header.setValidated("n");

                dgSampleCollectionHeaderRepository.save(header);
            }

            // SAVE DETAILS
            for (SampleCollectionInvestigationReq detailReq : detailsList) {

                DgSampleCollectionDetails detail = new DgSampleCollectionDetails();
                detail.setSampleCollectionHeader(header);
                detail.setRemarks(detailReq.getRemarks());

                Optional<DgMasInvestigation> masInvestigation =
                        dgMasInvestigationRepository.findById((long) detailReq.getInvestigationId());
                detail.setInvestigationId(masInvestigation.orElse(null));

                detail.setEmpanelledStatus(detailReq.getEmpanelledStatus());

                LabTurnAroundTime labTurnAroundTime=new LabTurnAroundTime();
                labTurnAroundTime.setInvestigation(masInvestigation.get());
                labTurnAroundTime.setOrderHd(labHdRepository.findById(sampleReq.getOrderHdId()).get());
                labTurnAroundTime.setPatient(visit.get().getPatient());
                labTurnAroundTime.setSampleCollectionDateTime(LocalDateTime.now());
                labTurnAroundTime.setSampleCollectedBy(currentUser.getFirstName()+" "+currentUser.getMiddleName()+" "+currentUser.getLastName());
                labTurnAroundTimeRepository.save( labTurnAroundTime);
                Optional<DgMasSample> dgMasSample =
                        dgMasSampleRepository.findById((long) detailReq.getSampleId());
                detail.setSampleId(dgMasSample.orElse(null));

                detail.setOrderStatus("n");
                detail.setSampleCollDatetime(LocalDateTime.now());
                detail.setValidated("n");
                detail.setResult_status("n");

                dgSampleCollectionDetailsRepository.save(detail);
            }
        }

        // AFTER DETAILS → UPDATE ORDER STATUS
        List<DgOrderDt> orderDetails = labDtRepository.findByOrderhdIdId(sampleReq.getOrderHdId());

        Set<Long> requestedInvestigationIds = sampleReq.getSampleCollectionReq().stream()
                .map(req -> (long) req.getInvestigationId())
                .collect(Collectors.toSet());

        for (DgOrderDt orderDetail : orderDetails) {
            if (orderDetail.getInvestigationId() != null &&
                    requestedInvestigationIds.contains(orderDetail.getInvestigationId().getInvestigationId())) {

                if ("y".equalsIgnoreCase(orderDetail.getBillingStatus())) {
                    orderDetail.setOrderStatus("y");
                    labDtRepository.save(orderDetail);
                }
            }
        }

        // UPDATE HEADER ORDER STATUS
        orderDetails = labDtRepository.findByOrderhdIdId(sampleReq.getOrderHdId());

        boolean allCompleted = true;
        boolean anyCompleted = false;

        for (DgOrderDt orderDetail : orderDetails) {
            if ("y".equalsIgnoreCase(orderDetail.getOrderStatus())) {
                anyCompleted = true;
            } else {
                allCompleted = false;
            }
        }

        Optional<DgOrderHd> dgOrderHdOpt = labHdRepository.findById(sampleReq.getOrderHdId());
        if (dgOrderHdOpt.isPresent()) {

            DgOrderHd hdorderObj = dgOrderHdOpt.get();

            if (allCompleted) {
                hdorderObj.setOrderStatus("y");
            } else if (anyCompleted) {
                hdorderObj.setOrderStatus("p");
            } else {
                hdorderObj.setOrderStatus("n");
            }

            labHdRepository.save(hdorderObj);
        }

    } catch (Exception e) {
        e.printStackTrace();
        return ResponseUtils.createFailureResponse(res, new TypeReference<>() {}, "Internal Server Error", 500);
    }

    res.setMsg("Success");
    return ResponseUtils.createSuccessResponse(res, new TypeReference<AppsetupResponse>() {});
}

    @Transactional
    @Override
    public ApiResponse<AppsetupResponse> labRegForExistingOrder(LabBillingOnlyRequest labReq) {
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

        if (labReq.getOrderhdid() == null) {
            return ResponseUtils.createFailureResponse(res, new TypeReference<>() {},
                    "Orderhdid must not be null for billing flow", HttpStatus.BAD_REQUEST.value());
        }

        Patient patient = patientRepository.findById(labReq.getPatientId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid patient ID: " + labReq.getPatientId()));

        DgOrderHd existingOrderHd = labHdRepository.findById(labReq.getOrderhdid());
        if (existingOrderHd == null) {
            throw new IllegalArgumentException("Invalid orderhdid: " + labReq.getOrderhdid());
        }

        Visit visit = existingOrderHd.getVisitId();
        if (visit == null) {
            throw new IllegalArgumentException("No visit linked to this orderhdid: " + labReq.getOrderhdid());
        }

        try {
            // ✅ Calculate sum, discount, tax based on ALL items in request
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

            // ✅ Create BillingHeader
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

            // ✅ Get ALL existing order details for this order
            List<DgOrderDt> allOrderDetails = labDtRepository.findByOrderhdId(existingOrderHd);
            System.out.println("Found " + allOrderDetails.size() + " existing order details for orderhdid: " + existingOrderHd.getId());

            // ✅ Link ALL order details to billing header
            for (DgOrderDt orderDetail : allOrderDetails) {
                orderDetail.setBillingHd(billingHeader);
                labDtRepository.save(orderDetail);
                System.out.println("✓ Linked order detail ID: " + orderDetail.getId() + " to billing header");
            }

            // ✅ Create BillingDetail rows ONLY for items that exist in order details
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
                        System.err.println("❌ WARNING: No order detail found for investigation ID: " + inv.getId());
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
                        System.err.println("❌ WARNING: No order detail found for package ID: " + inv.getId());
                        continue;
                    }

                    System.out.println("✓ Creating billing detail for package: " + pkgObj.getPackName() + " (OrderDt ID: " + matchingOrderDt.getId() + ")");
                    BillingDetaiDataSavePackage(billingHeader, pkgObj, inv);
                }
            }

            System.out.println("✓ Successfully created billing for existing order. Billing ID: " + billingHeader.getId());
            System.out.println("✓ Linked " + allOrderDetails.size() + " order details to billing header");
            System.out.println("✓ Created billing details for " + labReq.getLabInvestigationReq().size() + " items");

            res.setMsg("Success");
            return ResponseUtils.createSuccessResponse(res, new TypeReference<AppsetupResponse>() {});

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtils.createFailureResponse(res, new TypeReference<>() {}, "Internal Server Error: " + e.getMessage(), 500);
        }
    }

}
