package com.hims.entity.repository;

import com.hims.entity.MasEmployeeLanguageMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasEmployeeLanguageMappingRepository
        extends JpaRepository<MasEmployeeLanguageMapping,Long> {
    List<MasEmployeeLanguageMapping> findByEmpId(Long empId);


    void deleteByEmpId(Long employeeId);
}
