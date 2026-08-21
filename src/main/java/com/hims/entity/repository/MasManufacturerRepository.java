package com.hims.entity.repository;

import com.hims.entity.MasManufacturer;
import com.hims.response.MasManufacturerResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MasManufacturerRepository extends JpaRepository<MasManufacturer,Long> {

    List<MasManufacturer> findByStatusIgnoreCaseOrderByManufacturerNameAsc(String y);

  //  List<MasManufacturer> findByStatusIgnoreCaseInOrderByLastUpdatedDtDesc(List<String> y);

    List<MasManufacturer> findAllByOrderByStatusDescLastUpdatedDtDesc();


    @Query(value = """
SELECT new com.hims.response.MasManufacturerResponse(
m.manufacturerId,
m.manufacturerName,
m.description,
i.name
)
FROM MasManufacturer m
LEFT JOIN m.itemType i
WHERE m.status=:activeStatus
AND 
(
:itemTypeCode IS NULL 
OR 
i.code=:itemTypeCode
)
ORDER BY m.manufacturerName ASC
""")
    Optional<List<MasManufacturerResponse>> getMasManufacturerWrtItemTypeCode(String itemTypeCode, String activeStatus);
}
