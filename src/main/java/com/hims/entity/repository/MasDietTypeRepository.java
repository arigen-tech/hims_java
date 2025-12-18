package com.hims.entity.repository;

import com.hims.entity.MasDietType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasDietTypeRepository extends JpaRepository<MasDietType,Long> {
  //  List<MasDietType> findByStatusIgnoreCase(String y);

    List<MasDietType> findByStatusIgnoreCaseOrderByDietTypeNameAsc(String y);

 //   List<MasDietType> findAllOrderByLastUpdateDateDesc();

    List<MasDietType> findAllByOrderByLastUpdateDateDesc();

    List<MasDietType> findAllByOrderByStatusDescLastUpdateDateDesc();
}
