package com.hims.entity.repository;

import com.hims.entity.IpDietOrder;
import com.hims.response.PreviousDietHistoryResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IpDietOrderRepository extends JpaRepository<IpDietOrder,Long> {
    @Query("""
    SELECT new com.hims.response.PreviousDietHistoryResponse(
        ido.inpatient.inpatientId,
        ido.dietOrderId,
        ido.dietType.dietTypeId,
        ido.dietType.dietTypeName,
        ido.fromDate,
        ido.toDate,
        ido.specialInstruction,
        ido.orderedBy.userName,
        ido.status
    )
    FROM IpDietOrder ido
    WHERE ido.inpatient.inpatientId = :inpatientId
    ORDER BY ido.dietOrderId ASC
    """)
    List<PreviousDietHistoryResponse> getPreviousDietHistory(
            @Param("inpatientId") Long inpatientId
    );

    Optional<IpDietOrder> findByInpatient_InpatientIdAndStatus(Long inpatientId, String ipActiveDiet);
}
