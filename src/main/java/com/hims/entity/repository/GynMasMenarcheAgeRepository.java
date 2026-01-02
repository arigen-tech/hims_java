package com.hims.entity.repository;

import com.hims.entity.GynMasMenarcheAge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GynMasMenarcheAgeRepository
        extends JpaRepository<GynMasMenarcheAge, Long> {

    List<GynMasMenarcheAge>
    findByStatusIgnoreCaseOrderByMenarcheAgeAsc(String status);

    List<GynMasMenarcheAge>
    findAllByOrderByStatusDescLastUpdateDateDesc();
}
