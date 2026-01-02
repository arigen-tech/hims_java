package com.hims.entity.repository;

import com.hims.entity.MasPatientPreparation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasPatientPreparationRepo extends JpaRepository<MasPatientPreparation, Long> {

    List<MasPatientPreparation> findByStatusIgnoreCaseOrderByPreparationNameAsc(String status);

    List<MasPatientPreparation> findAllByOrderByStatusDescLastUpdateDateDesc();

    boolean existsByPreparationCodeIgnoreCase(String preparationCode);
}
