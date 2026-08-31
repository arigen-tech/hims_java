package com.hims.entity.repository;

import com.hims.entity.MasProcedureType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MasProcedureTypeRepository extends JpaRepository<MasProcedureType, Long> {
   // List<MasProcedureType> findByStatusIgnoreCase(String y);

    List<MasProcedureType> findByStatusIgnoreCaseOrderByProcedureTypeNameAsc(String y);

    List<MasProcedureType> findAllByOrderByLastUpdateDateDesc();

    List<MasProcedureType> findAllByOrderByStatusDescLastUpdateDateDesc();

    Optional<MasProcedureType>findByProcedureTypeCode(String code);
}
