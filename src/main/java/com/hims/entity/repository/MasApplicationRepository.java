package com.hims.entity.repository;

import com.hims.entity.MasApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MasApplicationRepository extends JpaRepository<MasApplication, String> {
    List<MasApplication> findByStatusIgnoreCase(String status);
    List<MasApplication> findByStatusInIgnoreCase(List<String> statuses);

}
