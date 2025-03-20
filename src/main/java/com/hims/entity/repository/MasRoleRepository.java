package com.hims.entity.repository;

import com.hims.entity.MasReligion;
import com.hims.entity.MasRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MasRoleRepository extends JpaRepository<MasRole, String> {
    Optional<MasRole> findByRoleDesc(String doctor);
    List<MasRole> findByStatusIgnoreCase(String status);
    List<MasRole> findByStatusInIgnoreCase(List<String> statuses);
}
