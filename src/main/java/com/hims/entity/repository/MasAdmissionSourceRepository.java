package com.hims.entity.repository;

import com.hims.entity.MasAdmissionSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasAdmissionSourceRepository extends JpaRepository<MasAdmissionSource,Long> {
    List<MasAdmissionSource> findByStatusIgnoreCaseOrderByAdmissionSourceNameAsc(String lowerCase);

    List<MasAdmissionSource> findAllByOrderByStatusDescLastUpdateDateDesc();
}
