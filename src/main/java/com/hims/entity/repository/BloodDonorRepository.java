package com.hims.entity.repository;

import com.hims.entity.BloodDonor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface BloodDonorRepository extends JpaRepository<BloodDonor,Long> {

    @Query(value = """
    SELECT donor_code FROM blood_donor 
    WHERE donor_code LIKE CONCAT(:prefix, '%') 
    ORDER BY donor_code DESC LIMIT 1 
    """, nativeQuery = true)
    String findLastDonorCodeByPrefix(@Param("prefix") String prefix);

    boolean existsByMobileNoAndFirstNameAndDateOfBirthAndRelationAndBloodGroup_BloodGroupId(
            String mobileNo,
            String firstName,
            LocalDate dateOfBirth,
            String relation,
            Long bloodGroupId
    );
}
