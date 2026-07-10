package com.hims.entity.repository;

import com.hims.entity.MasBloodCompatibility;
import jakarta.validation.constraints.DecimalMin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasBloodCompatibilityRepository
        extends JpaRepository<MasBloodCompatibility, Long> {

    List<MasBloodCompatibility> findByStatusIgnoreCaseOrderByCompatibilityIdAsc(String status);

    List<MasBloodCompatibility> findAllByOrderByStatusDescLastUpdateDateDesc();
}
