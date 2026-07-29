package com.hims.entity.repository;

import com.hims.entity.IpDischargeSummary;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IpDischargeSummaryRepository extends JpaRepository<IpDischargeSummary,Long> {
    Optional<IpDischargeSummary> findByInpatient_InpatientId(@NotNull(message = "Inpatient Id is required") Long inpatientId);
}
