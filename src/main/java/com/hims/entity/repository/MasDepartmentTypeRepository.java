package com.hims.entity.repository;

import com.hims.entity.MasDepartmentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MasDepartmentTypeRepository extends JpaRepository<MasDepartmentType, Long> {
//    List<MasDepartmentType> findByStatusIgnoreCase(String status);
//    List<MasDepartmentType> findByStatusInIgnoreCase(List<String> statuses);

    List<MasDepartmentType> findByStatusIgnoreCaseOrderByDepartmentTypeNameAsc(String y);

  //  List<MasDepartmentType> findByStatusIgnoreCaseInOrderByLastChgDateDesc(List<String> y);

    List<MasDepartmentType> findAllByOrderByStatusDescLastChgDateDesc();
}
