package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.response.*;
import com.hims.service.BillingService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.hims.helperUtil.ConverterUtils.ageCalculator;

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
    @Autowired
    AuthUtil authUtil;

    @Override
    @Transactional
    public ApiResponse<OpdBillingPaymentResponse> saveBillingForOpd(Visit visit, MasServiceCategory serviceCategory, MasDiscount discount) {
        BillingHeader header = new BillingHeader();
        OpdBillingPaymentResponse response = new OpdBillingPaymentResponse();
        User currentUser = authUtil.getCurrentUser();
        try {
            BigDecimal totalDiscount = BigDecimal.valueOf(0);
            header.setBillDate(OffsetDateTime.now());
            header.setPatient(visit.getPatient());
            header.setPatientDisplayName(visit.getPatient().getPatientFn() + " " + visit.getPatient().getPatientMn() + " " + visit.getPatient().getPatientLn());
            header.setPatientAge(visit.getPatient().getPatientAge());
            header.setPatientGender(visit.getPatient().getPatientGender().getGenderName());
            header.setPatientAddress(visit.getPatient().getPatientAddress1() + " " + visit.getPatient().getPatientAddress2());
            header.setHospital(visit.getHospital());
            header.setHospitalName(visit.getHospital().getHospitalName());
            header.setHospitalAddress(visit.getHospital().getAddress());
            header.setHospitalMobileNo(visit.getHospital().getContactNumber());
            header.setHospitalGstin(visit.getHospital().getGstnNo());
            header.setReferredBy(visit.getIniDoctor().getFirstName() + " " + visit.getIniDoctor().getMiddleName() + " " + visit.getIniDoctor().getLastName());
            header.setGstnBillNo("");
            header.setBillDate(OffsetDateTime.now());
            Optional<MasServiceOpd> serviceOpd = masServiceOpdRepository.findByHospitalIdAndDoctorUserIdAndDepartmentIdAndServiceCatId(visit.getHospital(), visit.getDoctor(), visit.getDepartment(), serviceCategory);
            if (serviceOpd.isPresent()) {
                if (discount != null) {
                    if (discount.getDisPercentage() != null && discount.getMaxDiscount() != null) {
                        totalDiscount = serviceOpd.get().getBaseTariff().multiply(discount.getDisPercentage().divide(BigDecimal.valueOf(100)));
                        if (totalDiscount.compareTo(discount.getMaxDiscount()) > 0) {
                            totalDiscount = discount.getMaxDiscount();
                        }
                    }
                }
                BigDecimal total = serviceOpd.get().getBaseTariff().subtract(totalDiscount);
                header.setNetAmount(total);
                header.setTotalAmount(total);
                header.setTaxTotal(BigDecimal.valueOf(0));
                header.setTotalPaid(BigDecimal.valueOf(0));
            }
            header.setDiscountAmount(totalDiscount);
            header.setPaymentStatus("n");
            header.setCreatedBy(currentUser.getFirstName());
            header.setUpdatedDt(Instant.now());
            header.setCreatedDt(Instant.now());
            header.setInvoiceNo("");
            header.setBillNo("");
            header.setUpdatedAt(OffsetDateTime.now());
            header.setBillingDate(Instant.now());
            header.setDiscount(discount);
            header.setVisit(visit);
            header.setServiceCategory(serviceCategory);
            header.setBillingHdId(0);
            BillingHeader savedHeader = billingHeaderRepository.save(header);
            response.setHeader(savedHeader);
            if (savedHeader != null) {
                BillingDetail detail = new BillingDetail();
                detail.setBillingHd(savedHeader);
                detail.setServiceCategory(serviceCategory);
                detail.setServiceId(0L);
                detail.setItemName("");
                detail.setPaymentStatus("n");

//                Optional<MasServiceOpd> serviceOpd=masServiceOpdRepository.findByHospitalIdAndDoctorUserIdAndDepartmentIdAndServiceCatId(visit.getHospital(), visit.getDoctor(), visit.getDepartment(), serviceCategory);
                if (serviceOpd.isPresent()) {
                    detail.setOpdService(serviceOpd.get());
                    detail.setChargeCost(serviceOpd.get().getBaseTariff());
                    detail.setBasePrice(serviceOpd.get().getBaseTariff());
                    detail.setTariff(serviceOpd.get().getBaseTariff());


                    if (discount != null) {
                        if (discount.getDisPercentage() != null && discount.getMaxDiscount() != null) {
                            totalDiscount = serviceOpd.get().getBaseTariff().multiply(discount.getDisPercentage().divide(BigDecimal.valueOf(100)));
                            if (totalDiscount.compareTo(discount.getMaxDiscount()) > 0) {
                                totalDiscount = discount.getMaxDiscount();
                            }
                        }
                    }
                    detail.setDiscount(totalDiscount);
                    BigDecimal total = serviceOpd.get().getBaseTariff().subtract(totalDiscount);
                    detail.setAmountAfterDiscount(total);
                    detail.setTaxPercent(BigDecimal.valueOf(0));
                    detail.setTaxAmount(BigDecimal.valueOf(0));
                    detail.setNetAmount(total);
                    detail.setCreatedAt(Instant.now());
                    detail.setTotal(total);
                }
                detail.setInvestigation(null);
                detail.setCreatedDt(OffsetDateTime.now());
                detail.setUpdatedDt(OffsetDateTime.now());
                detail.setBillHd(savedHeader);
                BillingDetail savedDetail = billingDetailRepository.save(detail);
//                PaymentDetail payment=new PaymentDetail();
                boolean paymentFlag = false;
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
            } else {
                return ResponseUtils.createFailureResponse(response, new TypeReference<>() {
                }, "Error processing billing data", 500);
            }
        } catch (Exception ex) {
            return ResponseUtils.createFailureResponse(response, new TypeReference<>() {
            }, "Error processing billing data", 500);
        }
        return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {
        });
    }

    public boolean setPaymentDetail(BillingHeader savedHeader) {
        PaymentDetail payment = new PaymentDetail();
        boolean paymentFlag = false;
        try {
            payment.setBillingHd(savedHeader);
            payment.setPaymentDate(Instant.now());
            payment.setAmount(savedHeader.getNetAmount());
            payment.setPaymentMode("Cash");
            payment.setPaymentReferenceNo("NA");
            payment.setCreatedAt(Instant.now());
            payment.setCreatedBy(authUtil.getCurrentUser().getFirstName());
            payment.setPaymentStatus("y");
            payment.setUpdatedAt(Instant.now());
            PaymentDetail savedPayment = paymentDetailRepository.save(payment);
            if (savedPayment != null) {
                paymentFlag = true;
            }

        } catch (Exception e) {
            paymentFlag = false;
        }
        return paymentFlag;
    }

    @Override
    public ApiResponse<List<PendingBillingResponse>> getPendingBilling() {
        List<BillingHeader> headers = billingHeaderRepository.findByPaymentStatusIn(List.of("n", "p"));

        List<PendingBillingResponse> responseList = headers.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(
                responseList,
                new TypeReference<List<PendingBillingResponse>>() {});
    }

    private PendingBillingResponse mapToResponse(BillingHeader header) {
        PendingBillingResponse response = new PendingBillingResponse();
        response.setBillinghdid(header.getId());

        // ✅ Name from BillingHeader
        response.setPatientName(safe(header.getPatientDisplayName()));
        response.setAddress(header.getPatientAddress());


        if (header.getVisit() != null && header.getVisit().getPatient() != null) {
            response.setPatientid(header.getVisit().getPatient().getUhidNo());
        } else {
            response.setPatientid(null);
        }

        // ✅ Mobile from Visit → Patient
        if (header.getVisit() != null && header.getVisit().getPatient() != null) {
            response.setMobileNo(safe(header.getVisit().getPatient().getPatientMobileNumber()));
        } else {
            response.setMobileNo("");
        }

        // ✅ Age from Patient DOB in Visit
        if (header.getVisit() != null && header.getVisit().getPatient() != null &&
                header.getVisit().getPatient().getPatientDob() != null) {
            String ageStr = ageCalculator(header.getVisit().getPatient().getPatientDob());
            response.setAge(ageStr);
        } else {
            response.setAge("");
        }

        // ✅ Gender from BillingHeader
        response.setSex(safe(header.getPatientGender()));

        // ✅ Relation from Visit → Patient → patientRelation
        if (header.getVisit() != null && header.getVisit().getPatient() != null &&
                header.getVisit().getPatient().getPatientRelation() != null) {
            response.setRelation(safe(header.getVisit().getPatient().getPatientRelation().getRelationName()));
        } else {
            response.setRelation("");
        }

        // ✅ Consulted doctor
        response.setConsultedDoctor(safe(header.getReferredBy()));

        // ✅ Department from Visit → Department
        if (header.getVisit() != null && header.getVisit().getDepartment() != null) {
            response.setDepartment(safe(header.getVisit().getDepartment().getDepartmentName()));
        } else {
            response.setDepartment("");
        }

        // ✅ Billing type from ServiceCategory
        if (header.getServiceCategory() != null) {
            response.setBillingType(safe(header.getServiceCategory().getServiceCatName()));
        } else {
            response.setBillingType("");
        }

        // ✅ Amount
        response.setAmount(
                header.getNetAmount() != null
                        ? header.getNetAmount().subtract(
                        header.getTotalPaid() != null ? header.getTotalPaid() : BigDecimal.ZERO
                )
                        : BigDecimal.ZERO
        );

        // ✅ Billing status
        response.setBillingStatus(safe(header.getPaymentStatus()));


        List<BillingDetail> detailsList = billingDetailRepository.findByBillHdIdAndPaymentStatusIn(
                header.getId(), List.of("n", "p")
        );

        List<BillingDetailResponse> details = detailsList.stream()
                .map(this::mapToDetailResponse)
                .collect(Collectors.toList());
        response.setDetails(details);

        return response;
    }

    private BillingDetailResponse mapToDetailResponse(BillingDetail detail) {
        BillingDetailResponse d = new BillingDetailResponse();
        d.setId(detail.getId());
        d.setItemName(safe(detail.getItemName()));
        d.setQuantity(detail.getQuantity());
        d.setBasePrice(detail.getBasePrice());
        d.setTariff(detail.getTariff());
        d.setDiscount(detail.getDiscount());
        d.setAmountAfterDiscount(detail.getAmountAfterDiscount());
        d.setTaxPercent(detail.getTaxPercent());
        d.setTaxAmount(detail.getTaxAmount());
        d.setNetAmount(detail.getNetAmount());
        d.setPaymentStatus(safe(detail.getPaymentStatus()));

        // ✅ Include Investigation
        if (detail.getInvestigation() != null) {
            d.setInvestigationId(detail.getInvestigation().getInvestigationId());
            d.setInvestigationName(detail.getInvestigation().getInvestigationName());
        }

        // ✅ Include Package
        if (detail.getPackageField() != null) {
            d.setPackageId(detail.getPackageField().getPackId());
            d.setPackageName(detail.getPackageField().getPackName());
        }

        return d;
    }

    private String safe(String value) {
        return value != null ? value : "";
    }



    @Override
    public List<PendingBillingSearchResponse> searchPendingBilling(String patientName, String uhidNo) {
        List<BillingHeader> headers = billingHeaderRepository.searchPendingBilling(
                patientName,
                uhidNo,
                List.of("y","n","p")
        );
        return headers.stream().map(this::mapToSearchResponse).collect(Collectors.toList());
    }


    private PendingBillingSearchResponse mapToSearchResponse(BillingHeader header) {
        PendingBillingSearchResponse response = new PendingBillingSearchResponse();
        response.setBillinghdid(header.getId());
        response.setPatientName(safe(header.getPatientDisplayName()));
        response.setUhidNo(header.getVisit().getPatient().getUhidNo());
        response.setAddress(header.getPatientAddress());

        if (header.getVisit() != null && header.getVisit().getPatient() != null) {
            response.setPatientid(header.getVisit().getPatient().getId()); // Usually UHID, not DB ID
            response.setMobileNo(safe(header.getVisit().getPatient().getPatientMobileNumber()));
            if (header.getVisit().getPatient().getPatientDob() != null) {
                response.setAge(ageCalculator(header.getVisit().getPatient().getPatientDob()));
            }
            if (header.getVisit().getPatient().getPatientRelation() != null) {
                response.setRelation(safe(header.getVisit().getPatient().getPatientRelation().getRelationName()));
            }
        }

        response.setSex(safe(header.getPatientGender()));
        response.setConsultedDoctor(safe(header.getReferredBy()));
        if (header.getVisit() != null && header.getVisit().getDepartment() != null) {
            response.setDepartment(safe(header.getVisit().getDepartment().getDepartmentName()));
        }
        if (header.getServiceCategory() != null) {
            response.setBillingType(safe(header.getServiceCategory().getServiceCatName()));
        }

        response.setAmount(
                header.getNetAmount() != null ?
                        header.getNetAmount().subtract(header.getTotalPaid() != null ? header.getTotalPaid() : BigDecimal.ZERO) :
                        BigDecimal.ZERO
        );
        response.setBillingStatus(safe(header.getPaymentStatus()));

        List<BillingDetail> detailsList = billingDetailRepository.findByBillHd_Id(header.getId());

        List<BillingDetailResponse> details = detailsList.stream().map(this::mapToDetailResponse).collect(Collectors.toList());
        response.setDetails(details);

        return response;
    }







}

