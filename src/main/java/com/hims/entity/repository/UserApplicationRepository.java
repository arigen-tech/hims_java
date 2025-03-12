package com.hims.entity.repository;

import com.hims.entity.UserApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserApplicationRepository extends JpaRepository<UserApplication, Long> {
    List<UserApplication> findByStatusIgnoreCase(String status);
    List<UserApplication> findByStatusInIgnoreCase(List<String> statuses);

}
