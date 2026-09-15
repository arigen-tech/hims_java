package com.hims.entity.repository;

import com.hims.entity.BillingHeader;
import com.hims.entity.PaymentDetailsV2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentDetailsV2Repository extends JpaRepository<PaymentDetailsV2, Long> {

    Optional<PaymentDetailsV2> findByGatewayOrderId(String gatewayOrderId);

    Optional<PaymentDetailsV2> findByGatewayPaymentId(String gatewayPaymentId);

    Optional<PaymentDetailsV2> findByPaymentReferenceNo(String paymentReferenceNo);

    Optional<PaymentDetailsV2> findByBillingHeader_Id(Long billingHeaderId);

    Boolean existsByBillingHeader_Id(Long billingHeaderId);

    Boolean existsByBillingHeader_IdAndGatewayPaymentIdIsNotNull(Long billingHeaderId);


    Optional<PaymentDetailsV2> findByBillingHeader_IdAndGatewayPaymentIdIsNull(
            Long billingHdId
    );

    List<PaymentDetailsV2> findAllByGatewayOrderId(String gatewayOrderId);

    List<PaymentDetailsV2> findAllByGatewayPaymentId(String gatewayPaymentId);

}
