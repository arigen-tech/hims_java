package com.hims.utils;

import com.hims.constants.PaymentStatusCode;
import com.hims.constants.RazorpayPaymentMethod;
import com.hims.entity.*;
import com.hims.entity.repository.MasPaymentGatewayRepository;
import com.hims.entity.repository.MasPaymentModeRepository;
import com.hims.entity.repository.MasPaymentStatusRepository;
import com.hims.exception.SDDException;
import com.hims.service.TransactionSequenceService;
import com.hims.service.UserContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentUtils {


    private final MasPaymentStatusRepository masPaymentStatusRepository;

    private final MasPaymentGatewayRepository masPaymentGatewayRepository;

    private final MasPaymentModeRepository masPaymentModeRepository;

    private final AuthUtil authUtil;

    private final TransactionSequenceService transactionSequenceService;

    private final UserContextService userContextService;



    public MasPaymentStatus getPaymentStatus(PaymentStatusCode statusCode) {

        return masPaymentStatusRepository
                .findByStatusCode(statusCode.name())
                .orElseThrow(() ->
                        new SDDException(
                               "Payment Status Code",
                                HttpStatus.NOT_FOUND.value(),
                                "Payment status not configured: " + statusCode
                        )
                );
    }

    public MasPaymentGateway getPaymentGateway(String code) {
        return  masPaymentGatewayRepository.findByGatewayCodeIgnoreCase(code)
                .orElseThrow(() ->
                        new SDDException(
                                "Payment Gateway",
                                HttpStatus.NOT_FOUND.value(),
                                "Payment gateway not configured: " + code
                        )
                );

    }


    /**
     * Resolves payment_mode_id from the raw Razorpay "method" string
     * (payload.payment.entity.method in the webhook), matched against
     * mas_payment_mode.mode_code.
     * Returns null if the method is unrecognized or unmapped — caller
     * decides whether that's acceptable (leave column null) or a hard failure.
     */
    public Long resolvePaymentModeId(String razorpayMethodRaw) {

        RazorpayPaymentMethod method = RazorpayPaymentMethod.fromValue(razorpayMethodRaw);

        if (method == null) {
            log.warn(
                    "Unrecognized Razorpay payment method '{}' -- payment_mode_id will be left unset",
                    razorpayMethodRaw
            );
            return null;
        }

        Optional<MasPaymentMode> paymentMode =
                masPaymentModeRepository.findByModeCodeIgnoreCase(method.getValue());

        if (paymentMode.isEmpty()) {
            log.warn(
                    "No mas_payment_mode row found for mode_code='{}' -- payment_mode_id will be left unset",
                    method.getValue()
            );
            return null;
        }

        return paymentMode.get().getPaymentModeId();
    }

    public String generatePaymentReferenceNo() {
        return   transactionSequenceService.generateTransactionNumber(
                HMISTransaction.PAYMENT_REFERENCE_NO,
                userContextService.getCurrentUserContext().getHospitalId()
        );
    }

    public String generateReceiptNumber(BillingHeader billingHeader) {

        return transactionSequenceService.generateTransactionNumber(
                HMISTransaction.RECEIPT_NO,
                billingHeader.getHospital().getId()
        );
    }

    public String generateRefundReferenceNo() {
        return transactionSequenceService.generateTransactionNumber(
                HMISTransaction.REFUND_REFERENCE_NO,
                userContextService.getCurrentUserContext().getHospitalId()
        );
    }

    public MasPaymentMode getPaymentMode(String mode) {

        return masPaymentModeRepository
                .findByModeCodeIgnoreCase(mode)
                .orElseThrow(() ->
                        new SDDException(
                                "Payment Status Code",
                                HttpStatus.NOT_FOUND.value(),
                                "Payment status not configured: " + mode
                        )
                );
    }


    /**
     * Reverse lookup — resolves a payment_status_id back to its string code
     * (e.g. "PAID", "REFUND_PENDING"), so business logic can branch on the
     * code instead of hardcoding numeric ids.
     */
    public String getStatusCodeById(Long statusId) {
        if (statusId == null) {
            return null;
        }
        return masPaymentStatusRepository.findById(statusId)
                .map(MasPaymentStatus::getStatusCode)
                .orElseThrow(() ->
                        new SDDException(
                                "Payment Status Code",
                                HttpStatus.NOT_FOUND.value(),
                                "Payment status not configured for id: " + statusId
                        )
                );
    }

    public long getAmountInSubUnitINR(BigDecimal amount){

        long amountInPaisa;
        try {
            amountInPaisa=amount.movePointRight(2)
                    .longValueExact();

        } catch (ArithmeticException e) {

            throw new SDDException("Invalid refund amount",
                    HttpStatus.BAD_REQUEST.value(),
                    "Amount must have maximum 2 decimal places"
            );
        }
        return amountInPaisa;
    }
}
