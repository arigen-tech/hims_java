package com.hims.entity.repository;

import com.hims.entity.MasApplication;
import com.hims.entity.TemplateApplication;
import com.hims.projection.TemplateAppStatusProjection;
import com.hims.projection.TemplateApplicationProjection;
import com.hims.projection.UrlAppProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TemplateApplicationRepository extends JpaRepository<TemplateApplication, Long> {
    List<TemplateApplication> findByStatusIgnoreCase(String status);
    List<TemplateApplication> findByStatusInIgnoreCase(List<String> statuses);
    List<TemplateApplication> findByTemplateId(Long templateId);
    Optional<TemplateApplication> findByTemplate_IdAndApp_AppId(Long templateId, String appId);

    List<TemplateApplication> findByApp_AppId(String appId);
    @Query("SELECT ta FROM TemplateApplication ta WHERE ta.template.id = :templateId AND ta.app.appId = :appId")
    Optional<TemplateApplication> findByTemplateAndApp(
            @Param("templateId") Long templateId,
            @Param("appId") String appId);

    List<TemplateApplication> findByTemplateIdAndStatusIgnoreCase(Long templateId, String y);

    Optional<TemplateApplication> findByTemplateIdAndApp_AppId(Long templateId, String appId);

    List<TemplateApplication> findByApp_AppIdAndStatusNot(String appId, String status);

    List<TemplateApplication> findByTemplateIdInAndStatusIgnoreCase(Set<Long> templateIds, String y);

    @Query("""
        select ta
        from TemplateApplication ta
        join fetch ta.template t
        join fetch ta.app a
        where t.id in :templateIds
          and lower(ta.status) = lower(:status)
    """)
    List<TemplateApplication> findActiveByTemplateIds(@Param("templateIds") Set<Long> templateIds,
                                                      @Param("status") String status);

    @Query("""
    select distinct a
    from TemplateApplication ta
    join ta.template t
    join ta.app a
    where t.id in :templateIds
      and lower(ta.status) = lower(:status)
      and lower(a.status) = lower(:status)
""")
    List<MasApplication> findActiveAppsByTemplateIds(@Param("templateIds") Set<Long> templateIds,
                                                     @Param("status") String status);

    @Query(value = """
            SELECT
                ta.temp_app_id AS id,
                ta.template_id AS templateId,
                ma.app_id AS appId,
                ma.name AS appName,
                ta.status AS status,
                ta.last_chg_date AS lastChgDate,
                ta.last_chg_by AS lastChgBy,
                ta.order_no AS orderNo,
                ma.parent_id AS parentId
            FROM template_application ta
            LEFT JOIN mas_application ma
                ON ta.app_id = ma.app_id
            WHERE ta.template_id = :templateId
              AND ma.app_id IS NOT NULL
              AND ma.parent_id = '0'
            ORDER BY ta.order_no ASC, ta.temp_app_id ASC
            """, nativeQuery = true)
    List<TemplateApplicationProjection> getTemplateApplicationsByTemplateId(@Param("templateId") Long templateId);

    @Query(value = """
            SELECT
                ta.temp_app_id AS id,
                ta.template_id AS templateId,
                ma.app_id AS appId,
                ma.name AS appName,
                ta.status AS status,
                ta.last_chg_date AS lastChgDate,
                ta.last_chg_by AS lastChgBy,
                ta.order_no AS orderNo,
                ma.parent_id AS parentId
            FROM template_application ta
            LEFT JOIN mas_application ma
                ON ta.app_id = ma.app_id
            WHERE LOWER(ta.status) = LOWER(:status)
            ORDER BY ta.order_no ASC, ta.temp_app_id ASC
            """, nativeQuery = true)
    List<TemplateApplicationProjection> getAllTemplateApplicationsByStatus(@Param("status") String status);

    @Query(value = """
            SELECT
                ta.temp_app_id AS id,
                ta.template_id AS templateId,
                ma.app_id AS appId,
                ma.name AS appName,
                ta.status AS status,
                ta.last_chg_date AS lastChgDate,
                ta.last_chg_by AS lastChgBy,
                ta.order_no AS orderNo,
                ma.parent_id AS parentId
            FROM template_application ta
            LEFT JOIN mas_application ma
                ON ta.app_id = ma.app_id
            WHERE LOWER(ta.status) IN (:statuses)
            ORDER BY ta.order_no ASC, ta.temp_app_id ASC
            """, nativeQuery = true)
    List<TemplateApplicationProjection> getAllTemplateApplicationsByStatuses(@Param("statuses") List<String> statuses);

    @Query("""
        select distinct
            a.appId as appId,
            a.name as name,
            a.url as url,
            a.parentId as parentId,
            a.orderNo as orderNo
        from TemplateApplication ta
        join ta.template t
        join ta.app a
        where t.id in :templateIds
          and lower(ta.status) = lower(:status)
          and lower(a.status) = lower(:status)
    """)
    List<UrlAppProjection> findActiveAppDetailsByTemplateIds(@Param("templateIds") Set<Long> templateIds,
                                                             @Param("status") String status);

    @Query("""
            SELECT
                ta.app.appId AS appId,
                ta.status AS status
            FROM TemplateApplication ta
            WHERE ta.template.id = :templateId
              AND ta.app.appId IN :appIds
            """)
    List<TemplateAppStatusProjection> findTemplateStatusesByTemplateIdAndAppIds(
            @Param("templateId") Long templateId,
            @Param("appIds") List<String> appIds
    );
}
