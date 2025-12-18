package com.hims.entity.repository;

import com.hims.entity.MasMealType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasMealTypeRepository extends JpaRepository<MasMealType,Long> {
    List<MasMealType> findByStatusIgnoreCase(String y);

    List<MasMealType> findByStatusIgnoreCaseOrderByMealTypeNameAsc(String y);

   // List<MasMealType> findAllOrderByLastUpdateDateDesc();

    List<MasMealType> findAllByOrderByLastUpdateDateDesc();

    List<MasMealType> findAllByOrderByStatusDescLastUpdateDateDesc();
}
