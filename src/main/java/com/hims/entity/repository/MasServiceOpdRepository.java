package com.hims.entity.repository;

import com.hims.entity.MasHospital;
import com.hims.entity.MasServiceOpd;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MasServiceOpdRepository extends JpaRepository<MasServiceOpd, Long> {
    List<MasServiceOpd> findByHospitalId(MasHospital hospital);
}
