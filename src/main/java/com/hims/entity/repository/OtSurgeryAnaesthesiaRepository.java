package com.hims.entity.repository;

import com.hims.entity.OtSurgeryAnaesthesia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OtSurgeryAnaesthesiaRepository extends JpaRepository<OtSurgeryAnaesthesia, Long> {
    Optional<OtSurgeryAnaesthesia> findByOtSurgeryExecution_OtSurgeryExecutionId(Long executionId);
    @Query(value = "SELECT anaesthesia.* " +
            "FROM public.ot_surgery_anaesthesia anaesthesia " +
            "JOIN public.ot_surgery_execution execution " +
            "ON execution.ot_surgery_execution_id = anaesthesia.ot_surgery_execution_id " +
            "WHERE execution.ot_booking_id = :otBookingId " +
            "ORDER BY anaesthesia.ot_surgery_anaesthesia_id DESC " +
            "LIMIT 1", nativeQuery = true)
    Optional<OtSurgeryAnaesthesia> findLatestByBookingId(@Param("otBookingId") Long otBookingId);
}
