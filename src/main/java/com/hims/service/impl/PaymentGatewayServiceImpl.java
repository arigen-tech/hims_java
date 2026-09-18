package com.hims.service.impl;

import com.hims.constants.PaymentStatusCode;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.exception.SDDException;
import com.hims.request.BillingItemRequest;
import com.hims.request.OrderRequest;
import com.hims.request.RefundRequest;
import com.hims.response.PaymentGatewayStatusResponse;
import com.hims.response.UserContext;
import com.hims.service.PaymentGatewayService;
import com.hims.service.TransactionSequenceService;
import com.hims.service.UserContextService;
import com.hims.utils.AuthUtil;
import com.hims.utils.HMISTransaction;
import com.hims.utils.HMISUtil;
import com.hims.utils.PaymentUtils;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Refund;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentGatewayServiceImpl implements PaymentGatewayService {

    private final RazorpayClient razorpayClient;
    private final PaymentDetailsV2Repository paymentRepository;
    private final PaymentRefundRepository refundRepository;
    private final PaymentUtils paymentUtils;

    private final BillingHeaderRepository billingHeaderRepository;

    private final MasPaymentStatusRepository masPaymentStatusRepository;

    private final TransactionSequenceService transactionSequenceService;

    private  final MasAppointmentChangeReasonRepository masAppointmentChangeReasonRepository;


    private final AuthUtil authUtil;

    @Autowired
    private UserContextService userContextService;

    /**
     * Creates a Razorpay order and a PENDING row in payment_details_v2.
     * payment_capture=1 => auto-capture, matching your "pay and done" counter-billing decision.
     */
//    @Transactional
//    @Override
//    public Map<String, Object> createPaymentOrder(OrderRequest request) throws RazorpayException {
//        long amountInSubUnit = request.getAmount() * 100L;
//
//        JSONObject options = new JSONObject();
//        options.put("amount", amountInSubUnit);
//        options.put("currency", "INR");
//        options.put("receipt", "hmis_rcpt_" + System.currentTimeMillis());
//        options.put("payment_capture", 1); // auto-capture for pay-and-done billing
//
//        Order order = razorpayClient.orders.create(options);
//        String orderId = order.get("id");
//        BillingHeader billingHeader =
//                billingHeaderRepository.findById(request.getBillingHdId().intValue())
//                        .orElseThrow(
//                                ()-> new SDDException(
//                                        "Billing Header",
//                                        HttpStatus.NOT_FOUND.value(),
//                                        "Billing Header not found"
//                                )
//                        );
//
//
//        PaymentDetailsV2 payment = new PaymentDetailsV2();
//        payment.setBillingHeader(billingHeader);
//        payment.setPaymentStatusId(paymentUtils.getPaymentStatus(PaymentStatusCode.PENDING).getId());
//        payment.setPaymentReferenceNo(paymentUtils.generatePaymentReferenceNo());
//        payment.setAmount(BigDecimal.valueOf(request.getAmount()));
//        payment.setCurrency("INR");
//        payment.setPaymentGateway("RAZORPAY");
//        payment.setGatewayOrderId(orderId);
//        payment.setGatewayPaymentStatus("created");
//        payment.setCreatedBy(authUtil.getCurrentUserFullName());
//        paymentRepository.save(payment);
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("paymentId", payment.getPaymentId());
//        response.put("orderId", orderId);
//        response.put("amount", order.get("amount"));
//        response.put("currency", order.get("currency"));
//        response.put("paymentReferenceNo", payment.getPaymentReferenceNo());
//        return response;
//    }

//    @Transactional
//    @Override
//    public Map<String, Object> createPaymentOrder(OrderRequest request) throws RazorpayException {
//
//        BillingHeader billingHeader =
//                billingHeaderRepository.findById(request.getBillingHdId().intValue())
//                        .orElseThrow(
//                                () -> new SDDException(
//                                        "Billing Header",
//                                        HttpStatus.NOT_FOUND.value(),
//                                        "Billing Header not found"
//                                )
//                        );
//
//        Optional<PaymentDetailsV2> existingOpt =
//                paymentRepository.findByBillingHeader_Id(request.getBillingHdId());
//
//        if (existingOpt.isPresent()) {
//            return handleExistingPayment(existingOpt.get(), request);
//        }
//
//        return createNewOrderAndPaymentRow(billingHeader, request);
//    }
//
//    private Map<String, Object> handleExistingPayment(
//            PaymentDetailsV2 existing, OrderRequest request) throws RazorpayException {
//
//        String statusCode = paymentUtils.getStatusCodeById(existing.getPaymentStatusId());
//
//        switch (statusCode) {
//
//
//            case "PAID":
//            case "REFUND_PENDING":
//            case "PARTIALLY_REFUNDED":
//            case "REFUNDED":
//                // Money has moved (fully or partially) or is in flight -
//                // never allow a brand new order against this bill.
//                throw new SDDException(
//                        "Payment already exists",
//                        HttpStatus.CONFLICT.value(),
//                        "This bill already has a payment in progress or completed. Current status: "
//                                + statusCode
//                );
//
//            case "PENDING": {
//                // Order was already created — user likely backed out of the
//                // Razorpay popup without paying. Reuse the SAME order/row,
//                // do not call Razorpay again and do not insert a new row.
//                log.info(
//                        "Reusing existing PENDING order for billingHdId={}. paymentId={}, gatewayOrderId={}",
//                        request.getBillingHdId(),
//                        existing.getPaymentId(),
//                        existing.getGatewayOrderId()
//                );
//
//                Map<String, Object> reuseResponse = new HashMap<>();
//                reuseResponse.put("paymentId", existing.getPaymentId());
//                reuseResponse.put("orderId", existing.getGatewayOrderId());
//                reuseResponse.put(
//                        "amount",
//                        existing.getAmount().multiply(BigDecimal.valueOf(100)).longValueExact()
//                );
//                reuseResponse.put("currency", existing.getCurrency());
//                reuseResponse.put("paymentReferenceNo", existing.getPaymentReferenceNo());
//                return reuseResponse;
//            }
//
//            case "FAILED":
//            case "CANCELLED":
//                // Genuinely dead attempt — allow a fresh Razorpay order,
//                // but update the SAME row rather than inserting a new one.
//                return retryFailedPayment(existing, request);
//
//            default:
//                throw new SDDException(
//                        "Invalid payment state",
//                        HttpStatus.CONFLICT.value(),
//                        "Unexpected payment status for billing header: " + statusCode
//                );
//        }
//    }
//
//    private Map<String, Object> retryFailedPayment(
//            PaymentDetailsV2 existing, OrderRequest request) throws RazorpayException {
//
//        long amountInSubUnit = request.getAmount() * 100L;
//
//        JSONObject options = new JSONObject();
//        options.put("amount", amountInSubUnit);
//        options.put("currency", "INR");
//        options.put("receipt", "hmis_rcpt_" + System.currentTimeMillis());
//        options.put("payment_capture", 1);
//
//        Order order = razorpayClient.orders.create(options);
//        String newOrderId = order.get("id");
//
//        log.info(
//                "Retrying previously {} payment. paymentId={}, oldOrderId={}, newOrderId={}",
//                paymentUtils.getStatusCodeById(existing.getPaymentStatusId()),
//                existing.getPaymentId(),
//                existing.getGatewayOrderId(),
//                newOrderId
//        );
//
//        existing.setGatewayOrderId(newOrderId);
//        existing.setGatewayPaymentId(null);
//        existing.setGatewayPaymentStatus("created");
//        existing.setPaymentStatusId(paymentUtils.getPaymentStatus(PaymentStatusCode.PENDING).getId());
//        existing.setAmount(BigDecimal.valueOf(request.getAmount()));
//        paymentRepository.save(existing);
//
//        return buildOrderResponse(existing, order);
//    }

    @Transactional
    @Override
    public Map<String, Object> createPaymentOrder(OrderRequest request) throws RazorpayException {

        List<BillingItemRequest> items = request.getBillingItems();

        // ------------------------------------------------------------
        // VALIDATE: every billing header must exist, and none of them
        // may already have a payment in progress / completed.
        // ------------------------------------------------------------

        List<BillingHeader> billingHeaders = new ArrayList<>();
        List<PaymentDetailsV2> reusableExisting = new ArrayList<>();

        for (BillingItemRequest item : items) {

            BillingHeader billingHeader =
                    billingHeaderRepository.findById(item.getBillingHdId().intValue())
                            .orElseThrow(() -> new SDDException(
                                    "Billing Header",
                                    HttpStatus.NOT_FOUND.value(),
                                    "Billing Header not found: " + item.getBillingHdId()
                            ));
            billingHeaders.add(billingHeader);

            Optional<PaymentDetailsV2> existingOpt =
                    paymentRepository.findByBillingHeader_Id(item.getBillingHdId());

            if (existingOpt.isPresent()) {

                String statusCode =
                        paymentUtils.getStatusCodeById(existingOpt.get().getPaymentStatusId());

                switch (statusCode) {

                    case "PAID":
                    case "REFUND_PENDING":
                    case "PARTIALLY_REFUNDED":
                    case "REFUNDED":
                        throw new SDDException(
                                "Payment already exists",
                                HttpStatus.CONFLICT.value(),
                                "Billing header " + item.getBillingHdId()
                                        + " already has a payment in progress or completed. Current status: "
                                        + statusCode
                        );

                    case "PENDING":
                    case "FAILED":
                    case "CANCELLED":
                        // Fine to reuse/retry — collect for the reuse-or-retry
                        // branch below instead of inserting a fresh row.
                        reusableExisting.add(existingOpt.get());
                        break;

                    default:
                        throw new SDDException(
                                "Invalid payment state",
                                HttpStatus.CONFLICT.value(),
                                "Unexpected payment status for billing header "
                                        + item.getBillingHdId() + ": " + statusCode
                        );
                }
            }
        }

        // ------------------------------------------------------------
        // If EVERY item in this group already has a PENDING row sharing
        // the same gateway_order_id, this is a pure retry — reuse the
        // existing order instead of creating a new one in Razorpay.
        // ------------------------------------------------------------

        if (reusableExisting.size() == items.size()) {

            String sharedOrderId = reusableExisting.get(0).getGatewayOrderId();

            boolean allPending = reusableExisting.stream()
                    .allMatch(p ->
                            "PENDING".equals(paymentUtils.getStatusCodeById(p.getPaymentStatusId()))
                                    && sharedOrderId != null
                                    && sharedOrderId.equals(p.getGatewayOrderId())
                    );

            if (allPending) {
                log.info(
                        "Reusing existing PENDING multi-item order. gatewayOrderId={}, billingHdIds={}",
                        sharedOrderId,
                        items.stream().map(BillingItemRequest::getBillingHdId).toList()
                );
                return buildMultiOrderResponse(reusableExisting, sharedOrderId);
            }
        }

        // ------------------------------------------------------------
        // Otherwise: create ONE new Razorpay order for the total, and
        // either insert new rows or update the FAILED/CANCELLED ones
        // in place, all pointing at the same new gatewayOrderId.
        // ------------------------------------------------------------

        BigDecimal totalAmount = items.stream()
                .map(BillingItemRequest::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalAmountInPaise = paymentUtils.getAmountInSubUnitINR(totalAmount);

        JSONObject options = new JSONObject();
        options.put("amount", totalAmountInPaise);
        options.put("currency", "INR");
        options.put("receipt", "hmis_rcpt_" + System.currentTimeMillis());
        options.put("payment_capture", 1);

        Order order = razorpayClient.orders.create(options);
        String orderId = order.get("id");

        List<PaymentDetailsV2> savedRows = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) {

            BillingItemRequest item = items.get(i);
            BillingHeader billingHeader = billingHeaders.get(i);

            PaymentDetailsV2 existingRow = reusableExisting.stream()
                    .filter(p -> p.getBillingHeader().getId().equals(billingHeader.getId()))
                    .findFirst()
                    .orElse(null);

            PaymentDetailsV2 payment = existingRow != null ? existingRow : new PaymentDetailsV2();

            payment.setBillingHeader(billingHeader);
            payment.setPaymentStatusId(paymentUtils.getPaymentStatus(PaymentStatusCode.PENDING).getId());
            payment.setAmount(item.getAmount());
            payment.setCurrency("INR");
            payment.setPaymentGateway("RAZORPAY");
            payment.setGatewayOrderId(orderId);
            payment.setGatewayPaymentId(null);
            payment.setGatewayPaymentStatus("created");

            if (existingRow == null) {
                payment.setPaymentReferenceNo(paymentUtils.generatePaymentReferenceNo());
                payment.setCreatedBy(userContextService.getCurrentUserContext().getUserFullName());
            }

            paymentRepository.save(payment);
            savedRows.add(payment);
        }

        log.info(
                "Created new multi-item Razorpay order. gatewayOrderId={}, billingHdIds={}, total={}",
                orderId,
                items.stream().map(BillingItemRequest::getBillingHdId).toList(),
                totalAmount
        );

        return buildMultiOrderResponse(savedRows, orderId);
    }

    private Map<String, Object> buildMultiOrderResponse(
            List<PaymentDetailsV2> rows, String orderId) {

        BigDecimal totalAmount = rows.stream()
                .map(PaymentDetailsV2::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> response = new HashMap<>();
        response.put("orderId", orderId);
        response.put("amount", paymentUtils.getAmountInSubUnitINR(totalAmount));
        response.put("currency", "INR");
        response.put(
                "paymentIds",
                rows.stream().map(PaymentDetailsV2::getPaymentId).toList()
        );
        response.put(
                "billingHdIds",
                rows.stream().map(p -> p.getBillingHeader().getId()).toList()
        );
        return response;
    }

//    private Map<String, Object> buildOrderResponse(PaymentDetailsV2 payment, Order order) {
//        Map<String, Object> response = new HashMap<>();
//        response.put("paymentId", payment.getPaymentId());
//        response.put("orderId", order.get("id"));
//        response.put("amount", order.get("amount"));
//        response.put("currency", order.get("currency"));
//        response.put("paymentReferenceNo", payment.getPaymentReferenceNo());
//        return response;
//    }

//    private Map<String, Object> createNewOrderAndPaymentRow(
//            BillingHeader billingHeader, OrderRequest request) throws RazorpayException {
//
//        long amountInSubUnit = request.getAmount() * 100L;
//
//        JSONObject options = new JSONObject();
//        options.put("amount", amountInSubUnit);
//        options.put("currency", "INR");
//        options.put("receipt", "hmis_rcpt_" + System.currentTimeMillis());
//        options.put("payment_capture", 1); // auto-capture for pay-and-done billing
//
//        Order order = razorpayClient.orders.create(options);
//        String orderId = order.get("id");
//
//        PaymentDetailsV2 payment = new PaymentDetailsV2();
//        payment.setBillingHeader(billingHeader);
//        payment.setPaymentStatusId(paymentUtils.getPaymentStatus(PaymentStatusCode.PENDING).getId());
//        payment.setPaymentReferenceNo(paymentUtils.generatePaymentReferenceNo());
//        payment.setAmount(BigDecimal.valueOf(request.getAmount()));
//        payment.setCurrency("INR");
//        payment.setPaymentGateway("RAZORPAY");
//        payment.setGatewayOrderId(orderId);
//        payment.setGatewayPaymentStatus("created");
//        payment.setCreatedBy(authUtil.getCurrentUserFullName());
//        paymentRepository.save(payment);
//
//        log.info(
//                "Created new Razorpay order. billingHdId={}, paymentId={}, orderId={}",
//                billingHeader.getId(), payment.getPaymentId(), orderId
//        );
//
//        return buildOrderResponse(payment, order);
//    }
/**
 * ==========================================================
 * INITIATE REFUND
 * ==========================================================
 *
 * Payment must be PAID before refund can be initiated.
 *
 * Flow:
 *
 * PAID
 *   |
 *   | refund request
 *   v
 * REFUND_PENDING
 *   |
 *   | refund.processed webhook
 *   v
 * REFUNDED
 *
 * For partial refunds:
 *
 * PAID
 *   |
 *   +---- ₹500 refund ----> REFUND_PENDING
 *   |
 *   +---- ₹200 refund ----> REFUND_PENDING
 *
 * Total refundable amount is always validated against
 * existing pending + completed refunds.
 */
    @Override
    @Transactional
    public PaymentRefund initiateRefund(
            RefundRequest request) throws RazorpayException {

        /*
         * ======================================================
         * 1. FETCH PAYMENT
         * ======================================================
         */
        PaymentDetailsV2 payment =
                paymentRepository
                        .findByBillingHeader_Id(request.getBillingHeaderId())
                        .orElseThrow(
                                () -> new EntityNotFoundException(
                                        "Payment not found for billing header ID:  "
                                                + request.getBillingHeaderId()
                                )
                        );


        /*
         * ======================================================
         * 2. VALIDATE REFUND AMOUNT
         * ======================================================
         */
        if (request.getRefundAmount() == null
                || request.getRefundAmount()
                .compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Refund amount must be greater than zero"
            );
        }

        MasAppointmentChangeReason cancelReason = masAppointmentChangeReasonRepository.findById(request.getRefundReasonId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Appointment change reason not found"));


        /*
         * ======================================================
         * 3. GET PAYMENT STATUS IDS
         * ======================================================
         */
        Long paidStatusId =
                paymentUtils
                        .getPaymentStatus(PaymentStatusCode.PAID)
                        .getId();

        Long refundPendingStatusId =
                paymentUtils
                        .getPaymentStatus(PaymentStatusCode.REFUND_PENDING)
                        .getId();

        Long refundedStatusId =
                paymentUtils
                        .getPaymentStatus(PaymentStatusCode.REFUNDED)
                        .getId();


        /*
         * ======================================================
         * 4. PAYMENT MUST BE PAID
         * ======================================================
         */
        if (!paidStatusId.equals(payment.getPaymentStatusId())) {

            throw new SDDException("Refund cannot be initiated for a payment that is not PAID.",
                    HttpStatus.NO_CONTENT.value(),
                    "Refund can only be initiated for a PAID payment. Payment ID: " + payment.getPaymentId());
        }


        /*
         * ======================================================
         * 5. RAZORPAY PAYMENT ID MUST EXIST
         * ======================================================
         */
        if (payment.getGatewayPaymentId() == null || payment.getGatewayPaymentId().isBlank()) {

            throw new SDDException("Gateway Payment Id",
                    HttpStatus.NOT_FOUND.value(),
                    "Cannot refund payment because gateway payment id is missing. Payment ID: "+ payment.getPaymentId());
        }


        /*
         * ======================================================
         * 6. PAYMENT AMOUNT MUST EXIST
         * ======================================================
         */
        if (payment.getAmount() == null || payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {

            throw new SDDException("Invalid Amount",
                    HttpStatus.BAD_REQUEST.value(),
                    "Invalid payment amount for payment: " + payment.getPaymentId()
            );
        }


        /*
         * ======================================================
         * 7. CALCULATE ALREADY REFUNDED / PENDING AMOUNT
         * ======================================================
         *
         * Both REFUND_PENDING and REFUNDED must be included.
         *
         * Why?
         *
         * Suppose:
         *
         * Payment = ₹1,000
         *
         * Refund 1 = ₹700 -> PENDING
         *
         * User requests another ₹500.
         *
         * Remaining refundable amount is only ₹300.
         *
         * Therefore ₹500 must be rejected.
         */
        BigDecimal alreadyRefundedAmount =
                refundRepository
                        .getTotalRefundedAmount(
                                payment.getPaymentId(),
                                List.of(
                                        refundPendingStatusId,
                                        refundedStatusId
                                )
                        );


        if (alreadyRefundedAmount == null) {
            alreadyRefundedAmount = BigDecimal.ZERO;
        }


        /*
         * ======================================================
         * 8. CALCULATE REMAINING REFUNDABLE AMOUNT
         * ======================================================
         */
        BigDecimal remainingRefundableAmount =
                payment.getAmount()
                        .subtract(alreadyRefundedAmount);

        /*
         * ======================================================
         * 9. VALIDATE REQUESTED REFUND
         * ======================================================
         */
        if (request.getRefundAmount().compareTo(remainingRefundableAmount) > 0) {

            throw new SDDException("Exceed refund amount",
                    HttpStatus.BAD_REQUEST.value(),
                    "Refund amount exceeds remaining refundable amount. "
                            + "Payment amount: " + payment.getAmount()
                            + ", already refunded: " + alreadyRefundedAmount
                            + ", remaining refundable: " + remainingRefundableAmount
            );
        }


        /*
         * ======================================================
         * 10. CONVERT INR TO PAISE
         * ======================================================
         *
         * Razorpay expects amount in the smallest currency unit.
         *
         * ₹100.50 -> 10050 paise
         */
        long refundAmountInPaise=paymentUtils.getAmountInSubUnitINR( request.getRefundAmount());



        /*
         * ======================================================
         * 11. PREPARE RAZORPAY REFUND REQUEST
         * ======================================================
         */
        JSONObject refundOptions = new JSONObject();

        refundOptions.put("amount", refundAmountInPaise);
        refundOptions.put("speed", "normal");
        refundOptions.put("receipt", "hmis_rfnd_pd" + payment.getPaymentId());
        JSONObject notes = new JSONObject();
        notes.put("payment_detail_id", String.valueOf(payment.getPaymentId()));
        notes.put("billing_hd_id", String.valueOf(request.getBillingHeaderId()));
        refundOptions.put("notes", notes);


        /*
         * ======================================================
         * 12. CALL RAZORPAY
         * ======================================================
         */
        Refund razorpayRefund =
                razorpayClient
                        .payments
                        .refund(payment.getGatewayPaymentId(), refundOptions);


        /*
         * ======================================================
         * 13. READ RAZORPAY RESPONSE
         * ======================================================
         */
        String gatewayRefundId = razorpayRefund.get("id");
        String gatewayRefundStatus = razorpayRefund.get("status");


        if (gatewayRefundId == null || gatewayRefundId.isBlank()) {

            throw new SDDException("Refund response",
                    HttpStatus.NOT_FOUND.value(),
                    "Razorpay refund response did not contain refund ID"
            );
        }


        /*
         * ======================================================
         * 14. CREATE PAYMENT REFUND
         * ======================================================
         */
        PaymentRefund refund = new PaymentRefund();

        refund.setPayment(payment);
        refund.setRefundModeId(payment.getPaymentModeId()!=null ?
                payment.getPaymentModeId() :
                null
        );
        refund.setRefundAmount(request.getRefundAmount());


        /*
         * Initially Razorpay normally returns PENDING
         * for normal refunds.
         */
        refund.setRefundStatusId(
                paymentUtils
                        .getPaymentStatus(PaymentStatusCode.REFUND_PENDING)
                        .getId()
        );
        refund.setRefundReferenceNo(paymentUtils.generateRefundReferenceNo());
        refund.setPaymentGatewayId(
                paymentUtils
                        .getPaymentGateway("RAZORPAY")
                        .getGatewayId()
        );
        refund.setGatewayRefundId(gatewayRefundId);
        refund.setAppointmentChangeReason(cancelReason);
        refund.setRefundReason(cancelReason.getReasonName());

        String currentUser = userContextService.getCurrentUserFullNameFromToken();
        refund.setCreatedBy(currentUser);
        refund.setUpdatedBy(currentUser);
        refund.setRefundRequestedAt(HMISUtil.getCurrentLocalDateTime());


        /*
         * ======================================================
         * 19. SAVE REFUND
         * ======================================================
         */
        PaymentRefund savedRefund = refundRepository.save(refund);


        log.info(
                "Refund initiated successfully. " +
                        "paymentId={}, gatewayPaymentId={}, " +
                        "refundId={}, amount={}, razorpayStatus={}",
                payment.getPaymentId(),
                payment.getGatewayPaymentId(),
                gatewayRefundId,
                request.getRefundAmount(),
                gatewayRefundStatus
        );


        return savedRefund;
    }


    public PaymentGatewayStatusResponse getPaymentStatus(Long paymentId) {

        PaymentDetailsV2 payment =
                paymentRepository.findById(paymentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Payment not found"
                                )
                        );

        MasPaymentStatus status =
                masPaymentStatusRepository
                        .findById(payment.getPaymentStatusId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Payment status not found"
                                )
                        );

        return new PaymentGatewayStatusResponse(
                payment.getPaymentId(),
                status.getStatusCode(),
                payment.getGatewayOrderId(),
                payment.getGatewayPaymentId(),
                payment.getAmount(),
                payment.getCurrency()
        );
    }




}
