package com.hims.entity.repository;

import com.hims.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MasServiceOpdRepository extends JpaRepository<MasServiceOpd, Long> {
    List<MasServiceOpd> findByHospitalIdId(Long hospitalId);
//    Optional<MasServiceOpd> findByHospitalIdAndDoctorUserIdAndDepartmentIdAndServiceCatId(MasHospital hospital, User doctor, MasDepartment department, MasServiceCategory serviceCategory);
}
