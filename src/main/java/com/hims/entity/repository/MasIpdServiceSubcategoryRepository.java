package com.hims.entity.repository;

import com.hims.entity.MasIpdServiceSubcategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MasIpdServiceSubcategoryRepository extends JpaRepository<MasIpdServiceSubcategory, Long> {

    Optional<MasIpdServiceSubcategory> findBySubcategoryCode(String subcategoryCode);
}
