package com.hims.entity.repository;

import com.hims.entity.GynMasMenstrualPattern;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GynMasMenstrualPatternRepository
        extends JpaRepository<GynMasMenstrualPattern, Long> {

    List<GynMasMenstrualPattern>
    findByStatusIgnoreCaseOrderByPatternValueAsc(String status);

    List<GynMasMenstrualPattern>
    findAllByOrderByStatusDescLastUpdateDateDesc();
}
