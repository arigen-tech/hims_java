package com.hims.entity.repository;

import com.hims.entity.PaymentWebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentWebhookEventRepository extends JpaRepository<PaymentWebhookEvent, Long> {

    boolean existsByGatewayAndEventId(
            String gateway,
            String eventId
    );

    Optional<PaymentWebhookEvent>
    findByGatewayAndEventId(
            String gateway,
            String eventId
    );

    Optional<PaymentWebhookEvent> findByEventId(String eventId);

    List<PaymentWebhookEvent> findByPayment_PaymentIdOrderByReceivedAtAsc(Long paymentId);


        @Query("""
        select we from PaymentWebhookEvent we
        left join we.payments p
        where p.paymentId = :paymentId or we.payment.paymentId = :paymentId
        """)
        List<PaymentWebhookEvent> findAllForPayment(@Param("paymentId") Long paymentId);

}
