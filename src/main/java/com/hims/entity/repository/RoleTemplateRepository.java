package com.hims.entity.repository;

import com.hims.entity.RoleTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RoleTemplateRepository extends JpaRepository<RoleTemplate, Long> {
    List<RoleTemplate> findByRoleId(Long roleId);

    @Query("SELECT r FROM RoleTemplate r WHERE r.roleId = :roleId AND r.template.id = :templateId")
    Optional<RoleTemplate> findByRoleIdAndTemplateId(Long roleId, Long templateId);
}
