package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.request.PrescriptionDetailsApproveRequest;
import com.hims.request.PrescriptionHeaderApproveRequest;
import com.hims.request.StoreStockLedgerRequest;
import com.hims.request.UpdateStoreItemBatchStockRequest;
import com.hims.response.*;
import com.hims.service.DispensaryService;
import com.hims.service.TransactionSequenceService;
import com.hims.utils.AuthUtil;
import com.hims.utils.HMISTransaction;
import com.hims.utils.InventoryUtils;
import com.hims.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DispensaryServiceImpl implements DispensaryService {

    private final PatientPrescriptionHdRepository patientPrescriptionHdRepository;

    private final PatientPrescriptionDtRepository patientPrescriptionDtRepository;

    private final StoreItemBatchStockRepository storeItemBatchStockRepository;

    private  final MasHospitalRepository hospitalRepository;

    private final MasDepartmentRepository departmentRepository;

    private  final  StoreIssueMRepository storeIssueMRepository;

    private final MasStoreItemRepository storeItemRepository;

    private final BillingHeaderRepository billingHeaderRepository;

    private final BillingDetailRepository billingDetailRepository;

    private  final MasServiceCategoryRepository masServiceCategoryRepository;


    private final TransactionSequenceService transactionSequenceService;

    private final AuthUtil authUtil;
    private final InventoryUtils inventoryUtils;

    @Value("${hos.define.dispensaryId}")
    private Long dispensaryDepartmentId;

    @Value("${pharmacyServiceCode}")
    private String pharmacyServiceCode;

    @Value("${closingDaysForPrescription}")
    private  Integer closingDaysForPrescription;
    private final StoreIssueTRepository storeIssueTRepository;


    @Override
    public ApiResponse<Page<PatientPrescriptionHeaderResponse>> getPendingPrescriptionsHeaders(
            Long hospitalId,
            Long departmentId,
            String patientName,
            String patientMobileNo,
            int page,
            int size
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "prescriptionDate"));
            Page<PatientPrescriptionHeaderResponse> prescriptionHeaders = patientPrescriptionHdRepository
                    .findPendingPrescriptionsHeaders(
                            hospitalId,
                            departmentId,
                            patientName,
                            patientMobileNo,
                            AppConstants.STATUS_N.toLowerCase(),
                            pageable
                    );
            return ResponseUtils.createSuccessResponse(prescriptionHeaders,
                    new TypeReference<>() {}
            );
        } catch (Exception e) {
            log.error("Error retrieving pending prescriptions for method getPendingPrescriptionsHeaders : {}", e.getMessage(), e);
            return ResponseUtils.createFailureResponse(null,
                    new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    public ApiResponse<List<PatientPrescriptionDetailsResponse>> getPendingPrescriptionsDetailsWrtHeader(Long prescriptionHeaderId) {
        try {
            List<PatientPrescriptionDetailsResponse> prescriptionDetails = patientPrescriptionDtRepository
                    .findPendingPrescriptionsDetailsWrtHeader(prescriptionHeaderId, AppConstants.STATUS_N.toLowerCase());
            return ResponseUtils.createSuccessResponse(prescriptionDetails,
                    new TypeReference<>() {}
            );
        } catch (Exception e) {
            log.error("Error retrieving pending prescriptions for method getPendingPrescriptionsDetailsWrtHeader : {}", e.getMessage(), e);
            return ResponseUtils.createFailureResponse(null,
                    new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    @Transactional
    public ApiResponse<PrescriptionApproveHeaderResponse> approvePrescription(PrescriptionHeaderApproveRequest request) {

        try {

            log.info("Approving prescription started with header ID: {}",
                    request.getPrescriptionHeaderId());



            PatientPrescriptionHd header = patientPrescriptionHdRepository
                    .findById(request.getPrescriptionHeaderId())
                    .orElseThrow(() -> new RuntimeException(
                            "Prescription header not found with ID: "
                                    + request.getPrescriptionHeaderId()
                    ));

            header.setStatus(AppConstants.STATUS_Y.toLowerCase());


            patientPrescriptionHdRepository.save(header);

            MasServiceCategory serviceCategory =
                    masServiceCategoryRepository.findByServiceCateCode(pharmacyServiceCode);

            if (serviceCategory == null) {
                throw new RuntimeException(
                        "Pharmacy service category not found for service code: "
                                + pharmacyServiceCode
                );
            }


            BillingHeader billingHeader = createPrescriptionBilling(header, request.getPrescriptionDetails(), serviceCategory);

            MasHospital masHospital = hospitalRepository.findById(header.getHospitalId())
                    .orElseThrow(() -> new RuntimeException(
                            "Hospital not found with ID: "
                                    + header.getHospitalId()
                    ));

            MasDepartment dispensaryDept = departmentRepository.findById(dispensaryDepartmentId)
                    .orElseThrow(() -> new RuntimeException(
                            "Dispensary department not found with ID: "
                                    + dispensaryDepartmentId
                    ));

            StoreIssueM issueM = new StoreIssueM();
            issueM.setIssueNo(inventoryUtils.generateIssueNumber());
            issueM.setStatus(AppConstants.INDENT_ISSUED_AT_ISSUE_DEPT);

            issueM.setHospitalId(masHospital);
            issueM.setIssueDate(LocalDateTime.now());
            issueM.setIssuedDate(LocalDateTime.now());
            issueM.setToDeptId(dispensaryDept);
            issueM.setPrescriptionHdId(header.getPrescriptionHdId());
            issueM.setPatientId(header.getPatientId());
            issueM.setIssuedBy(authUtil.getCurrentUser().getUsername());

            StoreIssueM savedIssueM = storeIssueMRepository.save(issueM);

            billingHeader.setStoreIssueM(savedIssueM);
            billingHeaderRepository.save(billingHeader);


            boolean nisRequired = false;
            if (request.getPrescriptionDetails() != null
                    && !request.getPrescriptionDetails().isEmpty()) {

                for (PrescriptionDetailsApproveRequest detailRequest
                        : request.getPrescriptionDetails()) {

                    BigDecimal prescribedQty = detailRequest.getTotal();
                    BigDecimal issuedQty = detailRequest.getIssuedQty();

                    if (prescribedQty != null
                            && issuedQty != null
                            && issuedQty.compareTo(prescribedQty) < 0) {

                        nisRequired = true;
                    }


                    PatientPrescriptionDt detail;


                    Optional<StoreItemBatchStock> byId = storeItemBatchStockRepository.findById(detailRequest.getStockId());
                    if(byId.isEmpty()){
                        throw new RuntimeException("Stock not found with ID: " + detailRequest.getStockId());
                    }
                    StoreItemBatchStock storeItemBatchStock = byId.get();

                    // Existing detail -> update
                    if (detailRequest.getPrescriptionDetailsId() != null) {

                        detail = patientPrescriptionDtRepository
                                .findById(detailRequest.getPrescriptionDetailsId())
                                .orElseThrow(() -> new RuntimeException(
                                        "Prescription detail not found with ID: "
                                                + detailRequest.getPrescriptionDetailsId()
                                ));

                        // Make sure detail belongs to this header
                        if (!detail.getPrescriptionHeader()
                                .getPrescriptionHdId()
                                .equals(header.getPrescriptionHdId())) {

                            throw new RuntimeException(
                                    "Prescription detail does not belong to prescription header"
                            );
                        }
                        detail.setInstruction(detail.getInstruction());

                    } else {

                        detail = new PatientPrescriptionDt();

                        detail.setPrescriptionHeader(header);
                    }

                    // 4. Set/update detail values
                    detail.setPrescriptionHeader(header);
                    detail.setPrescriptionHdId(header.getPrescriptionHdId());
                    detail.setItemId(detailRequest.getItemId());
                    detail.setBatchNo(detailRequest.getBatchName());
                    detail.setDosage(detailRequest.getDosage());
                    detail.setFrequency(detailRequest.getFrequency());
                    detail.setDays(detailRequest.getDays());
                    detail.setTotal(detailRequest.getTotal());
                    detail.setIssuedQty(detailRequest.getIssuedQty());
                    detail.setInstruction(detailRequest.getInstruction());
                    detail.setStatus(AppConstants.STATUS_Y.toLowerCase());
                    detail.setExpiryDate(storeItemBatchStock.getExpiryDate());
                    detail.setUnitPrice(storeItemBatchStock.getMrpPerUnit());

                    PatientPrescriptionDt savedDetail = patientPrescriptionDtRepository.save(detail);



                    StoreIssueT issueT= new StoreIssueT();
                    issueT.setStoreIssueMId(savedIssueM);
                    issueT.setIssuedQty(detailRequest.getIssuedQty());
                    issueT.setPrescriptionDtId(savedDetail.getPrescriptionDtId());
                    issueT.setStockId(storeItemBatchStock);
                    issueT.setUnitPrice(storeItemBatchStock.getMrpPerUnit());
                    issueT.setBatchNo(storeItemBatchStock.getBatchNo());
                    issueT.setExpiryDate(storeItemBatchStock.getExpiryDate());
                    issueT.setStatus(AppConstants.INDENT_ISSUED_AT_ISSUE_DEPT);
                    issueT.setDom(storeItemBatchStock.getManufactureDate());
                    issueT.setBrandname(storeItemBatchStock.getBrandId().getBrandName());
                    issueT.setManufacturername(storeItemBatchStock.getManufacturerId().getManufacturerName());
                    storeItemRepository.findById(detailRequest.getItemId()).ifPresent(issueT::setItemId);

                    storeIssueTRepository.save(issueT);


                    UpdateStoreItemBatchStockRequest updateStockRequest = new UpdateStoreItemBatchStockRequest();
                    updateStockRequest.setStockId(detailRequest.getStockId());
                    updateStockRequest.setOpdIssueQty(detailRequest.getIssuedQty());

                    StockUpdateResponse stockUpdate =
                            inventoryUtils.updateStoreItemBatchStock(updateStockRequest);




                    StoreStockLedgerRequest ledgerRequest = new StoreStockLedgerRequest();
                    ledgerRequest.setStockId(detailRequest.getStockId());
                    ledgerRequest.setQtyOut( BigDecimal.valueOf(stockUpdate.getQtyOut()) );
                    ledgerRequest.setTxnType("OPD ISSUE");
                    ledgerRequest.setQtyBefore( BigDecimal.valueOf(stockUpdate.getQtyBefore()) );
                    ledgerRequest.setQtyAfter( BigDecimal.valueOf(stockUpdate.getQtyAfter()) );
                    ledgerRequest.setDepartmentId(dispensaryDepartmentId);
                    ledgerRequest.setRemarks( "ISSUE AGAINST PRESCRIPTION NO: " + header.getPrescriptionNumber() );
                    ledgerRequest.setHospitalId(header.getHospitalId());
                    ledgerRequest.setTxnSource("OPD ISSUE");
                    ledgerRequest.setTxnReferenceId( savedDetail.getPrescriptionDtId() );
                    ledgerRequest.setReferenceNo(issueM.getIssueNo());
                    ledgerRequest.setCreatedBy(authUtil.getCurrentUser().getUsername());

                    inventoryUtils.updateStoreStockLedger(ledgerRequest);


                }
            }
            if (nisRequired && header.getNisNo() == null) {

                String nisNumber = transactionSequenceService.generateTransactionNumber(
                        HMISTransaction.NIS_NO,
                        authUtil.getCurrentUser().getHospital().getId()
                );

                header.setNisNo(nisNumber);
                patientPrescriptionHdRepository.save(header);

                log.info(
                        "NIS generated for prescription header ID {}: {}",
                        header.getPrescriptionHdId(),
                        nisNumber
                );
            }

            log.info("Approving prescription ended with header ID: {}",
                    request.getPrescriptionHeaderId());

            return ResponseUtils.createSuccessResponse(
                    new PrescriptionApproveHeaderResponse(
                            header.getPrescriptionHdId(), header.getNisNo()
                    ),
                    new TypeReference<>() {}
            );

        } catch (Exception e) {

            log.error(
                    "Error approving prescription for method approvePrescription : {}",
                    e.getMessage(),
                    e
            );

            throw  new RuntimeException(
                    "Error approving prescription for method approvePrescription : "
                            + e.getMessage()
            );
        }
    }

    @Override
    public ApiResponse<String> closePendingPrescription(Long prescriptionHeaderId) {
        try {
            Optional<PatientPrescriptionHd> byId = patientPrescriptionHdRepository.findById(prescriptionHeaderId);
            if(byId.isEmpty()){
                return ResponseUtils.createNotFoundResponse("Invalid Prescription Header ID",
                        HttpStatus.NOT_FOUND.value()
                );
            }
            PatientPrescriptionHd patientPrescriptionHd = byId.get();
            LocalDateTime prescriptionDate = patientPrescriptionHd.getPrescriptionDate();
            LocalDateTime closingDate = prescriptionDate.plusDays(closingDaysForPrescription);

            if (LocalDateTime.now().isBefore(closingDate)) {
                return ResponseUtils.createFailureResponse(
                        null,
                        new TypeReference<>() {},
                        "Prescription can be closed only after "
                                + closingDaysForPrescription
                                + " days",
                        HttpStatus.BAD_REQUEST.value()
                );
            }
            patientPrescriptionHd.setStatus(AppConstants.PRESCRIPTION_STATUS_CLOSED.toLowerCase());
            patientPrescriptionHdRepository.save(patientPrescriptionHd);
            return ResponseUtils.createSuccessResponse("Prescription Closed Successfully",
                    new TypeReference<>() {}
            );
        } catch (Exception e) {
            log.error("Error retrieving pending prescriptions for method getPendingPrescriptionsHeaders : {}", e.getMessage(), e);
            return ResponseUtils.createFailureResponse(null,
                    new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    private BillingHeader createPrescriptionBilling(
            PatientPrescriptionHd prescriptionHeader,
            List<PrescriptionDetailsApproveRequest> prescriptionDetails,
            MasServiceCategory serviceCategory) {

        if (prescriptionDetails == null || prescriptionDetails.isEmpty()) {
            return null;
        }

        Visit visit = prescriptionHeader.getVisit();

        User currentUser = authUtil.getCurrentUser();

        /*
         * ==========================================================
         * CREATE ONE BILLING HEADER
         * ==========================================================
         */
        BillingHeader billingHeader = new BillingHeader();

        billingHeader.setBillDate(OffsetDateTime.now());

        billingHeader.setPatient(visit.getPatient());

        billingHeader.setPatientDisplayName(
                visit.getPatient().getPatientFn()
                        + " "
                        + visit.getPatient().getPatientMn()
                        + " "
                        + visit.getPatient().getPatientLn()
        );

        billingHeader.setPatientAge(
                visit.getPatient().getPatientAge()
        );

        billingHeader.setPatientGender(
                visit.getPatient().getPatientGender().getGenderName()
        );

        billingHeader.setPatientAddress(
                visit.getPatient().getPatientAddress1()
                        + " "
                        + visit.getPatient().getPatientAddress2()
        );

        billingHeader.setHospital(visit.getHospital());
        billingHeader.setHospitalName(visit.getHospital().getHospitalName());
        billingHeader.setHospitalAddress(visit.getHospital().getAddress());
        billingHeader.setHospitalMobileNo(visit.getHospital().getContactNumber());
        billingHeader.setHospitalGstin(visit.getHospital().getGstnNo());

        billingHeader.setVisit(visit);

        billingHeader.setCreatedBy(currentUser.getFullName());
        billingHeader.setCreatedDt(Instant.now());
        billingHeader.setUpdatedDt(Instant.now());
        billingHeader.setUpdatedAt(OffsetDateTime.now());
        billingHeader.setBillingDate(Instant.now());
        billingHeader.setServiceCategory(serviceCategory);
        billingHeader.setPrescriptionHeader(prescriptionHeader);

        billingHeader.setInvoiceNo("");

        billingHeader.setBillNo(
                transactionSequenceService.generateTransactionNumber(
                        HMISTransaction.BILL_NO,
                        currentUser.getHospital().getId()
                )
        );

        billingHeader.setGstnBillNo("");

        billingHeader.setTotalAmount(BigDecimal.ZERO);
        billingHeader.setNetAmount(BigDecimal.ZERO);
        billingHeader.setTaxTotal(BigDecimal.ZERO);
        billingHeader.setTotalPaid(BigDecimal.ZERO);
        billingHeader.setDiscountAmount(BigDecimal.ZERO);

        billingHeader.setPaymentStatus(
                AppConstants.PAYMENT_NOT_PAID.toLowerCase()
        );

        BillingHeader savedBillingHeader =
                billingHeaderRepository.save(billingHeader);


        /*
         * ==========================================================
         * CREATE BILLING DETAILS
         * ==========================================================
         */

        BigDecimal grandTotal = BigDecimal.ZERO;
        BigDecimal grandTaxAmount = BigDecimal.ZERO;
        BigDecimal grandNetAmount = BigDecimal.ZERO;

        for (PrescriptionDetailsApproveRequest detailRequest
                : prescriptionDetails) {

            StoreItemBatchStock stock =
                    storeItemBatchStockRepository
                            .findById(detailRequest.getStockId())
                            .orElseThrow(() -> new RuntimeException(
                                    "Stock not found with ID: "
                                            + detailRequest.getStockId()
                            ));

            MasStoreItem item =
                    storeItemRepository
                            .findById(detailRequest.getItemId())
                            .orElseThrow(() -> new RuntimeException(
                                    "Item not found with ID: "
                                            + detailRequest.getItemId()
                            ));

            /*
             * ======================================================
             * PRICE CALCULATION
             *
             * MRP PER UNIT IS GST EXCLUSIVE
             * ======================================================
             */

            BigDecimal unitPrice = stock.getMrpPerUnit();

            BigDecimal issuedQty = BigDecimal.valueOf(
                    detailRequest.getIssuedQty().longValue()
            );

            BigDecimal gstPercent = stock.getGstPercent() != null
                    ? stock.getGstPercent()
                    : BigDecimal.ZERO;

            // MRP is GST exclusive
            BigDecimal taxableAmount = unitPrice
                    .multiply(issuedQty)
                    .setScale(2, RoundingMode.HALF_UP);

            // Calculate GST on taxable amount
            BigDecimal taxAmount = taxableAmount
                    .multiply(gstPercent)
                    .divide(
                            BigDecimal.valueOf(100),
                            2,
                            RoundingMode.HALF_UP
                    );

            // GST-inclusive total amount
            BigDecimal totalAmount = taxableAmount
                    .add(taxAmount)
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal registrationCost = BigDecimal.ZERO;

            if(serviceCategory.getRegistrationCost()!=null){
                registrationCost=serviceCategory.getRegistrationCost();
            }

            // Net amount = GST-inclusive total + registration cost
            BigDecimal netAmount = totalAmount
                    .add(registrationCost)
                    .setScale(2, RoundingMode.HALF_UP);


            /*
             * ======================================================
             * CREATE BILLING DETAIL
             * ======================================================
             */

            BillingDetail billingDetail = new BillingDetail();

            billingDetail.setBillingHd(savedBillingHeader);
            billingDetail.setBillHd(savedBillingHeader);
            billingDetail.setServiceId(detailRequest.getItemId());
            billingDetail.setItemName(item.getNomenclature());
            billingDetail.setServiceCategory(serviceCategory);

            billingDetail.setQuantity(issuedQty.intValue());

            // GST-exclusive amount
            billingDetail.setBasePrice(taxableAmount);
            billingDetail.setTariff(taxableAmount);

            billingDetail.setDiscount(BigDecimal.ZERO);
            billingDetail.setAmountAfterDiscount(totalAmount);

            billingDetail.setTaxPercent(gstPercent);
            billingDetail.setTaxAmount(taxAmount);

            billingDetail.setRegistrationCost(registrationCost);
            billingDetail.setNetAmount(netAmount);

            // Taxable + GST
            billingDetail.setNetAmount(netAmount);

            billingDetail.setCreatedAt(Instant.now());
            billingDetail.setCreatedDt(OffsetDateTime.now());
            billingDetail.setUpdatedDt(OffsetDateTime.now());
            billingDetail.setRegistrationCost(BigDecimal.ZERO);
            billingDetail.setChargeCost(BigDecimal.ZERO);

            billingDetail.setPaymentStatus(
                    AppConstants.PAYMENT_NOT_PAID.toLowerCase()
            );
            billingDetail.setRegistrationCost(registrationCost);
            billingDetail.setItem(item);
            billingDetail.setCollectedBy(authUtil.getCurrentUser());


            billingDetailRepository.save(billingDetail);


            /*
             * ======================================================
             * ADD DETAIL AMOUNTS TO HEADER TOTALS
             * ======================================================
             */

            grandTotal = grandTotal.add(totalAmount);
            grandTaxAmount = grandTaxAmount.add(taxAmount);
            grandNetAmount = grandNetAmount.add(netAmount);
        }


        /*
         * ==========================================================
         * UPDATE BILLING HEADER
         * ==========================================================
         */

        savedBillingHeader.setTotalAmount(grandTotal);
        savedBillingHeader.setTaxTotal(grandTaxAmount);
        savedBillingHeader.setNetAmount(grandNetAmount);
        savedBillingHeader.setDiscountAmount(BigDecimal.ZERO);
        savedBillingHeader.setTotalPaid(BigDecimal.ZERO);

       return billingHeaderRepository.save(savedBillingHeader);
    }



}
