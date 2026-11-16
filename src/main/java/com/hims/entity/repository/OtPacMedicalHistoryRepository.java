package com.hims.entity.repository;
import com.hims.entity.OtPacMedicalHistory; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import java.util.*;
public interface OtPacMedicalHistoryRepository extends JpaRepository<OtPacMedicalHistory,Long> {
 @Query(value="SELECT * FROM public.ot_pac_medical_history WHERE pac_assessment_id=:id ORDER BY pac_medical_history_id",nativeQuery=true) List<OtPacMedicalHistory> findNativeByAssessmentId(@Param("id") Long id);
 void deleteByPacAssessment_PacAssessmentId(Long id);
}
