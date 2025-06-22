package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.response.ApiResponse;
import com.hims.response.OpdBillingPaymentResponse;
import com.hims.service.BillingService;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Optional;

@Service
@Transactional
public class BillingServiceImpl implements BillingService {

    @Autowired
    BillingHeaderRepository billingHeaderRepository;
    @Autowired
    MasServiceOpdRepository masServiceOpdRepository;
    @Autowired
    BillingDetailRepository billingDetailRepository;
    @Autowired
    BillingPaymentRepository billingPaymentRepository;
    @Autowired
    PaymentDetailRepository paymentDetailRepository;
    @Autowired
    private VisitRepository visitRepository;

    @Override
    public ApiResponse<OpdBillingPaymentResponse> saveBillingForOpd(Visit visit, MasServiceCategory serviceCategory, MasDiscount discount) {
        BillingHeader header=new BillingHeader();
        OpdBillingPaymentResponse response=new OpdBillingPaymentResponse();
        try{
            BigDecimal totalDiscount= BigDecimal.valueOf(0);
            header.setBillDate(OffsetDateTime.from(Instant.now()));
            header.setPatient(visit.getPatient());
            header.setPatientDisplayName(visit.getPatient().getPatientFn()+" "+visit.getPatient().getPatientMn()+" "+visit.getPatient().getPatientLn());
            header.setPatientAge(Integer.valueOf(visit.getPatient().getPatientAge()));
            header.setPatientGender(visit.getPatient().getPatientGender().getGenderName());
            header.setPatientAddress(visit.getPatient().getPatientAddress1()+" "+visit.getPatient().getPatientAddress2());
            header.setHospital(visit.getHospital());
            header.setHospitalName(visit.getHospital().getHospitalName());
            header.setHospitalAddress(visit.getHospital().getAddress());
            header.setHospitalMobileNo(visit.getHospital().getContactNumber());
            header.setHospitalGstin(visit.getHospital().getGstnNo());
            header.setReferredBy(visit.getIniDoctor().getFirstName()+" "+visit.getIniDoctor().getMiddleName()+" "+visit.getIniDoctor().getLastName());
            header.setGstnBillNo("");
            header.setBillDate(OffsetDateTime.from(Instant.now()));
            Optional<MasServiceOpd> serviceOpd=masServiceOpdRepository.findByHospitalIdAndDoctorUserIdAndDepartmentIdAndServiceCatId(visit.getHospital(), visit.getDoctor(), visit.getDepartment(), serviceCategory);
            if(serviceOpd.isPresent()) {
                if (discount != null) {
                    totalDiscount = serviceOpd.get().getBaseTariff().multiply(discount.getDisPercentage().divide(BigDecimal.valueOf(100)));
                    if(totalDiscount.compareTo(discount.getMaxDiscount()) > 0){
                        totalDiscount=discount.getMaxDiscount();
                    }
                    header.setDiscountAmount(totalDiscount);
                }
                BigDecimal total=serviceOpd.get().getBaseTariff().subtract(totalDiscount);
                header.setNetAmount(total);
                header.setTotalAmount(total);
                header.setTaxTotal(BigDecimal.valueOf(0));
                header.setTotalPaid(BigDecimal.valueOf(0));
            }
            header.setPaymentStatus("n");
            header.setCreatedBy("");
            header.setUpdatedDt(Instant.now());
            header.setCreatedDt(Instant.now());
            header.setInvoiceNo("");
            header.setDiscount(discount);
            header.setVisit(visit);
            header.setServiceCategory(serviceCategory);
            BillingHeader savedHeader=billingHeaderRepository.save(header);
            response.setHeader(savedHeader);
            if(savedHeader!=null){
                BillingDetail detail=new BillingDetail();
                detail.setBillingHd(savedHeader);
                detail.setServiceCategory(serviceCategory);
                detail.setServiceId(0L);
                detail.setItemName("");

//                Optional<MasServiceOpd> serviceOpd=masServiceOpdRepository.findByHospitalIdAndDoctorUserIdAndDepartmentIdAndServiceCatId(visit.getHospital(), visit.getDoctor(), visit.getDepartment(), serviceCategory);
                if(serviceOpd.isPresent()) {
                    detail.setOpdService(serviceOpd.get());
                    detail.setChargeCost(serviceOpd.get().getBaseTariff());
                    detail.setBasePrice(serviceOpd.get().getBaseTariff());
                    detail.setTariff(serviceOpd.get().getBaseTariff());


                    if (discount != null) {
                        totalDiscount = serviceOpd.get().getBaseTariff().multiply(discount.getDisPercentage().divide(BigDecimal.valueOf(100)));
                        if(totalDiscount.compareTo(discount.getMaxDiscount()) > 0){
                            totalDiscount=discount.getMaxDiscount();
                        }
                        detail.setDiscount(totalDiscount);
                    }
                    BigDecimal total=serviceOpd.get().getBaseTariff().subtract(totalDiscount);
                    detail.setAmountAfterDiscount(total);
                    detail.setTaxPercent(BigDecimal.valueOf(0));
                    detail.setTaxAmount(BigDecimal.valueOf(0));
                    detail.setNetAmount(total);

                    detail.setCreatedAt(Instant.now());
                    detail.setTotal(total);
                }
                detail.setInvestigation(null);
                detail.setCreatedDt(OffsetDateTime.from(Instant.now()));
                detail.setUpdatedDt(OffsetDateTime.from(Instant.now()));
//                BillingDetail savedDetail=billingDetailRepository.save(detail);
//                PaymentDetail payment=new PaymentDetail();
                boolean paymentFlag=false;
//                try{
//                    payment.setBillingHd(savedHeader);
//                    payment.setPaymentDate(Instant.now());
//                    payment.setAmount(savedDetail.getTotal());
//                    payment.setPaymentMode("Cash");
//                    payment.setPaymentReferenceNo("NA");
//                    payment.setCreatedAt(Instant.now());
//                    payment.setCreatedBy("");
//                    payment.setPaymentStatus("y");
//                    payment.setUpdatedAt(Instant.now());
//                    PaymentDetail savedPayment=paymentDetailRepository.save(payment);
//                    if(savedPayment!=null){
//                        paymentFlag=true;
//                    }
//
//                }catch (Exception e){
//                    paymentFlag=false;
//                }

            response.setPaymentFlag(paymentFlag);
            }
            else{
                return  ResponseUtils.createFailureResponse(response,new TypeReference<>() {},"Error processing billing data",500);
            }
        }catch (Exception ex){
            return ResponseUtils.createFailureResponse(response, new TypeReference<>() {},"Error processing billing data",500);
        }
        return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});
    }

    public boolean setPaymentDetail(BillingHeader savedHeader){
        PaymentDetail payment=new PaymentDetail();
        boolean paymentFlag=false;
        try{
            payment.setBillingHd(savedHeader);
            payment.setPaymentDate(Instant.now());
            payment.setAmount(savedHeader.getNetAmount());
            payment.setPaymentMode("Cash");
            payment.setPaymentReferenceNo("NA");
            payment.setCreatedAt(Instant.now());
            payment.setCreatedBy("");
            payment.setPaymentStatus("y");
            payment.setUpdatedAt(Instant.now());
            PaymentDetail savedPayment=paymentDetailRepository.save(payment);
            if(savedPayment!=null){
                paymentFlag=true;
            }

        }catch (Exception e){
            paymentFlag=false;
        }
        return paymentFlag;
    }
}
