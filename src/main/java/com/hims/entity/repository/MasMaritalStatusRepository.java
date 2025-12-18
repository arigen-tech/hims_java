package com.hims.entity.repository;

import com.hims.entity.MasMaritalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MasMaritalStatusRepository extends JpaRepository<MasMaritalStatus, Long> {
    List<MasMaritalStatus> findByStatusIgnoreCase(String status);
    List<MasMaritalStatus> findByStatusInIgnoreCase(List<String> statuses);

    List<MasMaritalStatus> findByStatusIgnoreCaseOrderByNameAsc(String y);



    //List<MasMaritalStatus> findByStatusIgnoreCaseInOrderByLastChgDateDesc(List<String> y);

    List<MasMaritalStatus> findAllByOrderByStatusDescLastChgDateDesc();
}
