package com.hims.entity.repository;

import com.hims.entity.EmployeeLanguageId;
import com.hims.entity.MasEmployee;
import com.hims.entity.MasEmployeeLanguageMapping;
import io.netty.handler.codec.http2.Http2Connection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasEmployeeLanguageMappingRepository
        extends JpaRepository<MasEmployeeLanguageMapping, EmployeeLanguageId> {
    List<MasEmployeeLanguageMapping> findByEmployee(MasEmployee emp);
}
