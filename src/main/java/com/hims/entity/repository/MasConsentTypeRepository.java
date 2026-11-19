package com.hims.entity.repository;

import com.hims.entity.MasConsentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface MasConsentTypeRepository extends JpaRepository<MasConsentType, Long> {
    Optional<MasConsentType> findByConsentTypeCodeIgnoreCase(String consentTypeCode);

    @Query(value = """
            SELECT * FROM public.mas_consent_type
            WHERE (:flag = 0 OR UPPER(status) = 'Y')
              AND (COALESCE(CAST(:search AS varchar), '') = ''
                   OR LOWER(consent_type_code) LIKE LOWER(CONCAT('%', CAST(:search AS varchar), '%'))
                   OR LOWER(consent_type_name) LIKE LOWER(CONCAT('%', CAST(:search AS varchar), '%')))
            ORDER BY status DESC, consent_type_name ASC, consent_type_id ASC
            """, nativeQuery = true)
    List<MasConsentType> searchAll(@Param("flag") int flag, @Param("search") String search);

    @Query(value = """
            SELECT * FROM public.mas_consent_type
            WHERE (:flag = 0 OR UPPER(status) = 'Y')
              AND (COALESCE(CAST(:search AS varchar), '') = ''
                   OR LOWER(consent_type_code) LIKE LOWER(CONCAT('%', CAST(:search AS varchar), '%'))
                   OR LOWER(consent_type_name) LIKE LOWER(CONCAT('%', CAST(:search AS varchar), '%')))
            ORDER BY status DESC, consent_type_name ASC, consent_type_id ASC
            """, countQuery = """
            SELECT COUNT(*) FROM public.mas_consent_type
            WHERE (:flag = 0 OR UPPER(status) = 'Y')
              AND (COALESCE(CAST(:search AS varchar), '') = ''
                   OR LOWER(consent_type_code) LIKE LOWER(CONCAT('%', CAST(:search AS varchar), '%'))
                   OR LOWER(consent_type_name) LIKE LOWER(CONCAT('%', CAST(:search AS varchar), '%')))
            """, nativeQuery = true)
    Page<MasConsentType> search(@Param("flag") int flag, @Param("search") String search, Pageable pageable);
}
