package com.hims.entity.repository;

import com.hims.entity.MasOtTeamRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MasOtTeamRoleRepository extends JpaRepository<MasOtTeamRole, Long> {

    // Active records only, alphabetical by role name (flag = 1)
    List<MasOtTeamRole> findByStatusIgnoreCaseOrderByRoleNameAsc(String status);

    // All records, active first, most recently changed first (flag = 0)
    List<MasOtTeamRole> findAllByOrderByStatusDescLastChgDateDesc();

    // For uniqueness validation on roleCode (create / update)
    Optional<MasOtTeamRole> findByRoleCodeIgnoreCase(String roleCode);

    // For uniqueness validation on roleName (create / update)
    Optional<MasOtTeamRole> findByRoleNameIgnoreCase(String roleName);
}