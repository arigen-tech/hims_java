package com.hims.entity.repository;

import com.hims.entity.MasLanguage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MasLanguageRepository extends JpaRepository<MasLanguage,Long> {
    List<MasLanguage> findByStatusIgnoreCaseOrderByLanguageNameAsc(String y);

    List<MasLanguage> findAllByOrderByStatusDescLastUpdateDateDesc();
}
