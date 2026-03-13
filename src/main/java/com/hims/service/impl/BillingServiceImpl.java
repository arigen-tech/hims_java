package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.exception.BillingException;
import com.hims.exception.GlobalExceptionHandler;
import com.hims.exception.SDDException;
import com.hims.projection.*;
import com.hims.request.InvestigationandPackegBillStatus;
import com.hims.request.PaymentUpdateRequest;
import com.hims.response.*;
import com.hims.service.BillingService;
import com.hims.utils.AuthUtil;
import com.hims.utils.RandomNumGenerator;
import com.hims.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.implementation.bytecode.Throw;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.hims.helperUtil.ConverterUtils.ageCalculator;

@Service
@Transactional
@Slf4j
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
    @Autowired
    PatientRepository patientRepository;
    @Autowired
    private RandomNumGenerator randomNumGenerator;
    @Autowired
    private MasHospitalRepository masHospitalRepository;

    @Autowired
    private MasServiceCategoryRepository masServiceCategoryRepository;

    @Autowired

    private LabHdRepository labHdRepository;

    @Autowired
    private LabDtRepository labDtRepository;
    @Autowired
    private MasInvestigationPriceDetailsRepository masInvestigationPriceDetailsRepository;

    @Autowired
    BillingPolicyRepository billingPolicyRepository;

    @Autowired
    RadOrderDtRepository radOrderDtRepository;

    @Autowired
    RadOrderHdRepository radOrderHdRepository;

    @Value("${serviceCategoryRegistration}")
    private String serviceCategoryRegistration;

    @Value("${OPDPaid}")
    private Long opdPaid;

    @Value("${OPDFollowUp}")
    private Long opdFollowUp;

    @Value("${serviceCategoryOPD}")
    private String opdServiceCategoryCode;

    @Value("${serviceCategoryLab}")
    private String labServiceCategoryCode;

    @Value("${serviceCategoryRegistration}")
    private String regServiceCategoryCode;

    @Value("${serviceCategoryRad}")
    private String radioServiceCategoryCode;


    @Override
    @Transactional
    public ApiResponse<OpdBillingPaymentResponse> saveBillingForOpd(Visit visit, MasServiceCategory serviceCategory, MasDiscount discount) {
        BillingHeader header = new BillingHeader();
        String orderNum = generateInvoiceNumber();
        OpdBillingPaymentResponse response = new OpdBillingPaymentResponse();
        User currentUser = authUtil.getCurrentUser();
        BigDecimal tax = BigDecimal.ZERO;
        BigDecimal registrationCost = BigDecimal.ZERO;

        Long policyIdToApply = opdPaid;
        BillingPolicyMaster billingPolicyMaster;
        String visitTypeToApply = visit.getVisitType();
        Patient patient = visit.getPatient();

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
            Instant currentDate = Instant.now();


            Optional<Visit> lastVisitOpt = visitRepository.findPreviousVisit(
                    patient.getId(),
                    visit.getDoctor().getUserId(),
                    visit.getDepartment().getId(),
                    visit.getHospital().getId(),
                    visit.getId()
            );

            //finding the policy
            BillingPolicyMaster policy = FindCorrectBillingPolicy(lastVisitOpt, visit);
            header.setBillingPolicy(policy);

            Optional<MasServiceOpd> serviceOpd = masServiceOpdRepository.findByHospitalIdAndDoctorUserIdAndDepartmentIdAndServiceCatIdAndCurrentDate(visit.getHospital(), visit.getDoctor(), visit.getDepartment(), serviceCategory, currentDate);
            if (serviceOpd.isPresent()) {
                if (discount != null) {
                    if (discount.getDisPercentage() != null && discount.getMaxDiscount() != null) {
                        totalDiscount = serviceOpd.get().getBaseTariff().multiply(discount.getDisPercentage().divide(BigDecimal.valueOf(100)));
                        if (totalDiscount.compareTo(discount.getMaxDiscount()) > 0) {
                            totalDiscount = discount.getMaxDiscount();
                        }
                    }
                }
                //policy Discount
                if (header.getBillingPolicy() != null) {
                    BigDecimal policyDiscount = header.getBillingPolicy().getDiscountPercentage();
                    if (policyDiscount != null) {
                        totalDiscount = serviceOpd.get().getBaseTariff().multiply(policyDiscount.divide(BigDecimal.valueOf(100)));
                    }
                }

                if (visit.getVisitType().equalsIgnoreCase("N")) {
                    MasServiceCategory masServiceCategory = masServiceCategoryRepository.findByServiceCateCode(serviceCategoryRegistration);
                    if (masServiceCategory != null) {
                        registrationCost = masServiceCategory.getRegistrationCost();
                    }
                }

                BigDecimal baseprice = serviceOpd.get().getBaseTariff();
                BigDecimal amountAfterDiscount = baseprice.subtract(totalDiscount);
                tax = tax.add(BigDecimal.valueOf(serviceCategory.getGstPercent()).multiply(amountAfterDiscount).divide(BigDecimal.valueOf(100)));
                BigDecimal total = serviceOpd.get().getBaseTariff().subtract(totalDiscount).add(tax);

                header.setTotalAmount(total);
                header.setNetAmount(total.add(registrationCost));
                header.setTaxTotal(tax);
                header.setTotalPaid(BigDecimal.valueOf(0));
            } else {
                throw new BillingException("MasServiceOPD or Tariff is not defined yet");
            }
            header.setDiscountAmount(totalDiscount);
            if (visit.getHospital().getAppCostApplicable().equalsIgnoreCase("n")) {
                header.setPaymentStatus("y");
            } else {
                header.setPaymentStatus("n");
            }
            header.setPaymentStatus("n");
            header.setCreatedBy(currentUser.getFirstName());
            header.setUpdatedDt(Instant.now());
            header.setCreatedDt(Instant.now());
            header.setInvoiceNo("");
            header.setBillNo(orderNum);
            header.setUpdatedAt(OffsetDateTime.now());
            header.setBillingDate(Instant.now());
            header.setDiscount(discount);
            header.setVisit(visit);
            header.setServiceCategory(serviceCategory);
            header.setBillingHdId(0);

            BillingHeader savedHeader = billingHeaderRepository.save(header);
            response.setHeader(savedHeader);
            //only for new patient we add new details with same header
            if (visit.getVisitType().equalsIgnoreCase("N")) {
                MasServiceCategory masServiceCategory = masServiceCategoryRepository.findByServiceCateCode(serviceCategoryRegistration);
                if (savedHeader != null) {
                    BillingDetail detail = new BillingDetail();
                    detail.setBillingHd(savedHeader);
                    detail.setServiceCategory(masServiceCategory);
                    detail.setServiceId(0L);
                    detail.setItemName("");
                    if (visit.getHospital().getAppCostApplicable().equalsIgnoreCase("n")) {
                        detail.setPaymentStatus("y");
                    } else {
                        detail.setPaymentStatus("n");
                    }
                    detail.setInvestigation(null);
                    detail.setChargeCost(masServiceCategory.getRegistrationCost());
                    detail.setBasePrice(masServiceCategory.getRegistrationCost());
                    detail.setDiscount(BigDecimal.ZERO);
                    BigDecimal total = masServiceCategory.getRegistrationCost();
                    detail.setAmountAfterDiscount(total);
                    detail.setTaxPercent(BigDecimal.ZERO);
                    detail.setTaxAmount(BigDecimal.ZERO);
                    detail.setNetAmount(total);
                    detail.setCreatedAt(Instant.now());
                    detail.setRegistrationCost(BigDecimal.ZERO);
                    detail.setCreatedDt(OffsetDateTime.now());
                    detail.setUpdatedDt(OffsetDateTime.now());
                    detail.setBillHd(savedHeader);
                    BillingDetail savedDetail = billingDetailRepository.save(detail);
                    boolean paymentFlag = false;
                    response.setPaymentFlag(paymentFlag);
                }
            }

            if (savedHeader != null) {
                BillingDetail detail = new BillingDetail();
                detail.setBillingHd(savedHeader);
                detail.setServiceCategory(serviceCategory);
                detail.setServiceId(0L);
                detail.setItemName("");
                if (visit.getHospital().getAppCostApplicable().equalsIgnoreCase("n")) {
                    detail.setPaymentStatus("y");
                } else {
                    detail.setPaymentStatus("n");
                }

                if (serviceOpd.isPresent()) {
                    detail.setOpdService(serviceOpd.get());

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
                    BigDecimal discountedAmount = serviceOpd.get().getBaseTariff().subtract(totalDiscount);
                    BigDecimal totalAmount = discountedAmount.add(tax);
                    detail.setChargeCost(serviceOpd.get().getBaseTariff().add(tax));
                    detail.setAmountAfterDiscount(discountedAmount);
                    detail.setTaxPercent(BigDecimal.valueOf(serviceCategory.getGstPercent()));
                    detail.setTaxAmount(tax);
                    detail.setNetAmount(totalAmount);
                    detail.setCreatedAt(Instant.now());
                }
                detail.setInvestigation(null);
                detail.setCreatedDt(OffsetDateTime.now());
                detail.setUpdatedDt(OffsetDateTime.now());
                detail.setBillHd(savedHeader);
                BillingDetail savedDetail = billingDetailRepository.save(detail);

                boolean paymentFlag = false;
                response.setPaymentFlag(paymentFlag);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Billing failed: " + ex.getMessage(), ex);
        }
        return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {
        });
    }

    public String generateInvoiceNumber() {
        return randomNumGenerator.generateOrderNumber("BILL", true, true);
    }

    private BillingPolicyMaster FindCorrectBillingPolicy(Optional<Visit> lastVisitOpt, Visit currentVisit) {
        Optional<BillingPolicyMaster> pdPolicy = billingPolicyRepository.findByBillingPolicyId(opdPaid);
        Optional<BillingPolicyMaster> flwUpPolicy = billingPolicyRepository.findByBillingPolicyId(opdFollowUp);
        BillingPolicyMaster paidPolicy = pdPolicy.get();
        BillingPolicyMaster followUpPolicy = flwUpPolicy.get();
        if (lastVisitOpt.isEmpty() ||
                "C".equalsIgnoreCase(lastVisitOpt.get().getVisitStatus()) ||
                "X".equalsIgnoreCase(lastVisitOpt.get().getVisitStatus())) {
            return paidPolicy;
        }
        Visit lastVisit = lastVisitOpt.get();
        BillingPolicyMaster lastPolicy = Optional.ofNullable(lastVisit.getBillingHd())
                .map(BillingHeader::getBillingPolicy)
                .orElse(null);

        if (lastPolicy != null && followUpPolicy != null &&
                followUpPolicy.getBillingPolicyId().equals(lastPolicy.getBillingPolicyId())) {
            return paidPolicy;
        }
        if (followUpPolicy != null && followUpPolicy.getFollowupDaysAllowed() > 0) {
            LocalDate lastVisitDate = lastVisit.getVisitDate().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate currentVisitDate = currentVisit.getVisitDate().atZone(ZoneId.systemDefault()).toLocalDate();
            long daysBetween = ChronoUnit.DAYS.between(lastVisitDate, currentVisitDate);

            if (daysBetween > 0 && daysBetween <= followUpPolicy.getFollowupDaysAllowed()) {
                return followUpPolicy;
            }
        }

        return paidPolicy;
    }



    @Override
    public ApiResponse<?> getPendingBillingsByCategory(
            String serviceCategoryCode,
            String patientName,
            String mobileNo,
            String registrationNo,
            int page,
            int size) {
        try {


            MasServiceCategory serviceCategory =
                    masServiceCategoryRepository.findByServiceCateCode(serviceCategoryCode);

            Pageable pageable = PageRequest.of(page, size);

            if (serviceCategory == null) {
                return ResponseUtils.createNotFoundResponse("Service category not found", 404);
            }
            String notPaid = AppConstants.PAYMENT_NOT_PAID;
            String partialPaid = AppConstants.PAYMENT_PARTIAL_PENDING;

            Long categoryId = serviceCategory.getId();

            // OPD
            if (opdServiceCategoryCode.equalsIgnoreCase(serviceCategoryCode)) {
                List<OpdBillingProjection> billingHeaders =
                        billingHeaderRepository.findPendingBillingByServiceCategories(
                                categoryId,
                                patientName,
                                mobileNo,
                                registrationNo
                        );
                Map<String, List<OpdBillingProjection>> grouped =
                        billingHeaders.stream()
                                .collect(Collectors.groupingBy(
                                        p -> p.getPatientName() + "_" +
                                                p.getConsultingDoctorName() + "_" +
                                                p.getDepartmentName(),
                                        LinkedHashMap::new,
                                        Collectors.toList()
                                ));

                List<OpdPendingBillingResponse> response = grouped.values().stream()
                        .map(list -> {

                            OpdPendingBillingResponse r = new OpdPendingBillingResponse();

                            r.setVisitIds(
                                    list.stream()
                                            .map(OpdBillingProjection::getVisitId)
                                            .collect(Collectors.toList())
                            );

                            r.setBillingHdId(null);

                            OpdBillingProjection first = list.get(0);
                            r.setPatientId(first.getPatientId());
                            r.setRegistrationNo(first.getRegistrationNo());
                            r.setPatientName(first.getPatientName());
                            r.setAge(ageCalculator(first.getAge()));
                            r.setGender(first.getGender());
                            r.setRelation(first.getRelation());
                            r.setBillingType(first.getBillingType());
                            r.setConsultingDoctorName(first.getConsultingDoctorName());
                            r.setDepartmentName(first.getDepartmentName());
                            r.setMobileNo(first.getMobileNo());
                            r.setAppointmentDate(first.getAppointmentDate());

                            r.setNetAmount(
                                    list.stream()
                                            .mapToDouble(OpdBillingProjection::getNetAmount)
                                            .sum()
                            );
                            return r;
                        })
                        .toList();


                int start = (int) pageable.getOffset();
                int end = Math.min(start + pageable.getPageSize(), response.size());

                List<OpdPendingBillingResponse> pagedResponse =
                        start >= response.size() ? Collections.emptyList() : response.subList(start, end);

                Page<OpdPendingBillingResponse> pageResult =
                        new PageImpl<>(pagedResponse, pageable, response.size());

                return ResponseUtils.createSuccessResponse(
                        pageResult,
                        new TypeReference<Page<OpdPendingBillingResponse>>() {
                        }
                );
            } else if (labServiceCategoryCode.equalsIgnoreCase(serviceCategoryCode)) {

                Page<LabBillingProjection> projections =
                        billingHeaderRepository.findPendingBillingByCategoryId(
                                categoryId,
                                patientName,
                                mobileNo,
                                registrationNo,
                                notPaid,
                                partialPaid,
                                pageable,
                                LabBillingProjection.class
                        );

                if (projections == null || projections.isEmpty()) {
                    return ResponseUtils.createSuccessResponse(
                            Page.empty(pageable),
                            new TypeReference<Page<LabBillingPatientResponse>>() {
                            }
                    );
                }

                List<LabBillingPatientResponse> responseList =
                        projections.getContent().stream().map(p -> {

                            LabBillingPatientResponse response = new LabBillingPatientResponse();

                            response.setRegistrationNo(p.getRegistrationNo());
                            response.setMobileNo(p.getMobileNumber());
                            response.setAppointmentDate(p.getAppointmentDate());
                            response.setPatientName(p.getPatientName());
                            response.setAge(ageCalculator(p.getAge()));
                            response.setGender(p.getGenderName());
                            response.setBillingType(p.getServiceCategoryName());
                            response.setBillingHeaderId(p.getBillingHeaderId());
                            response.setDgOrderHdId(p.getOrderId());
                            response.setBillAmount(p.getNetAmount());
                            response.setPatientId(p.getPatientId());

                            return response;

                        }).toList();

                Page<LabBillingPatientResponse> pageResult =
                        new PageImpl<>(responseList, pageable, projections.getTotalElements());

                return ResponseUtils.createSuccessResponse(
                        pageResult,
                        new TypeReference<Page<LabBillingPatientResponse>>() {
                        }
                );
            }
            if (radioServiceCategoryCode.equalsIgnoreCase(serviceCategoryCode)) {

                Page<RadiologyBillingProjection> billingHeaders =
                        billingHeaderRepository.findPendingBillingByCategoryId(
                                categoryId,
                                patientName,
                                mobileNo,
                                registrationNo,
                                notPaid,
                                partialPaid,
                                pageable,
                                RadiologyBillingProjection.class
                        );

                List<RadiologyBillingResponse> response = billingHeaders.getContent()
                        .stream()
                        .map(p -> {
                            RadiologyBillingResponse r = new RadiologyBillingResponse();

                            r.setBillingHeaderId(p.getBillingHeaderId());
                            r.setPatientId(p.getPatientId());
                            r.setRegistrationNo(p.getRegistrationNo());
                            r.setPatientName(p.getPatientName());
                            r.setAge(ageCalculator(p.getAge()));
                            r.setGender(p.getGenderName());
                            r.setBillingType(p.getServiceCategoryName());
                            r.setMobileNo(p.getMobileNumber());
                            r.setAppointmentDate(p.getAppointmentDate());
                            r.setBillAmount(p.getNetAmount());

                            return r;
                        }).toList();

                Page<RadiologyBillingResponse> pageResult =
                        new PageImpl<>(response, pageable, billingHeaders.getTotalElements());

                return ResponseUtils.createSuccessResponse(
                        pageResult,
                        new TypeReference<Page<RadiologyBillingResponse>>() {
                        }
                );
            }

            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<Object>() {
                    },
                    "Invalid service category",
                    400
            );

        } catch (Exception e) {
            log.error("Error while fetching billing patients by category: {}", serviceCategoryCode, e);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<Object>() {
                    },
                    "Something went wrong while fetching billing patients",
                    500
            );
        }
    }

    @Override
    public ApiResponse<PatientAppointmentResponse> getOPDPatientBillDetails(Long patientId) {
        log.info("Fetching OPD billing details for patientId={}", patientId);
        String visitStatus = AppConstants.VISIT_STATUS_PENDING;
        String paymentStatusPending = AppConstants.PAYMENT_PARTIAL_PENDING;
        String paymentStatusPartial = AppConstants.PAYMENT_NOT_PAID;


        try {
            PatientProjection patient = billingHeaderRepository.getPatientDetails(patientId);

            List<VisitBillingProjection> visits = billingHeaderRepository.getVisitBillingDetails(patientId, opdServiceCategoryCode, visitStatus, paymentStatusPending, paymentStatusPartial, regServiceCategoryCode);

            PatientAppointmentResponse response = new PatientAppointmentResponse();

            response.setPatientid(patient.getId());
            response.setPatientName(patient.getFullName());
            response.setMobileNo(patient.getPatientMobileNumber());
            response.setAge(patient.getPatientAge());
            response.setGender(patient.getGender());
            response.setAddress(patient.getAddress());
            response.setRelation(patient.getRelation());
            response.setPatientUhid(patient.getUhidNo());


            List<AppointmentBlock> appointments = visits.stream().map(v -> {

                AppointmentBlock block = new AppointmentBlock();

                block.setVisitType(v.getVisitType());
                block.setTokenNo(v.getTokenNo());
                block.setDepartment(v.getDepartmentName());
                block.setConsultedDoctor(v.getConsultedDoctor());
                block.setSessionName(v.getSessionName());
                block.setVisitDate(v.getVisitDate());
                block.setBillingHdId(v.getBillingHdId());
                block.setTariff(v.getTariff());
                block.setDiscount(v.getDiscountAmount());
                block.setTaxPercent(v.getTaxPercent());
                block.setTaxAmount(v.getTaxAmount());
                block.setNetAmount(v.getNetAmount());
                block.setTotalAmount(v.getTotalAmount());
                block.setRegistrationCost(v.getRegistrationCost());
                block.setPolicyCode(v.getPolicyCode());
                block.setPolicyType(v.getPolicyType());
                block.setPolicyDiscountPercent(v.getPolicyDiscountPercent());
                block.setPolicyDescription(v.getPolicyDescription());
                block.setPolicyEligibilityDays(v.getPolicyEligibilityDays());

                return block;

            }).toList();

            response.setAppointments(appointments);


            if (visits.isEmpty()) {
                log.warn("No billing records found for patientId={}", patientId);
                return ResponseUtils.createFailureResponse(
                        null,
                        new TypeReference<PatientAppointmentResponse>() {
                        },
                        "No billing records found for the patient",
                        404
                );
            }


            log.info("Billing details fetched successfully for patientId={}", patientId);
            return ResponseUtils.createSuccessResponse(
                    response,
                    new TypeReference<PatientAppointmentResponse>() {
                    }
            );
        } catch (Exception e) {
            log.error("Error occurred while fetching billing details for patientId={}", patientId, e);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<PatientAppointmentResponse>() {
                    },
                    "Error fetching billing details",
                    500
            );
        }
    }

    @Override
    public ApiResponse<Page<BillingHeaderResponseProjection>> searchInvoiceDetails(
            String patientName, String phoneNo, String registrationNo, Pageable pageable) {
        try {
            String patientNameLike = (patientName == null || patientName.trim().isEmpty())
                    ? null
                    : "%" + patientName.trim().toLowerCase() + "%";

            String phoneNoLike = (phoneNo == null || phoneNo.trim().isEmpty())
                    ? null
                    : "%" + phoneNo.trim() + "%";

            String registrationNoLike = (registrationNo == null || registrationNo.trim().isEmpty())
                    ? null
                    : "%" + registrationNo.trim().toLowerCase() + "%";

            Page<BillingHeaderResponseProjection> response = billingHeaderRepository.searchBillingStatus(
                    patientNameLike,
                    phoneNoLike,
                    registrationNoLike, AppConstants.STATUS_Y, AppConstants.STATUS_P, pageable
            );

            return ResponseUtils.createSuccessResponse(
                    response,
                    new TypeReference<Page<BillingHeaderResponseProjection>>() {
                    }
            );

        } catch (Exception e) {
            log.error("Error while searching billing status", e);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<Page<BillingHeaderResponseProjection>>() {
                    },
                    "Something went wrong while searching billing status",
                    500
            );
        }
    }


    @Override
    @Transactional
    public ApiResponse<PaymentResponse> processOpdPayment(PaymentUpdateRequest request) {
        PaymentResponse res = new PaymentResponse();
        BillingHeader header;
        List<PaymentUpdateRequest.OpdBillPayment> opdPayments = request.getOpdBillPayments();
        if (opdPayments == null || opdPayments.isEmpty()) {
            throw new RuntimeException("OPD payment items missing in request.");
        }

        List<OpdPaymentItem> paymentItemList = new ArrayList<>();

        for (PaymentUpdateRequest.OpdBillPayment opd : opdPayments) {
            Integer billHeaderId = opd.getBillHeaderId();
            BigDecimal netAmount = opd.getNetAmount();

            Optional<BillingHeader> headerOpt = billingHeaderRepository.findById(billHeaderId);
            if (headerOpt.isPresent()) {
                header = headerOpt.get();
            } else {
                throw new RuntimeException("BillingHeader not found with id: " + billHeaderId);
            }
            List<BillingDetail> details = billingDetailRepository.findByBillHdId(Long.valueOf(billHeaderId));
            if (!details.isEmpty()) {
                for (BillingDetail bdt : details) {
                    bdt.setChargeCost(bdt.getNetAmount());
                    bdt.setPaymentStatus("y");
                }
            }
            Visit visit = header.getVisit();
            if (visit == null) {
                throw new RuntimeException("Visit not linked with OPD Bill Header " + billHeaderId);
            }

            PaymentDetail paymentDetail = new PaymentDetail();
            paymentDetail.setPaymentMode(request.getMode());
            paymentDetail.setPaymentStatus("y");
            paymentDetail.setPaymentReferenceNo(request.getPaymentReferenceNo());
            paymentDetail.setPaymentDate(Instant.now());
            paymentDetail.setAmount(netAmount);
            paymentDetail.setCreatedBy(authUtil.getCurrentUser().getFirstName());
            paymentDetail.setCreatedAt(Instant.now());
            paymentDetail.setUpdatedAt(Instant.now());
            paymentDetail.setBillingHd(header);
            paymentDetailRepository.save(paymentDetail);

            BigDecimal oldPaid = header.getTotalPaid() == null ? BigDecimal.ZERO : header.getTotalPaid();
            header.setTotalPaid(oldPaid.add(netAmount));
            header.setPaymentStatus("y");
            billingHeaderRepository.save(header);

            visit.setBillingStatus("y");
            visit.setBillingHd(header);
            visitRepository.save(visit);

            OpdPaymentItem item = new OpdPaymentItem();
            item.setBillHeaderId(billHeaderId);
            item.setVisitId(visit.getId());
            item.setNetAmount(netAmount);
            item.setPatientName(visit.getPatient().getFullName());
            item.setTokenNo(visit.getTokenNo());
            item.setDoctorName(visit.getDoctorName());
            paymentItemList.add(item);
        }
        res.setMsg("Success");
        res.setPaymentStatus("y");
        res.setBillPayments(paymentItemList);
        return ResponseUtils.createSuccessResponse(res, new TypeReference<>() {
        });
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<PaymentResponse> processLabPayment(PaymentUpdateRequest request) {

        log.info("Starting LAB payment update");
        log.debug("Request: {}", request);

        PaymentResponse res = new PaymentResponse();
        try {
            BillingHeader billingHeader = billingHeaderRepository
                    .findById(request.getBillHeaderId())
                    .orElseThrow(() -> new RuntimeException("BillingHeader not found"));

            PaymentDetail paymentDetail = new PaymentDetail();
            paymentDetail.setPaymentMode(request.getMode());
            paymentDetail.setPaymentStatus(AppConstants.PAYMENT_PAID);
            paymentDetail.setPaymentReferenceNo(request.getPaymentReferenceNo());
            paymentDetail.setPaymentDate(Instant.now());
            paymentDetail.setAmount(request.getAmount());
            paymentDetail.setCreatedBy(authUtil.getCurrentUser().getFirstName());
            paymentDetail.setCreatedAt(Instant.now());
            paymentDetail.setUpdatedAt(Instant.now());
            paymentDetail.setBillingHd(billingHeader);

            PaymentDetail saved = paymentDetailRepository.save(paymentDetail);
            log.info("PaymentDetail saved, id={}", saved.getId());

            for (InvestigationandPackegBillStatus item : request.getInvestigationandPackegBillStatus()) {

                int billHdId = request.getBillHeaderId();

                if ("i".equalsIgnoreCase(item.getType())) {
                    billingDetailRepository.updatePaymentStatusInvestigation(
                            AppConstants.PAYMENT_PAID, item.getId(), billHdId);

                    labDtRepository.updatePaymentStatusInvestigationDt(
                            AppConstants.PAYMENT_PAID, item.getId(), billHdId);

                } else {
                    billingDetailRepository.updatePaymentStatuPackeg(
                            AppConstants.PAYMENT_PAID, item.getId(), billHdId);

                    labDtRepository.updatePaymentStatusPackegDt(
                            AppConstants.PAYMENT_PAID, item.getId(), billHdId);
                }
            }

            boolean fullyPaid = true;

            List<DgOrderDt> dtList = labDtRepository.findByStatus(request.getBillHeaderId());

            for (DgOrderDt dt : dtList) {
                if (AppConstants.PAYMENT_NOT_PAID.equalsIgnoreCase(dt.getBillingStatus())) {
                    fullyPaid = false;
                    break;
                }
            }

            boolean partialPaid = !fullyPaid;

            DgOrderHd orderHd = billingHeader.getHdorder();
            Visit visit = visitRepository.findByBillingHd(billingHeader);

            BigDecimal totalPaidDB = Optional.ofNullable(billingHeader.getTotalPaid())
                    .orElse(BigDecimal.ZERO);

            BigDecimal totalPaidUI = Optional.ofNullable(request.getAmount())
                    .orElse(BigDecimal.ZERO);

            billingHeader.setTotalPaid(totalPaidDB.add(totalPaidUI));

            if (fullyPaid) {
                orderHd.setPaymentStatus(AppConstants.PAYMENT_PAID);
                billingHeader.setPaymentStatus(AppConstants.PAYMENT_PAID);
                if (visit != null) visit.setBillingStatus(AppConstants.PAYMENT_PAID);
                res.setPaymentStatus(AppConstants.PAYMENT_PAID);

            } else {
                orderHd.setPaymentStatus(AppConstants.PAYMENT_PARTIAL_PENDING);
                billingHeader.setPaymentStatus(AppConstants.PAYMENT_PARTIAL_PENDING);
                if (visit != null) visit.setBillingStatus(AppConstants.PAYMENT_PARTIAL_PENDING);
                res.setPaymentStatus(AppConstants.PAYMENT_PARTIAL_PENDING);
            }

            labHdRepository.save(orderHd);
            if (visit != null) visitRepository.save(visit);
            billingHeaderRepository.save(billingHeader);

            res.setBillNo(billingHeader.getBillNo());
            res.setMsg("Success");

            log.info("LAB payment completed successfully");

        } catch (Exception e) {
            log.error("Error in LAB payment", e);
            throw new BillingException("Payment failed: " + e.getMessage());
        }

        return ResponseUtils.createSuccessResponse(res, new TypeReference<PaymentResponse>() {
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<PaymentResponse> processRadiologyPayment(PaymentUpdateRequest request) {

        log.info("Starting payment status update process");
        log.debug("Received PaymentUpdateRequest: {}", request);
        PaymentResponse res = new PaymentResponse();
        try {
            BillingHeader billingHeader = billingHeaderRepository
                    .findById(request.getBillHeaderId())
                    .orElseThrow(() -> new RuntimeException("BillingHeader not found"));

            PaymentDetail paymentDetail = new PaymentDetail();
            paymentDetail.setPaymentMode(request.getMode());
            paymentDetail.setPaymentStatus(AppConstants.PAYMENT_PAID);
            paymentDetail.setPaymentReferenceNo(request.getPaymentReferenceNo());
            paymentDetail.setPaymentDate(Instant.now());
            paymentDetail.setAmount(request.getAmount());
            paymentDetail.setCreatedBy(authUtil.getCurrentUser().getFirstName());
            paymentDetail.setCreatedAt(Instant.now());
            paymentDetail.setUpdatedAt(Instant.now());
            paymentDetail.setBillingHd(billingHeader);

            PaymentDetail savedDetail = paymentDetailRepository.save(paymentDetail);
            log.info("PaymentDetail saved successfully, id={}", savedDetail.getId());

            for (InvestigationandPackegBillStatus invpkg : request.getInvestigationandPackegBillStatus()) {

                int billHdId = request.getBillHeaderId();

                if ("i".equalsIgnoreCase(invpkg.getType())) {
                    billingDetailRepository.updatePaymentStatusInvestigation(AppConstants.PAYMENT_PAID, invpkg.getId(), billHdId);
                    radOrderDtRepository.updatePaymentStatusInvestigationDt(AppConstants.PAYMENT_PAID, invpkg.getId(), billHdId);
                } else {
                    billingDetailRepository.updatePaymentStatuPackeg(AppConstants.PAYMENT_PAID, invpkg.getId(), billHdId);
                    radOrderDtRepository.updatePaymentStatusPackegDt(AppConstants.PAYMENT_PAID,
                            (long) invpkg.getId(), (long) billHdId);
                }
            }

            boolean fullyPaid = true;
            List<RadOrderDt> dtList =
                    radOrderDtRepository.findUnbilledByBillingHdId((long) request.getBillHeaderId());

            for (RadOrderDt orderDt : dtList) {
                if (AppConstants.PAYMENT_NOT_PAID.equalsIgnoreCase(orderDt.getBillingStatus())) {
                    fullyPaid = false;
                    break;
                }
            }
            boolean partialPaid = !fullyPaid;
            RadOrderHd orderHd = billingHeader.getRadOrderHd();
            Optional<Visit> visitOpt =
                    visitRepository.findByBillingHd_Id(Long.valueOf(request.getBillHeaderId()));

            BigDecimal totalPaidDB = Optional.ofNullable(billingHeader.getTotalPaid())
                    .orElse(BigDecimal.ZERO);

            BigDecimal totalPaidUi = Optional.ofNullable(request.getAmount())
                    .orElse(BigDecimal.ZERO);

            billingHeader.setTotalPaid(totalPaidDB.add(totalPaidUi));

            if (fullyPaid) {
                orderHd.setPaymentStatus(AppConstants.PAYMENT_PAID);
                billingHeader.setPaymentStatus(AppConstants.PAYMENT_PAID);
                visitOpt.ifPresent(v -> v.setBillingStatus(AppConstants.PAYMENT_PAID));
                res.setPaymentStatus(AppConstants.PAYMENT_PAID);
            } else {
                orderHd.setPaymentStatus(AppConstants.PAYMENT_PARTIAL_PENDING);
                billingHeader.setPaymentStatus(AppConstants.PAYMENT_PARTIAL_PENDING);
                visitOpt.ifPresent(v -> v.setBillingStatus(AppConstants.PAYMENT_PARTIAL_PENDING));
                res.setPaymentStatus(AppConstants.PAYMENT_PARTIAL_PENDING);
            }

            radOrderHdRepository.save(orderHd);
            visitOpt.ifPresent(visitRepository::save);
            billingHeaderRepository.save(billingHeader);
            res.setBillNo(billingHeader.getBillNo());
            res.setMsg("Success");
            log.info("Payment status update completed successfully");

        } catch (Exception e) {
            log.error("Unexpected error during payment status update", e);
            throw new BillingException("Payment failed: " + e.getMessage());
        }
        return ResponseUtils.createSuccessResponse(res, new TypeReference<PaymentResponse>() {
        });
    }

    // Your existing mapToResponse method (for BillingHeader)
    private PendingBillingResponse mapToResponse(BillingHeader header) {
        PendingBillingResponse response = new PendingBillingResponse();
        response.setBillinghdid(header.getId());
        response.setPatientName(safe(header.getPatientDisplayName()));
        response.setAddress(header.getPatientAddress());
        response.setVisitId(header.getVisit().getId());


        if (header.getVisit() != null && header.getVisit().getPatient() != null) {
            response.setPatientid(header.getVisit().getPatient().getId());
            response.setPatientUhid(header.getVisit().getPatient().getUhidNo());
            response.setTokenNo(header.getVisit().getTokenNo());
            String sessionName = Optional.ofNullable(header.getVisit())
                    .map(v -> v.getSession())
                    .map(s -> s.getSessionName())
                    .orElse(null);

            response.setSessionName(sessionName);
            response.setVisitType(header.getVisit().getVisitType());
            response.setVisitDate(header.getVisit().getVisitDate());

        } else {
            response.setPatientid(null);
        }
        if (header.getVisit() != null && header.getVisit().getPatient() != null) {
            response.setMobileNo(safe(header.getVisit().getPatient().getPatientMobileNumber()));
        } else {
            response.setMobileNo("");
        }
        if (header.getVisit() != null && header.getVisit().getPatient() != null &&
                header.getVisit().getPatient().getPatientDob() != null) {
            String ageStr = ageCalculator(header.getVisit().getPatient().getPatientDob());
            response.setAge(ageStr);
        } else {
            response.setAge("");
        }
        response.setGender(safe(header.getPatientGender()));
        if (header.getVisit() != null && header.getVisit().getPatient() != null &&
                header.getVisit().getPatient().getPatientRelation() != null) {
            response.setRelation(safe(header.getVisit().getPatient().getPatientRelation().getRelationName()));
        } else {
            response.setRelation("");
        }
        response.setConsultedDoctor(safe(header.getReferredBy()));
        if (header.getVisit() != null && header.getVisit().getDepartment() != null) {
            response.setDepartment(safe(header.getVisit().getDepartment().getDepartmentName()));
        } else {
            response.setDepartment("");
        }
        if (header.getServiceCategory() != null) {
            response.setBillingType(safe(header.getServiceCategory().getServiceCatName()));
        } else {
            response.setBillingType("");
        }

        response.setFlag("Direct");
        response.setAmount(
                header.getNetAmount() != null
                        ? header.getNetAmount().subtract(
                        header.getTotalPaid() != null ? header.getTotalPaid() : BigDecimal.ZERO
                )
                        : BigDecimal.ZERO
        );
        response.setBillingStatus(safe(header.getPaymentStatus()));
        response.setOrderhdid(null);
        response.setOrderhdPaymentStatus(null);

        List<BillingDetail> detailsList = billingDetailRepository.findByBillHdIdAndPaymentStatusIn(
                header.getId(), List.of("n", "p")
        );

        Optional<BillingDetail> registrationServiceOpt = detailsList.stream()
                .filter(bd -> bd.getServiceCategory() != null &&
                        "Registration Service".equalsIgnoreCase(bd.getServiceCategory().getServiceCatName()))
                .findFirst();
        BigDecimal registrationCost = registrationServiceOpt
                .map(bd -> bd.getServiceCategory().getRegistrationCost()) // Replace with actual method to get the value you need
                .orElse(null);

        List<BillingDetailResponse> details = detailsList.stream()
                .filter(bd -> bd.getServiceCategory() == null ||    // keep if null
                        !"Registration Service".equalsIgnoreCase(
                                bd.getServiceCategory().getServiceCatName()
                        )
                )
                .map(this::mapToDetailResponse)
                .collect(Collectors.toList());
        if (header.getBillingPolicy() != null) {
            response.setBillingPolicyId(header.getBillingPolicy().getBillingPolicyId());
        }
        response.setRegistrationCost(registrationCost);
        response.setDetails(details);
        return response;
    }

    //    // ✅ NEW METHOD: Map OrderHd to PendingBillingResponse
    private PendingBillingResponse mapOrderToResponse(DgOrderHd orderHd) {
        PendingBillingResponse response = new PendingBillingResponse();
        response.setOrderhdid(orderHd.getId());
        response.setOrderhdPaymentStatus(safe(orderHd.getPaymentStatus()));
        response.setSource(safe(orderHd.getSource())); // Add source to response

        // Set billing fields as null for order records
        response.setBillinghdid(null);
        response.setBillingStatus(null);

        response.setFlag("OPD");

        // ✅ Patient information
        if (orderHd.getPatientId() != null) {
            response.setPatientid(orderHd.getPatientId().getId());
            response.setPatientName(safe(
                    orderHd.getPatientId().getPatientFn() + " " +
                            safe(orderHd.getPatientId().getPatientMn()) + " " +
                            safe(orderHd.getPatientId().getPatientLn())
            ).trim());
            response.setMobileNo(safe(orderHd.getPatientId().getPatientMobileNumber()));
            response.setAddress(safe(orderHd.getPatientId().getPatientAddress1()) + " " +
                    safe(orderHd.getPatientId().getPatientAddress2()));

            // ✅ Age calculation
            if (orderHd.getPatientId().getPatientDob() != null) {
                String ageStr = ageCalculator(orderHd.getPatientId().getPatientDob());
                response.setAge(ageStr);
            } else {
                response.setAge("");
            }

            // ✅ Sex/Gender
            response.setGender(safe(orderHd.getPatientId().getPatientGender() != null ?
                    orderHd.getPatientId().getPatientGender().getGenderName() : ""));

            // ✅ Relation
            if (orderHd.getPatientId().getPatientRelation() != null) {
                response.setRelation(safe(orderHd.getPatientId().getPatientRelation().getRelationName()));
            }
        }

        response.setBillingType(null);
        response.setConsultedDoctor("");

        // ✅ Department
        if (orderHd.getVisitId() != null && orderHd.getVisitId().getDepartment() != null) {
            response.setDepartment(orderHd.getVisitId().getDepartment().getDepartmentName());
        } else {
            response.setDepartment("");
        }

        // ✅ Amount calculation
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<BillingDetailResponse> details = new ArrayList<>();

        // Fetch order details to calculate amount and set details
        List<DgOrderDt> orderDetails = labDtRepository.findByOrderhdIdAndBillingStatus(orderHd, "n");

        for (DgOrderDt orderDetail : orderDetails) {
            BillingDetailResponse detailResponse = mapOrderDetailToResponse(orderDetail);
            details.add(detailResponse);

            // ✅ Calculate total using the correctly mapped net amount
            totalAmount = totalAmount.add(detailResponse.getNetAmount());
        }

        response.setAmount(totalAmount);
        response.setDetails(details);

        return response;
    }

    // ✅ NEW METHOD: Map Order Detail to BillingDetailResponse
    private BillingDetailResponse mapOrderDetailToResponse(DgOrderDt orderDetail) {
        BillingDetailResponse response = new BillingDetailResponse();

        response.setId((long) orderDetail.getId());

        BigDecimal basePrice = BigDecimal.ZERO;
        BigDecimal tariff = BigDecimal.ZERO;

        // ✅ CASE 1: Investigation - Get price from InvestigationPriceDetails table
        if (orderDetail.getInvestigationId() != null) {
            DgMasInvestigation investigation = orderDetail.getInvestigationId();

            response.setInvestigationId(investigation.getInvestigationId());
            response.setInvestigationName(safe(investigation.getInvestigationName()));
            response.setItemName(safe(investigation.getInvestigationName()));

            // ✅ Get actual price from investigation_price_details table
            basePrice = getCurrentInvestigationPrice(investigation);
            tariff = basePrice;
        }

        // ✅ CASE 2: Package - Get price directly from Package entity
        if (orderDetail.getPackageId() != null) {
            DgInvestigationPackage package_obj = orderDetail.getPackageId();

            response.setPackageId(package_obj.getPackId());
            response.setPackageName(safe(package_obj.getPackName()));

            // If package exists but investigation doesn't, use package pricing
            if (orderDetail.getInvestigationId() == null) {
                response.setItemName(safe(package_obj.getPackName()));
            }

            // ✅ Get actual price directly from package entity fields
            // Use actualCost if available, otherwise use baseCost
            if (package_obj.getActualCost() > 0) {
                basePrice = BigDecimal.valueOf(package_obj.getActualCost());
                tariff = BigDecimal.valueOf(package_obj.getActualCost());
            } else if (package_obj.getBaseCost() > 0) {
                basePrice = BigDecimal.valueOf(package_obj.getBaseCost());
                tariff = BigDecimal.valueOf(package_obj.getBaseCost());
            }
        }

        response.setBasePrice(basePrice);
        response.setTariff(tariff);
        response.setQuantity(orderDetail.getOrderQty() > 0 ? orderDetail.getOrderQty() : 1);
        // ✅ Get discount from order detail
        BigDecimal discount = orderDetail.getDiscountAmt() != null ?
                BigDecimal.valueOf(orderDetail.getDiscountAmt()) : BigDecimal.ZERO;

        response.setDiscount(discount);

        // ✅ Calculate net amounts
        BigDecimal charge = basePrice.multiply(BigDecimal.valueOf(response.getQuantity()));
        BigDecimal netAmount = charge.subtract(discount);

        response.setAmountAfterDiscount(netAmount);
        response.setNetAmount(netAmount);
        response.setTotal(netAmount);

        response.setTaxPercent(BigDecimal.ZERO);
        response.setTaxAmount(BigDecimal.ZERO);
        response.setPaymentStatus(safe(orderDetail.getBillingStatus()));

        return response;
    }

    public List<PendingBillingResponse> mergeConsultation(List<PendingBillingResponse> list) {

        Map<String, List<PendingBillingResponse>> groups = new LinkedHashMap<>();
        List<PendingBillingResponse> finalList = new ArrayList<>();

        for (PendingBillingResponse item : list) {
            if (!"Consultation Services".equalsIgnoreCase(item.getBillingType())) {
                finalList.add(item);
                continue;
            }
            String key = item.getPatientid() + "|"
                    + item.getBillingType();

            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(item);
        }
        for (List<PendingBillingResponse> group : groups.values()) {
            if (group.size() == 1) {
                PendingBillingResponse single = group.get(0);
                single.setBillingHeaderIds(
                        Collections.singletonList(single.getBillinghdid())
                );
                AppointmentBlock ab = new AppointmentBlock();
                ab.setBillingPolicyId(single.getBillingPolicyId());
                ab.setBillingHdId(single.getBillinghdid());
                ab.setConsultedDoctor(single.getConsultedDoctor());
                ab.setDepartment(single.getDepartment());
                ab.setSessionName(single.getSessionName());
                ab.setVisitDate(single.getVisitDate());
                ab.setTokenNo(single.getTokenNo());
                ab.setVisitType(single.getVisitType());

                single.setAppointments(Collections.singletonList(ab));
                if (single.getDetails() == null) {
                    single.setDetails(new ArrayList<>());
                }
                finalList.add(single);
                continue;
            }
            PendingBillingResponse merged = new PendingBillingResponse();
            PendingBillingResponse first = group.get(0);

            merged.setPatientid(first.getPatientid());
            merged.setPatientUhid(first.getPatientUhid());
            merged.setPatientName(first.getPatientName());
            merged.setMobileNo(first.getMobileNo());
            merged.setAge(first.getAge());
            merged.setGender(first.getGender());
            merged.setRelation(first.getRelation());
            merged.setBillingType(first.getBillingType());
            merged.setConsultedDoctor(first.getConsultedDoctor());
            merged.setDepartment(first.getDepartment());
            merged.setAddress(first.getAddress());
            merged.setVisitType(first.getVisitType());
            merged.setVisitDate(first.getVisitDate());
            merged.setSessionName(first.getSessionName());
            merged.setBillingStatus(first.getBillingStatus());
            merged.setTokenNo(first.getTokenNo());
            merged.setVisitDate(first.getVisitDate());
            merged.setVisitType(first.getVisitType());
            merged.setRegistrationCost(first.getRegistrationCost());
            merged.setBillingHeaderIds(
                    group.stream()
                            .map(PendingBillingResponse::getBillinghdid)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList())
            );
            List<AppointmentBlock> appointmentList = new ArrayList<>();
            for (PendingBillingResponse item : group) {
                AppointmentBlock ab = new AppointmentBlock();
                ab.setBillingPolicyId(item.getBillingPolicyId());
                ab.setBillingHdId(item.getBillinghdid());
                ab.setConsultedDoctor(item.getConsultedDoctor());
                ab.setDepartment(item.getDepartment());
                ab.setSessionName(item.getSessionName());
                ab.setVisitDate(item.getVisitDate());
                ab.setTokenNo(item.getTokenNo());
                ab.setVisitType(item.getVisitType());
                appointmentList.add(ab);
            }
            merged.setAppointments(appointmentList);

            // Merge details
            merged.setDetails(
                    group.stream()
                            .flatMap(it -> it.getDetails() == null ? Stream.empty() : it.getDetails().stream())
                            .collect(Collectors.toList())
            );

            // Sum amounts
            merged.setAmount(
                    group.stream()
                            .map(PendingBillingResponse::getAmount)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
            );

            // Final billinghdid = first ID
            merged.setBillinghdid(merged.getBillingHeaderIds().get(0));
            finalList.add(merged);
        }

        return finalList;
    }


    // ✅ METHOD: Get current investigation price from price details table
    private BigDecimal getCurrentInvestigationPrice(DgMasInvestigation investigation) {
        try {
            LocalDate today = LocalDate.now();

            // First try to get active price for current date
            Optional<MasInvestigationPriceDetails> priceDetail = masInvestigationPriceDetailsRepository
                    .findActivePriceByInvestigationAndDate(investigation, today);

            if (priceDetail.isPresent() && priceDetail.get().getPrice() != null) {
                return priceDetail.get().getPrice();
            }

            // Fallback: if no active price found, try to get the latest price
            Optional<MasInvestigationPriceDetails> latestPrice = masInvestigationPriceDetailsRepository
                    .findTopByInvestigationOrderByFromDateDesc(investigation);

            if (latestPrice.isPresent() && latestPrice.get().getPrice() != null) {
                return latestPrice.get().getPrice();
            }

            // Final fallback: check if investigation has direct price field
            if (investigation.getPrice() != null) {
                return BigDecimal.valueOf(investigation.getPrice());
            }

        } catch (Exception e) {
            System.err.println("Error fetching price for investigation: " +
                    (investigation != null ? investigation.getInvestigationName() : "null") +
                    " - " + e.getMessage());
        }

        return BigDecimal.ZERO;
    }

    // Your existing mapToDetailResponse method
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
        d.setRegistrationCost(detail.getRegistrationCost());
        d.setTotal(detail.getNetAmount());
        if (detail.getInvestigation() != null) {
            d.setInvestigationId(detail.getInvestigation().getInvestigationId());
            d.setInvestigationName(detail.getInvestigation().getInvestigationName());
        }

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
    public ApiResponse<List<PendingBillingResponse>> getLabRadiologyBillingDetails(Long billingHdId, String serviceCategoryCode) {
        try {

            MasServiceCategory serviceCategory =
                    masServiceCategoryRepository.findByServiceCateCode(serviceCategoryCode);

            Long categoryId = serviceCategory.getId();

            String notPaid = AppConstants.PAYMENT_NOT_PAID;
            String partialPaid = AppConstants.PAYMENT_PARTIAL_PENDING;

            List<LabRadioBillingDetailsProjection> detailsList =
                    billingHeaderRepository.getUnpaidBillingDetailsByBillingHdId(billingHdId, categoryId, notPaid, partialPaid);

            PendingBillingResponse response = mapPendingBilling(detailsList);

            List<PendingBillingResponse> responses = new ArrayList<>();
            if (response != null) {
                responses.add(response);
            }

            return ResponseUtils.createSuccessResponse(
                    responses,
                    new TypeReference<List<PendingBillingResponse>>() {
                    }
            );

        } catch (Exception e) {
            log.error("Error fetching Lab/Radiology billing details", e);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {
                    },
                    "Internal Server Error",
                    500
            );
        }
    }

    public PendingBillingResponse mapPendingBilling(List<LabRadioBillingDetailsProjection> rows) {

        if (rows == null || rows.isEmpty()) {
            return null;
        }

        LabRadioBillingDetailsProjection first = rows.get(0);

        PendingBillingResponse response = new PendingBillingResponse();

        response.setVisitId(first.getVisitId());
        response.setBillinghdid(first.getBillinghdid());
        response.setPatientid(first.getPatientid());

        response.setPatientName(
                Stream.of(first.getFirstName(), first.getMiddleName(), first.getLastName())
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining(" "))
        );

        response.setMobileNo(first.getMobileNo());
        response.setGender(first.getGender());
        response.setAge(ageCalculator(first.getDob()));
        response.setAmount(first.getBillHdTotalAmount());
        response.setBillingStatus(first.getBillingStatus());
        response.setVisitDate(first.getVisitDate());

        // fields not in query remain null
        response.setRelation(first.getRelation());
        response.setBillingType(first.getBillingType());
        response.setDepartment(first.getDepartment());
        response.setAddress(first.getAddress());
        response.setOrderhdid(first.getOrderhdid());
        response.setOrderhdPaymentStatus(first.getOrderhdPaymentStatus());
        response.setFlag(null);
        response.setSource(null);
        response.setPatientUhid(first.getUhidNo());

        List<BillingDetailResponse> details = rows.stream().map(r -> {

            BillingDetailResponse d = new BillingDetailResponse();

            d.setId(r.getBillingdtId());
            d.setItemName(r.getItemName());
            d.setQuantity(r.getQuantity());
            d.setBasePrice(r.getBasePrice());
            d.setTariff(r.getTariff());
            d.setDiscount(r.getDiscount());
            d.setAmountAfterDiscount(r.getAmountAfterDiscount());
            d.setTaxPercent(r.getTaxPercent());
            d.setTaxAmount(r.getTaxAmount());
            d.setNetAmount(r.getNetAmount());
            d.setTotal(r.getNetAmount());
            d.setPaymentStatus(r.getDetailPaymentStatus());

            d.setRegistrationCost(null);
            d.setInvestigationId(r.getInvestigationId());
            d.setInvestigationName(r.getInvestigationName());
            d.setPackageId(r.getPackageId());
            d.setPackageName(r.getPackageName());

            return d;

        }).collect(Collectors.toList());

        response.setDetails(details);

        return response;
    }
}

