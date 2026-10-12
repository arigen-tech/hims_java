package com.hims.constants;


/**
 * Payment methods Razorpay sends in the webhook payload's
 * payload.payment.entity.method field.
 * Reference: https://razorpay.com/docs/api/payments/fetch-with-id/
 */
public enum RazorpayPaymentMethod {

    CARD("card"),
    NETBANKING("netbanking"),
    WALLET("wallet"),
    EMI("emi"),
    UPI("upi"),
    PAYLATER("paylater");

    private final String value;

    RazorpayPaymentMethod(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Maps the raw string from the webhook JSON to the enum.
     * Returns null (not an exception) for an unrecognized/blank value,
     * so a new Razorpay method type doesn't crash webhook processing —
     * the caller decides how to handle an unmapped method.
     */
    public static RazorpayPaymentMethod fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        for (RazorpayPaymentMethod method : values()) {
            if (method.value.equalsIgnoreCase(value.trim())) {
                return method;
            }
        }
        return null;
    }
}
