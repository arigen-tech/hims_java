package com.hims.entity.repository;

import com.hims.entity.MasMedicalHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MasMedicalHistoryRepository extends JpaRepository<MasMedicalHistory, Long> {
}
