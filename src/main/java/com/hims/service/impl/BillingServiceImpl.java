package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.exception.BillingException;
import com.hims.response.*;
import com.hims.service.BillingService;
import com.hims.utils.AuthUtil;
import com.hims.utils.RandomNumGenerator;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
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
    @Autowired
    PatientRepository patientRepository;
    @Autowired
    private RandomNumGenerator randomNumGenerator;
    @Autowired
    private MasHospitalRepository masHospitalRepository;

    @Autowired

    private LabHdRepository labHdRepository;

    @Autowired
    private LabDtRepository labDtRepository;
    @Autowired
    private MasInvestigationPriceDetailsRepository masInvestigationPriceDetailsRepository;

    @Override
    @Transactional
    public ApiResponse<OpdBillingPaymentResponse> saveBillingForOpd(Visit visit, MasServiceCategory serviceCategory, MasDiscount discount) {
        BillingHeader header = new BillingHeader();
        String orderNum = createInvoices();
        OpdBillingPaymentResponse response = new OpdBillingPaymentResponse();
        User currentUser = authUtil.getCurrentUser();
        BigDecimal tax=BigDecimal.ZERO;
        BigDecimal registrationCost = BigDecimal.ZERO;
        try {
            //Check the registration cost if available else 0
            if(visit.getVisitType().equalsIgnoreCase("N")){
                registrationCost = visit.getHospital().getRegistrationCost() != null ? visit.getHospital().getRegistrationCost() : BigDecimal.ZERO;
            }

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

                BigDecimal total = serviceOpd.get().getBaseTariff().subtract(totalDiscount).add(registrationCost);
                tax = tax.add(BigDecimal.valueOf(serviceCategory.getGstPercent()).multiply(total).divide(BigDecimal.valueOf(100)));

                header.setNetAmount(total.add(tax));
                header.setTaxTotal(tax);
                header.setTotalAmount(total.add(tax));
                header.setTotalPaid(BigDecimal.valueOf(0));
            }else if (serviceOpd.isEmpty()) {
                throw new BillingException("MasServiceOPD or Tariff is not defined yet");
            }
            header.setDiscountAmount(totalDiscount);
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
            if(visit.getVisitType().equalsIgnoreCase("N")){
                header.setRegistrationCost(registrationCost);
            }else{
                header.setRegistrationCost(BigDecimal.ZERO);
            }

            BillingHeader savedHeader = billingHeaderRepository.save(header);
            response.setHeader(savedHeader);
            if (savedHeader != null) {
                BillingDetail detail = new BillingDetail();
                detail.setBillingHd(savedHeader);
                detail.setServiceCategory(serviceCategory);
                detail.setServiceId(0L);
                detail.setItemName("");
                detail.setPaymentStatus("n");

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
                    detail.setTaxPercent(BigDecimal.valueOf(serviceCategory.getGstPercent()));
                    detail.setTaxAmount(tax);
                    detail.setNetAmount(total.add(registrationCost).add(tax));
                    detail.setCreatedAt(Instant.now());
                    //detail.setTotal(total.add(header.getRegistrationCost()));
                    detail.setRegistrationCost(header.getRegistrationCost());
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

    public String createInvoices() {
        return randomNumGenerator.generateOrderNumber("BILL",true,true);
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
        try {
            // 1. fetch all billing headers with payment status n or p
            List<BillingHeader> billingHeaders = billingHeaderRepository.findByPaymentStatusIn(List.of("n", "p"));

            // 2. group consultation service headers by patientId + visitDate (yyyy-MM-dd)
            Map<String, List<BillingHeader>> groupedConsultation = billingHeaders.stream()
                    .filter(h -> h.getServiceCategory() != null
                            && "Consultation Services".equalsIgnoreCase(h.getServiceCategory().getServiceCatName()))
                    .filter(h -> h.getVisit() != null && h.getVisit().getPatient() != null && h.getVisit().getVisitDate() != null)
                    .collect(Collectors.groupingBy(h -> {
                        Visit v = h.getVisit();
                        Long pid = v.getPatient().getId();
                        String date = v.getVisitDate().toString().substring(0, 10); // yyyy-MM-dd
                        return pid + "_" + date;
                    }));

            // 3. Map grouped consultations to single responses
            List<PendingBillingResponse> consultationResponses = groupedConsultation.values().stream()
                    .map(this::mapGroupedConsultation)
                    .collect(Collectors.toList());

            // 4. Collect header ids used in consultation groups so we don't duplicate them
            Set<Long> groupedHeaderIds = groupedConsultation.values().stream()
                    .flatMap(List::stream)
                    .map(BillingHeader::getId)
                    .collect(Collectors.toSet());

            // 5. Remaining billing headers (non-consultation or consultation headers that didn't group)
            List<PendingBillingResponse> remainingBillingResponses = billingHeaders.stream()
                    .filter(h -> !groupedHeaderIds.contains(h.getId()))
                    .map(this::mapToResponse) // uses your existing single-header mapping
                    .collect(Collectors.toList());

            // 6. Order (OPD LAB) responses (unchanged)
            List<DgOrderHd> orderHeaders = labHdRepository.findByPaymentStatusInAndSource(List.of("n", "p"), "OPD PATIENT");
            List<PendingBillingResponse> orderResponses = orderHeaders.stream()
                    .map(this::mapOrderToResponse)
                    .collect(Collectors.toList());

            // 7. Build final list: grouped consultations first, then remaining billing rows, then orders
            List<PendingBillingResponse> finalList = new ArrayList<>();
            finalList.addAll(consultationResponses);
            finalList.addAll(remainingBillingResponses);
            finalList.addAll(orderResponses);

            return ResponseUtils.createSuccessResponse(finalList, new TypeReference<List<PendingBillingResponse>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtils.createFailureResponse(new ArrayList<>(),
                    new TypeReference<List<PendingBillingResponse>>() {},
                    "Error fetching pending billing data", 500);
        }
    }

    private PendingBillingResponse mapGroupedConsultation(List<BillingHeader> headers) {
        BillingHeader first = headers.get(0);
        Visit visit = first.getVisit();
        Patient p = (visit != null) ? visit.getPatient() : null;

        PendingBillingResponse response = new PendingBillingResponse();

        response.setBillinghdid(first.getId());
        response.setPatientid(p != null ? p.getId() : null);
        response.setPatientName(safe(first.getPatientDisplayName()));
        response.setMobileNo(p != null ? safe(p.getPatientMobileNumber()) : "");
        response.setAge(p != null && p.getPatientDob() != null ? ageCalculator(p.getPatientDob()) : "");
        response.setSex(safe(first.getPatientGender()));
        response.setRelation(p != null && p.getPatientRelation() != null ? safe(p.getPatientRelation().getRelationName()) : "");
        response.setAddress(safe(first.getPatientAddress()));
        response.setBillingType(safe(first.getServiceCategory() != null ? first.getServiceCategory().getServiceCatName() : ""));
        response.setBillingStatus(safe(first.getPaymentStatus()));
        response.setFlag("Direct");
        response.setSource(null); // keep same as before



        // appointments: collect Visit info from each header's visit and include originating billinghdid
        List<AppointmentBlock> appointments = headers.stream()
                .map(h -> {
                    Visit v = h.getVisit();
                    AppointmentBlock ab = new AppointmentBlock();
                    ab.setBillingHdId(h.getId()); // IMPORTANT: keep the billing header id per appointment
                    if (v != null) {
                        ab.setVisitId(v.getId());
                        ab.setVisitType(v.getVisitType());
                        ab.setTokenNo(v.getTokenNo());
                        ab.setDepartment(v.getDepartment() != null ? safe(v.getDepartment().getDepartmentName()) : null);
                        ab.setConsultedDoctor(v.getDoctor() != null ? safe(v.getDoctor().getFullName()) : null);
                        ab.setSessionName(v.getSession() != null ? safe(v.getSession().getSessionName()) : null);
                        ab.setVisitDate(v.getVisitDate());
                    }
                    return ab;
                })
                .collect(Collectors.toList());
        response.setAppointments(appointments);

        // details: aggregate billing details across all headers in the group
        List<BillingDetailResponse> allDetails = headers.stream()
                .flatMap(h ->
                        billingDetailRepository
                                .findByBillHdIdAndPaymentStatusIn(h.getId(), List.of("n", "p"))
                                .stream()
                                .map(detail -> mapToDetailResponse(detail, h.getRegistrationCost()))
                )
                .collect(Collectors.toList());

        response.setDetails(allDetails);

        // amount: sum of net amounts of headers (avoid nulls)
        BigDecimal total = headers.stream()
                .map(BillingHeader::getNetAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        response.setAmount(total);

        return response;
    }
    // Your existing mapToResponse method (for BillingHeader)
    private PendingBillingResponse mapToResponse(BillingHeader header) {
        PendingBillingResponse response = new PendingBillingResponse();
        response.setBillinghdid(header.getId());

        // ✅ Name from BillingHeader
        response.setPatientName(safe(header.getPatientDisplayName()));
        response.setAddress(header.getPatientAddress());

        if (header.getVisit() != null && header.getVisit().getPatient() != null) {
            response.setPatientid(header.getVisit().getPatient().getId());
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

        response.setFlag("Direct");

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

        // Set order fields as null for billing records
        response.setOrderhdid(null);
        response.setOrderhdPaymentStatus(null);

        List<BillingDetail> detailsList = billingDetailRepository.findByBillHdIdAndPaymentStatusIn(
                header.getId(), List.of("n", "p")
        );

        List<BillingDetailResponse> details = detailsList.stream()
                .map(detail -> mapToDetailResponse(detail, header.getRegistrationCost()))
                .collect(Collectors.toList());
        response.setDetails(details);

        return response;
    }

    // ✅ NEW METHOD: Map OrderHd to PendingBillingResponse
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
            response.setSex(safe(orderHd.getPatientId().getPatientGender() != null ?
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
    private BillingDetailResponse mapToDetailResponse(BillingDetail detail, BigDecimal regCost) {
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
        d.setTotal(detail.getTotal());
        d.setRegistrationCost(regCost != null ? regCost : BigDecimal.ZERO);

        // Include Investigation
        if (detail.getInvestigation() != null) {
            d.setInvestigationId(detail.getInvestigation().getInvestigationId());
            d.setInvestigationName(detail.getInvestigation().getInvestigationName());
        }

        // Include Package
        if (detail.getPackageField() != null) {
            d.setPackageId(detail.getPackageField().getPackId());
            d.setPackageName(detail.getPackageField().getPackName());
        }

        return d;
    }

    private String safe(String value) {
        return value != null ? value : "";
    }
}

