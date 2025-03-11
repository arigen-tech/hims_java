package com.hims.entity.repository;

import com.hims.entity.MasRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MasRoleRepository extends JpaRepository<MasRole, String> {
    Optional<MasRole> findByRoleDesc(String doctor);
}
