package com.hims.constants;


/**
 * String codes for the payment/refund lifecycle. These are matched against
 * your payment-status master table's status_code column to resolve the
 * actual numeric payment_status_id / refund_status_id at runtime -
 * IDs are never hardcoded per the spec (Rule: "Do not hardcode payment-status IDs").
 */
public enum PaymentStatusCode {

    PENDING,

    PAID,

    FAILED,

    REFUND_PENDING_CASH,

    REFUND_PENDING,

    REFUND_PROCESSED,
    REFUNDED

}