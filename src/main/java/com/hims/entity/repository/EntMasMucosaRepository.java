package com.hims.entity.repository;

import com.hims.entity.EntMasMucosa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EntMasMucosaRepository
        extends JpaRepository<EntMasMucosa, Long> {

    List<EntMasMucosa>
    findByStatusIgnoreCaseOrderByMucosaStatusAsc(String status);

    List<EntMasMucosa>
    findAllByOrderByStatusDescLastUpdateDateDesc();
}
