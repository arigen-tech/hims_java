package com.hims.entity.repository;

import com.hims.entity.MasMedicalHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasMedicalHistoryRepository extends JpaRepository<MasMedicalHistory, Long> {
    List<MasMedicalHistory> findByStatusIgnoreCaseIn(List<String> y);

    List<MasMedicalHistory> findByStatusIgnoreCase(String y);
}
