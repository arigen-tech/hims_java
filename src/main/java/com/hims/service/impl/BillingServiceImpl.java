package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.exception.BillingException;
import com.hims.exception.SDDException;
import com.hims.helperUtil.HelperUtils;
import com.hims.mapper.PaidCancelledAppointmentMapper;
import com.hims.projection.*;
import com.hims.request.*;
import com.hims.response.*;
import com.hims.service.BillingService;
import com.hims.service.TransactionSequenceService;
import com.hims.utils.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
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
    private RandomNumGenerator randomNumGenerator;

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

    @Autowired
    OpdRefundDetailsRepository opdRefundDetailsRepository;

    @Autowired
    private LabOrderTrackingStatusRepository orderTrackingStatusRepository;

    @Value("${lab.track-order-status-reg.ordered}")
    private Long orderedStatusId;

    @Autowired
    HelperUtils helperUtils;

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

    @Value("${serviceCategoryLab}")
    private String LabServiceCategoryCode;

    @Autowired
    PaidCancelledAppointmentMapper paidCancelledAppointmentMapper;

    @Autowired
    TransactionSequenceService transactionSequenceService;


    @Override
    @Transactional
    public ApiResponse<OpdBillingPaymentResponse> saveBillingForOpd(Visit visit, MasServiceCategory serviceCategory, MasDiscount discount) {
        BillingHeader header = new BillingHeader();
        OpdBillingPaymentResponse response = new OpdBillingPaymentResponse();
        User currentUser = authUtil.getCurrentUser();
        BigDecimal tax = BigDecimal.ZERO;
        BigDecimal registrationCost = BigDecimal.ZERO;

        BillingPolicyMaster policy;
        Patient patient = visit.getPatient();
            BigDecimal totalDiscount = BigDecimal.valueOf(0);
            header.setBillDate(OffsetDateTime.now());
            header.setPatient(visit.getPatient());
            header.setPatientDisplayName(visit.getPatient().getFullName());
            header.setPatientAge(visit.getPatient().getPatientAge());
            header.setPatientGender(visit.getPatient().getPatientGender().getGenderName());
            header.setPatientAddress(visit.getPatient().getPatientAddress1() + " " + visit.getPatient().getPatientAddress2());
            header.setHospital(visit.getHospital());
            header.setHospitalName(visit.getHospital().getHospitalName());
            header.setHospitalAddress(visit.getHospital().getAddress());
            header.setHospitalMobileNo(visit.getHospital().getContactNumber());
            header.setHospitalGstin(visit.getHospital().getGstnNo());
            header.setReferredBy(visit.getIniDoctor().getFullName());
            header.setGstnBillNo("");
            header.setBillDate(OffsetDateTime.now());
        Instant instant = Instant.now();
        Instant dateOnly = instant
                .atZone(ZoneId.of("Asia/Kolkata"))
                .toLocalDate()
                .atStartOfDay(ZoneId.of("Asia/Kolkata"))
                .toInstant();
            Optional<Visit> lastVisitOpt = visitRepository.findPreviousVisit(
                    patient.getId(),
                    visit.getDoctor().getUserId(),
                    visit.getDepartment().getId(),
                    visit.getHospital().getId(),
                    visit.getId()
            );

            policy = lastVisitOpt
                    .map(lastVisit -> findCorrectBillingPolicy(lastVisit, visit))
                    .orElseGet(() -> billingPolicyRepository.findByBillingPolicyId(opdPaid)
                            .orElseThrow(() -> new EntityNotFoundException(AppConstants.POLICY_NOT_FOUND)));

            header.setBillingPolicy(policy);

            Optional<MasServiceOpd> serviceOpd = masServiceOpdRepository.findByHospitalIdAndDoctorUserIdAndDepartmentIdAndServiceCatIdAndCurrentDate(visit.getHospital(), visit.getDoctor(), visit.getDepartment(), serviceCategory, dateOnly);
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

                if (visit.getVisitType().equalsIgnoreCase(AppConstants.VISIT_TYPE_NEW)) {
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
                throw new BillingException(AppConstants.SERVICE_TARIFF_NOT_DEFINED);
            }
            header.setDiscountAmount(totalDiscount);
            if (visit.getHospital().getAppCostApplicable().equalsIgnoreCase(AppConstants.STATUS_N.toLowerCase())) {
                header.setPaymentStatus(AppConstants.PAYMENT_PAID.toLowerCase());
            } else {
                header.setPaymentStatus(AppConstants.PAYMENT_NOT_PAID.toLowerCase());
            }
            header.setBillNo(transactionSequenceService.generateTransactionNumber(HMISTransaction.BILL_NO, currentUser.getHospital().getId()));
            header.setCreatedBy(currentUser.getFullName());
            header.setUpdatedDt(Instant.now());
            header.setCreatedDt(Instant.now());
            header.setInvoiceNo("");
            header.setUpdatedAt(OffsetDateTime.now());
            header.setBillingDate(Instant.now());
            header.setDiscount(discount);
            header.setVisit(visit);
            header.setServiceCategory(serviceCategory);
            header.setBillingHdId(0);
            BillingHeader savedHeader = billingHeaderRepository.save(header);
            response.setHeader(savedHeader);
            //only for new patient we add new details with same header
            saveRegistrationServiceBillingDetails(visit, savedHeader);

            if (savedHeader != null) {
                saveOpdBillingDetails(visit, serviceCategory, discount, savedHeader, serviceOpd, totalDiscount, tax);
            }
        return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {
        });
    }

    private void saveOpdBillingDetails(Visit visit, MasServiceCategory serviceCategory, MasDiscount discount, BillingHeader savedHeader, Optional<MasServiceOpd> serviceOpd, BigDecimal totalDiscount, BigDecimal tax) {
        BillingDetail detail = new BillingDetail();
        detail.setBillingHd(savedHeader);
        detail.setServiceCategory(serviceCategory);
        detail.setServiceId(0L);
        detail.setItemName("");
        if (visit.getHospital().getAppCostApplicable().equalsIgnoreCase(AppConstants.STATUS_N.toLowerCase())) {
            detail.setPaymentStatus(AppConstants.PAYMENT_PAID.toLowerCase());
        } else {
            detail.setPaymentStatus(AppConstants.PAYMENT_NOT_PAID.toLowerCase());
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
        billingDetailRepository.save(detail);
    }

    public BillingDetail saveRegistrationServiceBillingDetails(Visit visit, BillingHeader savedHeader) {
        BillingDetail detail = new BillingDetail();
        if (visit.getVisitType().equalsIgnoreCase(AppConstants.VISIT_TYPE_NEW)) {
            MasServiceCategory masServiceCategory = masServiceCategoryRepository.findByServiceCateCode(serviceCategoryRegistration);
            if (savedHeader != null) {
                detail = new BillingDetail();
                detail.setBillingHd(savedHeader);
                detail.setServiceCategory(masServiceCategory);
                detail.setServiceId(0L);
                detail.setItemName("");
                if (visit.getHospital().getRegCostApplicable().equalsIgnoreCase(AppConstants.STATUS_N.toLowerCase())) {
                    detail.setPaymentStatus(AppConstants.PAYMENT_PAID.toLowerCase());
                } else {
                    detail.setPaymentStatus(AppConstants.PAYMENT_NOT_PAID.toLowerCase());
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
                billingDetailRepository.save(detail);
            }
        }
        return detail;
    }


    private BillingPolicyMaster findCorrectBillingPolicy(Visit lastVisit, Visit currentVisit) {

        Map<Long, BillingPolicyMaster> policyMap = Stream.of(opdPaid, opdFollowUp)
                .map(id -> billingPolicyRepository.findByBillingPolicyId(id)
                        .orElseThrow(() -> new EntityNotFoundException( AppConstants.POLICY_NOT_FOUND + " for ID: " + id)))
                .collect(Collectors.toMap(BillingPolicyMaster::getBillingPolicyId, p -> p));

        BillingPolicyMaster paidPolicy = policyMap.get(opdPaid);
        BillingPolicyMaster followUpPolicy = policyMap.get(opdFollowUp);

        LocalDate lastVisitDate = lastVisit.getVisitDate().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate currentVisitDate = currentVisit.getVisitDate().atZone(ZoneId.systemDefault()).toLocalDate();

        long daysBetween = ChronoUnit.DAYS.between(lastVisitDate, currentVisitDate);
        boolean isLastVisitCompleted = AppConstants.VISIT_STATUS_COMPLETED.equalsIgnoreCase(lastVisit.getVisitStatus());

        Long lastPolicyId = Optional.ofNullable(lastVisit.getBillingHd())
                .map(BillingHeader::getBillingPolicy)
                .map(BillingPolicyMaster::getBillingPolicyId)
                .orElse(null);

        boolean wasLastVisitFollowUp = followUpPolicy.getBillingPolicyId().equals(lastPolicyId);
        boolean isWithinTimeFrame = daysBetween <= followUpPolicy.getFollowupDaysAllowed();

        if (isLastVisitCompleted && isWithinTimeFrame && !wasLastVisitFollowUp) {
            return followUpPolicy;
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


            MasServiceCategory serviceCategory = masServiceCategoryRepository.findByServiceCateCode(serviceCategoryCode);

            Pageable pageable = PageRequest.of(page, size);

            if (serviceCategory == null) {
                return ResponseUtils.createNotFoundResponse(AppConstants.INVALID_SERVICE_CATEGORY, 404);
            }
            String notPaid = AppConstants.PAYMENT_NOT_PAID.toLowerCase();
            String partialPaid = AppConstants.PAYMENT_PARTIAL_PENDING.toLowerCase();

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
                            r.setAppointmentDate(HelperUtils.instantTimeToLocalDateTime(first.getAppointmentDate()));

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
                                AppConstants.VISIT_STATUS_CANCELLED.toLowerCase(),
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
                            response.setOrderDate(p.getOrderDate());

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
                                AppConstants.VISIT_STATUS_CANCELLED.toLowerCase(),
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
                            r.setOrderDate(p.getOrderDate());

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
                    AppConstants.INVALID_SERVICE_CATEGORY,
                    400
            );

        } catch (Exception e) {
            log.error("Error while fetching billing patients by category: {}", serviceCategoryCode, e);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<Object>() {},AppConstants.BILLING_RECORDS_NOT_FOUND, 500
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
                        AppConstants.BILLING_RECORDS_NOT_FOUND,
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
                    AppConstants.BILLING_RECORDS_NOT_FOUND,
                    500
            );
        }
    }

    @Override
    public ApiResponse<Page<BillingHeaderResponseProjection>> searchInvoiceDetails(
            String patientName, String phoneNo, String registrationNo,Long serviceCategoryId, int page,int size) {
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

            Pageable pageable = PageRequest.of(page, size);

            Page<BillingHeaderResponseProjection> response = billingHeaderRepository.searchBillingStatus(
                    patientNameLike,
                    phoneNoLike,
                    registrationNoLike, AppConstants.STATUS_Y.toLowerCase(), AppConstants.STATUS_P.toLowerCase(),serviceCategoryId, pageable
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
        User currentUser = authUtil.getCurrentUser();
        if (opdPayments == null || opdPayments.isEmpty()) {
            throw new SDDException(500,"OPD payment items missing in request.");
        }

        List<OpdPaymentItem> paymentItemList = new ArrayList<>();

        for (PaymentUpdateRequest.OpdBillPayment opd : opdPayments) {
            Integer billHeaderId = opd.getBillHeaderId();
            BigDecimal netAmount = opd.getNetAmount();

            Optional<BillingHeader> headerOpt = billingHeaderRepository.findById(billHeaderId);
            if (headerOpt.isPresent()) {
                header = headerOpt.get();
            } else {
                throw new SDDException(billHeaderId,AppConstants.BILLING_HEADER_NOT_FOUND );
            }
            List<BillingDetail> details = billingDetailRepository.findByBillHdId(Long.valueOf(billHeaderId));
            if (!details.isEmpty()) {
                for (BillingDetail bdt : details) {
                    bdt.setChargeCost(bdt.getNetAmount());
                    bdt.setPaymentStatus(AppConstants.PAYMENT_PAID.toLowerCase());
                    bdt.setCollectedBy(currentUser);
                    billingDetailRepository.save(bdt);
                }
            }

            Visit visit = header.getVisit();
            if (visit == null) {
                throw new SDDException(billHeaderId,"Visit not linked with OPD Bill Header " );
            }

            PaymentDetail paymentDetail = new PaymentDetail();
            paymentDetail.setPaymentMode(request.getMode());
            paymentDetail.setPaymentStatus(AppConstants.PAYMENT_PAID.toLowerCase());
            paymentDetail.setPaymentReferenceNo(request.getPaymentReferenceNo());
            paymentDetail.setPaymentDate(Instant.now());
            paymentDetail.setAmount(netAmount);
            paymentDetail.setCreatedBy(currentUser.getFullName());
            paymentDetail.setCreatedAt(Instant.now());
            paymentDetail.setUpdatedAt(Instant.now());
            paymentDetail.setBillingHd(header);
            paymentDetailRepository.save(paymentDetail);

            BigDecimal oldPaid = header.getTotalPaid() == null ? BigDecimal.ZERO : header.getTotalPaid();
            header.setTotalPaid(oldPaid.add(netAmount));
            header.setPaymentStatus(AppConstants.PAYMENT_PAID.toLowerCase());
            header.setCreatedBy(currentUser.getFullName());
            billingHeaderRepository.save(header);

            visit.setBillingStatus(AppConstants.PAYMENT_PAID.toLowerCase());
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
        res.setPaymentStatus(AppConstants.PAYMENT_PAID.toLowerCase());
        res.setBillPayments(paymentItemList);
        return ResponseUtils.createSuccessResponse(res, new TypeReference<>() {
        });
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<PaymentResponse> processLabPayment(PaymentUpdateRequest request) {

        log.info("Starting LAB payment update");
        log.debug("Request: {}", request);
        User currentUser = authUtil.getCurrentUser();
        PaymentResponse res = new PaymentResponse();
        try {
            BillingHeader billingHeader = billingHeaderRepository
                    .findById(request.getBillHeaderId())
                    .orElseThrow(() -> new RuntimeException("BillingHeader not found"));

            PaymentDetail paymentDetail = new PaymentDetail();
            paymentDetail.setPaymentMode(request.getMode());
            paymentDetail.setPaymentStatus(AppConstants.PAYMENT_PAID.toLowerCase());
            paymentDetail.setPaymentReferenceNo(request.getPaymentReferenceNo());
            paymentDetail.setPaymentDate(Instant.now());
            paymentDetail.setAmount(request.getAmount());
            paymentDetail.setCreatedBy(currentUser.getFullName());
            paymentDetail.setCreatedAt(Instant.now());
            paymentDetail.setUpdatedAt(Instant.now());
            paymentDetail.setBillingHd(billingHeader);

            PaymentDetail saved = paymentDetailRepository.save(paymentDetail);
            log.info("PaymentDetail saved, id={}", saved.getId());

            for (InvestigationandPackegBillStatus item : request.getInvestigationandPackegBillStatus()) {

                int billHdId = request.getBillHeaderId();

                if (AppConstants.INVESTIGATION.equalsIgnoreCase(item.getType())) {
                    billingDetailRepository.updatePaymentStatusInvestigation(
                            AppConstants.PAYMENT_PAID.toLowerCase(), currentUser, item.getId(), billHdId);

                    labDtRepository.updatePaymentStatusInvestigationDt(
                            AppConstants.PAYMENT_PAID.toLowerCase(), item.getId(), billHdId);

                } else {
                    //for package status
                    billingDetailRepository.updatePaymentStatusPackage(
                            AppConstants.PAYMENT_PAID.toLowerCase(),currentUser, item.getId(), billHdId);

                    labDtRepository.updatePaymentStatusPackageDt(
                            AppConstants.PAYMENT_PAID.toLowerCase(), item.getId(), billHdId);
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

            DgOrderHd orderHd = billingHeader.getHdorder();
            Visit visit = visitRepository.findByBillingHd(billingHeader);

            BigDecimal totalPaidDB = Optional.ofNullable(billingHeader.getTotalPaid())
                    .orElse(BigDecimal.ZERO);

            BigDecimal totalPaidUI = Optional.ofNullable(request.getAmount())
                    .orElse(BigDecimal.ZERO);

            billingHeader.setTotalPaid(totalPaidDB.add(totalPaidUI));
            billingHeader.setCreatedBy(currentUser.getFullName());

            if (fullyPaid) {
                orderHd.setPaymentStatus(AppConstants.PAYMENT_PAID.toLowerCase());
                billingHeader.setPaymentStatus(AppConstants.PAYMENT_PAID.toLowerCase());
                if (visit != null) visit.setBillingStatus(AppConstants.PAYMENT_PAID.toLowerCase());
                res.setPaymentStatus(AppConstants.PAYMENT_PAID.toLowerCase());

            } else {
                orderHd.setPaymentStatus(AppConstants.PAYMENT_PARTIAL_PENDING.toLowerCase());
                billingHeader.setPaymentStatus(AppConstants.PAYMENT_PARTIAL_PENDING.toLowerCase());
                if (visit != null) visit.setBillingStatus(AppConstants.PAYMENT_PARTIAL_PENDING.toLowerCase());
                res.setPaymentStatus(AppConstants.PAYMENT_PARTIAL_PENDING.toLowerCase());
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
        User currentUser = authUtil.getCurrentUser();
        try {


            List<Integer> billIds;
            if (request.getBillHeaderIds() != null && !request.getBillHeaderIds().isEmpty()) {
                billIds = request.getBillHeaderIds()
                        .stream()
                        .map(id -> Integer.parseInt(id.getBillingHdId()))
                        .collect(Collectors.toList());
            } else {
                billIds = List.of(request.getBillHeaderId());
            }
            for (Integer billId : billIds) {
                BillingHeader billingHeader = billingHeaderRepository
                        .findById(billId)
                        .orElseThrow(() -> new SDDException(
                                billId,"BillingHeader not found"));

                PaymentDetail paymentDetail = new PaymentDetail();
                paymentDetail.setPaymentMode(request.getMode());
                paymentDetail.setPaymentStatus(AppConstants.PAYMENT_PAID.toLowerCase());
                paymentDetail.setPaymentReferenceNo(request.getPaymentReferenceNo());
                paymentDetail.setPaymentDate(Instant.now());
                paymentDetail.setAmount(request.getAmount());
                paymentDetail.setCreatedBy(currentUser.getFullName());
                paymentDetail.setCreatedAt(Instant.now());
                paymentDetail.setUpdatedAt(Instant.now());
                paymentDetail.setBillingHd(billingHeader);

                paymentDetailRepository.save(paymentDetail);

                // INVESTIGATION LOOP SAME
                for (InvestigationandPackegBillStatus invpkg : request.getInvestigationandPackegBillStatus()) {

                    if (AppConstants.INVESTIGATION.toLowerCase().equalsIgnoreCase(invpkg.getType())) {
                        billingDetailRepository.updatePaymentStatusInvestigation(
                                AppConstants.PAYMENT_PAID.toLowerCase(),currentUser, invpkg.getId(), billId);

                        radOrderDtRepository.updatePaymentStatusInvestigationDt(
                                AppConstants.PAYMENT_PAID.toLowerCase(), invpkg.getId(), billId);

                    } else {
                        billingDetailRepository.updatePaymentStatusPackage(
                                AppConstants.PAYMENT_PAID.toLowerCase(),currentUser, invpkg.getId(), billId);

                        radOrderDtRepository.updatePaymentStatusPackegDt(
                                AppConstants.PAYMENT_PAID.toLowerCase(),
                                (long) invpkg.getId(), (long) billId);
                    }
                }


                List<RadOrderDt> dtList =
                        radOrderDtRepository.findUnbilledByBillingHdId((long) billId);

                boolean fullyPaid = dtList.stream()
                        .noneMatch(dt -> AppConstants.PAYMENT_NOT_PAID.equalsIgnoreCase(dt.getBillingStatus()));

                RadOrderHd orderHd = billingHeader.getRadOrderHd();

                Optional<Visit> visitOpt =
                        visitRepository.findByBillingHd_Id(Long.valueOf(billId));

                BigDecimal totalPaidDB = Optional.ofNullable(billingHeader.getTotalPaid())
                        .orElse(BigDecimal.ZERO);

                BigDecimal totalPaidUi = Optional.ofNullable(request.getAmount())
                        .orElse(BigDecimal.ZERO);

                billingHeader.setTotalPaid(totalPaidDB.add(totalPaidUi));
                billingHeader.setCreatedBy(currentUser.getFullName());

                if (fullyPaid) {
                    orderHd.setPaymentStatus(AppConstants.PAYMENT_PAID.toLowerCase());
                    billingHeader.setPaymentStatus(AppConstants.PAYMENT_PAID.toLowerCase());
                    visitOpt.ifPresent(v -> v.setBillingStatus(AppConstants.PAYMENT_PAID.toLowerCase()));
                    res.setPaymentStatus(AppConstants.PAYMENT_PAID.toLowerCase());
                } else {
                    orderHd.setPaymentStatus(AppConstants.PAYMENT_PARTIAL_PENDING.toLowerCase());
                    billingHeader.setPaymentStatus(AppConstants.PAYMENT_PARTIAL_PENDING.toLowerCase());
                    visitOpt.ifPresent(v -> v.setBillingStatus(AppConstants.PAYMENT_PARTIAL_PENDING.toLowerCase()));
                    res.setPaymentStatus(AppConstants.PAYMENT_PARTIAL_PENDING.toLowerCase());
                }

                radOrderHdRepository.save(orderHd);
                visitOpt.ifPresent(visitRepository::save);
                billingHeaderRepository.save(billingHeader);

                res.setBillNo(billingHeader.getBillNo());
            }
            res.setMsg("Success");
            log.info("Payment status update completed successfully");

        } catch (Exception e) {
            log.error("Unexpected error during payment status update", e);
            throw new BillingException("Payment failed: " + e.getMessage());
        }
        return ResponseUtils.createSuccessResponse(res, new TypeReference<PaymentResponse>() {
        });
    }


    // ✅ NEW METHOD: Map Order Detail to BillingDetailResponse
    private BillingDetailResponse mapOrderDetailToResponse(DgOrderDt orderDetail) {
        BillingDetailResponse response = new BillingDetailResponse();

        response.setId((long) orderDetail.getId());

        BigDecimal basePrice = BigDecimal.ZERO;
        BigDecimal tariff = BigDecimal.ZERO;

        // ✅ CASE 1: Investigation - Get price from InvestigationPriceDetails table
        if (orderDetail.getInvestigation() != null) {
            DgMasInvestigation investigation = orderDetail.getInvestigation();

            response.setInvestigationId(investigation.getInvestigationId());
            response.setInvestigationName(safe(investigation.getInvestigationName()));
            response.setItemName(safe(investigation.getInvestigationName()));

            // ✅ Get actual price from investigation_price_details table
            basePrice = getCurrentInvestigationPrice(investigation);
            tariff = basePrice;
        }

        // ✅ CASE 2: Package - Get price directly from Package entity
        if (orderDetail.getInvestigationPackage() != null) {
            DgInvestigationPackage package_obj = orderDetail.getInvestigationPackage();

            response.setPackageId(package_obj.getPackId());
            response.setPackageName(safe(package_obj.getPackName()));

            // If package exists but investigation doesn't, use package pricing
            if (orderDetail.getInvestigation() == null) {
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

            // First try to get active price for current date
            Optional<MasInvestigationPriceDetails> priceDetail = masInvestigationPriceDetailsRepository
                    .findActivePriceByInvestigationAndDate(investigation, HMISUtil.getCurrentLocalDate());

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

            String notPaid = AppConstants.PAYMENT_NOT_PAID.toLowerCase();
            String partialPaid = AppConstants.PAYMENT_PARTIAL_PENDING.toLowerCase();

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

        List<Long> billIds = new ArrayList<>();
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

        billIds.add(response.getBillinghdid());


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
            d.setAppointmentDate(r.getAppointmentDate());
            return d;

        }).collect(Collectors.toList());

        response.setBillingHeaderIds(billIds);
        response.setDetails(details);

        return response;
    }


    public BillingHeader saveBillingHeader(
            Object orderHd, Visit vId, User currentUser,
            BigDecimal sum, BigDecimal tax, BigDecimal disc,
            String serviceCategoryCode, boolean isRadiology) {

        BillingHeader billingHeader = new BillingHeader();
        billingHeader.setBillNo(transactionSequenceService.generateTransactionNumber(HMISTransaction.BILL_NO, currentUser.getHospital().getId()));

        billingHeader.setPatient(vId.getPatient());
        billingHeader.setVisit(vId);
        billingHeader.setPatientDisplayName(vId.getPatient().getFullName());

        LocalDate dob = vId.getPatient().getPatientDob();
        billingHeader.setPatientAge(ageCalculator(dob));
        billingHeader.setPatientGender(vId.getPatient().getPatientGender().getGenderName());
        billingHeader.setPatientAddress(vId.getPatient().getPatientAddress1());

        billingHeader.setHospital(currentUser.getHospital());
        billingHeader.setHospitalName(vId.getPatient().getPatientHospital().getHospitalName());
        billingHeader.setHospitalAddress(vId.getHospital().getAddress());
        billingHeader.setHospitalMobileNo(vId.getHospital().getContactNumber());
        billingHeader.setHospitalGstin(vId.getHospital().getGstnNo());

        billingHeader.setServiceCategory(
                masServiceCategoryRepository.findByServiceCateCode(serviceCategoryCode)
        );

        billingHeader.setReferredBy(vId.getDoctorName());
        billingHeader.setBillingDate(Instant.now());
        //Lab Billing Condition for Hospital
        if(vId.getHospital().getLabBilling().equalsIgnoreCase(AppConstants.STATUS_N)) {
            billingHeader.setPaymentStatus(AppConstants.PAYMENT_PAID.toLowerCase());
        } else {
            billingHeader.setPaymentStatus(AppConstants.PAYMENT_NOT_PAID.toLowerCase());
        }

        if (isRadiology) {
            billingHeader.setRadOrderHd((RadOrderHd) orderHd);
        } else {
            billingHeader.setHdorder((DgOrderHd) orderHd);
        }
        billingHeader.setTotalAmount(sum);
        billingHeader.setDiscountAmount(disc);
        billingHeader.setNetAmount(sum.subtract(disc).add(tax));
        billingHeader.setTaxTotal(tax);
        billingHeader.setCreatedBy(currentUser.getFullName());
        billingHeader.setCreatedDt(Instant.now());
        billingHeader.setUpdatedDt(Instant.now());
        billingHeader.setBillDate(OffsetDateTime.now());
        billingHeader.setUpdatedAt(OffsetDateTime.now());
        return billingHeaderRepository.save(billingHeader);
    }

    public BillingDetail saveBillingDetail(
            BillingHeader bhdId,
            Object dtId,
            BigDecimal actualAmount,
            BigDecimal discountedAmount,
            String serviceCategoryCode,
            boolean isRadiology) {

        BillingDetail billingDetail = new BillingDetail();
        MasServiceCategory sevcat =
                masServiceCategoryRepository.findByServiceCateCode(serviceCategoryCode);

        billingDetail.setBillingHd(bhdId);
        billingDetail.setBillHd(bhdId);
        billingDetail.setServiceCategory(sevcat);
        billingDetail.setServiceId(0L);
        billingDetail.setChargeCost(BigDecimal.ZERO);

        if (isRadiology) {
            RadOrderDt rad = (RadOrderDt) dtId;
            billingDetail.setItemName(rad.getInvestigation().getInvestigationName());
            billingDetail.setInvestigation(rad.getInvestigation());
            billingDetail.setPackageField(rad.getPackageId());
        } else {
            DgOrderDt dg = (DgOrderDt) dtId;
            billingDetail.setItemName(dg.getInvestigation().getInvestigationName());
            billingDetail.setInvestigation(dg.getInvestigation());
            billingDetail.setPackageField(dg.getInvestigationPackage());
        }

        billingDetail.setCreatedDt(OffsetDateTime.now());
        billingDetail.setUpdatedDt(OffsetDateTime.now());
        billingDetail.setCreatedAt(Instant.now());
        billingDetail.setQuantity(1);

        BigDecimal actual = actualAmount;
        BigDecimal discount = discountedAmount;

        billingDetail.setBasePrice(actual);
        billingDetail.setDiscount(discount);
        billingDetail.setTariff(actual);

        BigDecimal afterDiscount = actual.subtract(discount);
        billingDetail.setAmountAfterDiscount(afterDiscount);

        // Tax calculation
        BigDecimal tax = BigDecimal.ZERO;
        if (sevcat.getGstApplicable()) {
            tax = BigDecimal.valueOf(sevcat.getGstPercent())
                    .multiply(afterDiscount)
                    .divide(BigDecimal.valueOf(100));
        }

        billingDetail.setTaxAmount(tax);
        billingDetail.setTaxPercent(BigDecimal.valueOf(sevcat.getGstPercent()));

        BigDecimal net = afterDiscount.add(tax);
        billingDetail.setNetAmount(net);
        billingDetail.setTotal(net);

        if(bhdId.getPaymentStatus().equalsIgnoreCase(AppConstants.PAYMENT_PAID)) {
            billingDetail.setPaymentStatus(AppConstants.PAYMENT_PAID.toLowerCase());
        } else {
            billingDetail.setPaymentStatus(AppConstants.PAYMENT_NOT_PAID.toLowerCase());
        }

        return billingDetailRepository.save(billingDetail);
    }

    public BillingDetail saveBillingDetailPackage(
            BillingHeader bhdId,
            DgInvestigationPackage pack,
            LabRadioInvestigationRequest req,
            String serviceCategoryCode) {

        BillingDetail billingDetail = new BillingDetail();
        MasServiceCategory sevcat =
                masServiceCategoryRepository.findByServiceCateCode(serviceCategoryCode);

        billingDetail.setBillingHd(bhdId);
        billingDetail.setBillHd(bhdId);
        billingDetail.setServiceCategory(sevcat);

        billingDetail.setItemName(pack.getPackName());
        billingDetail.setPackageField(pack);
        billingDetail.setInvestigation(null); // always null for package

        billingDetail.setCreatedDt(OffsetDateTime.now());
        billingDetail.setUpdatedDt(OffsetDateTime.now());
        billingDetail.setCreatedAt(Instant.now());
        billingDetail.setQuantity(1);
        billingDetail.setServiceId(0L);
        billingDetail.setChargeCost(BigDecimal.ZERO);

        BigDecimal actual = req.getActualAmount();
        BigDecimal discount = req.getDiscountedAmount();

        billingDetail.setBasePrice(actual);
        billingDetail.setDiscount(discount);
        billingDetail.setTariff(actual);

        BigDecimal afterDiscount = actual.subtract(discount);
        billingDetail.setAmountAfterDiscount(afterDiscount);

        BigDecimal tax = BigDecimal.ZERO;
        if (sevcat.getGstApplicable()) {
            tax = BigDecimal.valueOf(sevcat.getGstPercent())
                    .multiply(afterDiscount)
                    .divide(BigDecimal.valueOf(100));
        }

        billingDetail.setTaxAmount(tax);
        billingDetail.setTaxPercent(BigDecimal.valueOf(sevcat.getGstPercent()));

        BigDecimal net = afterDiscount.add(tax);
        billingDetail.setNetAmount(net);
        billingDetail.setTotal(net);

        billingDetail.setPaymentStatus(AppConstants.PAYMENT_NOT_PAID.toLowerCase());

        return billingDetailRepository.save(billingDetail);
    }



    @Override
    @Transactional(readOnly = true)
    public ApiResponse<Page<PaidCancelledAppointmentResponse>> getBillingRefundPatientList(int page,int size,String patientName,
                String mobileNo,String billingServiceType,String refundStatus,LocalDate fromDate,LocalDate toDate
    ) {

        try {
            log.info(
                    "Fetching billing refund patient list: " +
                            "page={}, size={}, patientName={}, mobileNo={}, " +
                            "billingService={}, refundStatus={}, fromDate={}, toDate={}",
                    page,
                    size,
                    patientName,
                    mobileNo,
                    billingServiceType,
                    refundStatus,
                    fromDate,
                    toDate
            );

            helperUtils.validatePagination(page, size);
            helperUtils.validateDateRange(fromDate, toDate);
            patientName = helperUtils.cleanValue(patientName);
            mobileNo = helperUtils.cleanValue(mobileNo);
            billingServiceType = helperUtils.cleanValue(billingServiceType);
            refundStatus = helperUtils.normalizeRefundStatusFilter(refundStatus);
            Pageable pageable = PageRequest.of(page, size);
            Page<PaidCancelledAppointmentProjection> projectionPage =
                    visitRepository.getBillingRefundPatientList(
                            patientName,
                            mobileNo,
                            billingServiceType,
                            refundStatus,
                            AppConstants.STATUS_Y.toLowerCase(),
                            AppConstants.STATUS_N.toLowerCase(),
                            AppConstants.STATUS_N.toLowerCase(),
                            AppConstants.BILLING_REFUND_STATUS_COMPLETED_LABEL,
                            AppConstants.BILLING_REFUND_STATUS_PENDING_LABEL,
                            fromDate,
                            toDate,
                            pageable
                    );

            Page<PaidCancelledAppointmentResponse> responsePage =
                    projectionPage.map(paidCancelledAppointmentMapper::mapToResponse);

            log.info("Billing refund patient list fetched successfully. " + "Total records={}",
                    responsePage.getTotalElements());

            return ResponseUtils.createSuccessResponse(
                    responsePage,
                    new TypeReference<Page<PaidCancelledAppointmentResponse>>() {
                    },
                    "Billing refund patient list fetched successfully"
            );

        } catch (IllegalArgumentException exception) {
            log.warn("Invalid billing refund search request: {}", exception.getMessage());

            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<Page<PaidCancelledAppointmentResponse>>() {
                    },
                    exception.getMessage(),
                    HttpStatus.BAD_REQUEST.value()
            );
        } catch (Exception exception) {
            log.error("Error while fetching billing refund patient list",
                    exception);

            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<Page<PaidCancelledAppointmentResponse>>() {
                    },
                    "Unable to fetch billing refund patient list",
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }







    @Override
    public ApiResponse<List<PatientBillingRefundDetailsResponse>> getPatientBillingRefundDetails(Long billingId) {

        log.info("Fetching patient billing refund details for billingId: {}", billingId);
        try {
            if (billingId == null || billingId <= 0) {
                log.warn("Invalid billingId received: {}", billingId);
                return ResponseUtils.createFailureResponse(
                        null,
                        new TypeReference<>() {
                        },
                        "Valid billing ID is required",
                        HttpStatus.BAD_REQUEST.value()
                );
            }

            List<PatientBillingRefundDetailsProjection> refundDetails =
                    opdRefundDetailsRepository
                            .findRefundDetailsByBillingId(billingId);

            List<PatientBillingRefundDetailsResponse> response =
                    refundDetails.stream()
                            .map(this::mapToRefundResponse)
                            .toList();

            log.info(
                    "{} refund record(s) found for billingId: {}",
                    response.size(),
                    billingId
            );

            return ResponseUtils.createSuccessResponse(
                    response,
                    new TypeReference<>() {
                    }
            );

        } catch (Exception e) {

            log.error(
                    "Error while fetching refund details for billingId: {}",
                    billingId,
                    e
            );

            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {
                    },
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }


    private PatientBillingRefundDetailsResponse mapToRefundResponse(
            PatientBillingRefundDetailsProjection projection
    ) {

        return PatientBillingRefundDetailsResponse.builder()
                .refundStatus(projection.getRefundStatus())
                .refundAmount(projection.getRefundAmount())
                .refundMode(projection.getRefundMode())
                .transactionNumber(projection.getTransactionNumber())
                .refundDate(projection.getRefundDate())
                .processedBy(projection.getProcessedBy())
                .build();
    }

    @Override
    public BillingHeader saveBillingHeaderIfEnabled(
            boolean billingEnabled, Object orderHd, Visit visit, User currentUser,
            BigDecimal total, BigDecimal tax, BigDecimal discount,
            String serviceCategoryCode, boolean isRadiology) {

        if (!billingEnabled) {
            return null;
        }

        BillingHeader billing = saveBillingHeader(orderHd, visit, currentUser, total, tax, discount, serviceCategoryCode, isRadiology);

        if (billing == null) {
            throw new SDDException("billing", 500, "Failed to create billing");
        }

        Visit v = visitRepository.getReferenceById(visit.getId());
        v.setBillingHd(billing);
        visitRepository.save(v);

        return billing;
    }

    private LabOrderTrackingStatus getOrderedStatus(){
        return orderTrackingStatusRepository.findById(orderedStatusId).orElseThrow(()-> new RuntimeException("Order status not found")) ;
    }

}

