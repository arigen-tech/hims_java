package com.hims.entity.repository;

import com.hims.entity.MasOpdSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MasOpdSessionRepository extends JpaRepository<MasOpdSession, Long> {
    List<MasOpdSession> findByStatusIgnoreCase(String status);
    List<MasOpdSession> findByStatusInIgnoreCase(List<String> statuses);

}
