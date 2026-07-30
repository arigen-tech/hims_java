package com.hims.entity.repository;

import com.hims.entity.IpdBillingHeader;
import com.hims.response.PaymentStatusResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IpdBillingHeaderRepository extends JpaRepository<IpdBillingHeader,Long> {

    Optional<IpdBillingHeader> findByInpatientId_InpatientId(Long inpatientId);

    @Query("""
    SELECT new com.hims.response.PaymentStatusResponse(
        i.inpatientId,
        b.billId,
        bs.billStatusId,
        bs.statusName,
        ps.paymentStatusId,
        ps.statusName,
        b.outstandingAmount
    )
    FROM IpdBillingHeader b
    JOIN b.inpatientId i
    LEFT JOIN b.billStatus bs
    LEFT JOIN b.paymentStatus ps
    WHERE i.inpatientId = :inpatientId
    """)
    PaymentStatusResponse getPaymentStatus(@Param("inpatientId") Long inpatientId);


}
