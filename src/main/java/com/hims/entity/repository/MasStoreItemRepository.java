package com.hims.entity.repository;

import com.hims.entity.MasStoreItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MasStoreItemRepository extends JpaRepository<MasStoreItem,Long> {
    List<MasStoreItem> findByStatusIgnoreCaseAndHospitalIdAndDepartmentId(String y, Long hospitalId, Long departmentId);

    List<MasStoreItem> findByStatusInIgnoreCaseAndHospitalIdAndDepartmentId(List<String> y,  Long hospitalId, Long departmentId);

    Optional<MasStoreItem> findByPvmsNo(String code);

    List<MasStoreItem> findByStatusIgnoreCaseAndSectionId_SectionId(String status, Integer sectionId);

    List<MasStoreItem> findByStatusInIgnoreCaseAndSectionId_SectionId(List<String> statuses, Integer sectionId);



    Optional<MasStoreItem> findFirstByPvmsNoOrNomenclature(String pvmsNo, String nomenclature);

   
    Optional<MasStoreItem> findByPvmsNoAndItemIdNot(String pvmsNo, Long id);

    Optional<MasStoreItem> findByNomenclatureAndItemIdNot(String nomenclature, Long id);

    List<MasStoreItem> findByStatus(String y);
}
