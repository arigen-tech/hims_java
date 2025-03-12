package com.hims.entity.repository;

import com.hims.entity.MasIdentificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasIdentificationTypeRepository extends JpaRepository<MasIdentificationType, Long> {
    List<MasIdentificationType> findByStatusIgnoreCase(String status);
    List<MasIdentificationType> findByStatusInIgnoreCase(List<String> statuses);

}
