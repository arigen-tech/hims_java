package com.hims.service;

import com.hims.entity.PaymentRefund;
import com.hims.request.OrderRequest;
import com.hims.request.RefundRequest;
import com.hims.response.PaymentGatewayStatusResponse;
import com.razorpay.RazorpayException;

import java.util.Map;

public interface PaymentGatewayService {


     Map<String, Object> createPaymentOrder(OrderRequest request) throws RazorpayException;
     PaymentRefund initiateRefund(RefundRequest request) throws RazorpayException;

     PaymentGatewayStatusResponse getPaymentStatus(Long paymentId);
}
