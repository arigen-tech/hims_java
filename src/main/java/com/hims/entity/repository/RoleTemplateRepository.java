package com.hims.entity.repository;

import com.hims.entity.RoleTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoleTemplateRepository extends JpaRepository<RoleTemplate, Long> {
    List<RoleTemplate> findByRoleId(Long roleId);

    @Query("SELECT r FROM RoleTemplate r WHERE r.roleId = :roleId AND r.template.id = :templateId")
    Optional<RoleTemplate> findByRoleIdAndTemplateId(Long roleId, Long templateId);

    List<RoleTemplate> findByRoleIdAndStatusIgnoreCase(Long roleId, String status);

    List<RoleTemplate> findByRoleIdInAndStatusIgnoreCase(List<Long> roleIds, String y);
    @Query("""
        select rt
        from RoleTemplate rt
        join fetch rt.template t
        where rt.roleId in :roleIds
          and lower(rt.status) = lower(:status)
    """)
    List<RoleTemplate> findActiveByRoleIds(@Param("roleIds") List<Long> roleIds,
                                           @Param("status") String status);

    @Query("""
        select distinct t.id
        from RoleTemplate rt
        join rt.template t
        where rt.roleId in :roleIds
          and lower(rt.status) = lower(:status)
    """)
    List<Long> findActiveTemplateIdsByRoleIds(@Param("roleIds") List<Long> roleIds,
                                              @Param("status") String status);
}
