package com.hims.entity.repository;

import com.hims.entity.MasOpdMedicalAdvise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasOpdMedicalAdviseRepository extends JpaRepository<MasOpdMedicalAdvise,Long> {
    List<MasOpdMedicalAdvise> findByStatusIgnoreCaseOrderByMedicalAdviseNameAsc(String y);

    List<MasOpdMedicalAdvise> findAllByOrderByLastUpdateDateDesc();

    List<MasOpdMedicalAdvise> findAllByOrderByStatusDescLastUpdateDateDesc();
}
