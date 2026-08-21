package com.hims.entity.repository;

import com.hims.entity.MasBrand;
import com.hims.response.MasBrandResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MasBrandRepository extends JpaRepository<MasBrand,Long> {
//    List<MasBrand> findByStatusIgnoreCase(String y);
//
//    List<MasBrand> findByStatusInIgnoreCase(List<String> y);

    List<MasBrand> findByStatusIgnoreCaseOrderByBrandNameAsc(String y);

 //   List<MasBrand> findByStatusIgnoreCaseInOrderByLastUpdatedDtDesc(List<String> y);

    List<MasBrand> findAllByOrderByStatusDescLastUpdatedDtDesc();

    @Query(value = """
SELECT new com.hims.response.MasBrandResponse(
    b.brandId,
    b.brandName,
    b.description,
    b.status,
    b.lastUpdatedBy,
    b.lastUpdatedDt
)
FROM MasBrand  b
LEFT JOIN b.manufacturer m
LEFT JOIN  b.itemType i
WHERE m.manufacturerId =:manufacturerId
AND b.status=:activeStatus
AND (:itemTypeCode IS NULL 
         OR 
      i.code=:itemTypeCode
     )
     ORDER BY b.brandName ASC
""")
    Optional<List<MasBrandResponse>> getBrandsWrtManufacturerAndItemTypeCode(Long manufacturerId, String itemTypeCode, String activeStatus);
}
