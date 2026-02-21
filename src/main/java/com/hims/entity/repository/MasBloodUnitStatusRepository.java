package com.hims.entity.repository;

import com.hims.entity.MasBloodUnitStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasBloodUnitStatusRepository
        extends JpaRepository<MasBloodUnitStatus, Long> {




    List<MasBloodUnitStatus>
    findAllByOrderByStatusDescLastUpdateDateDesc();



    List<MasBloodUnitStatus> findByStatusIgnoreCaseOrderByStatusNameAsc(String y);
}
