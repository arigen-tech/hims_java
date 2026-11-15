package com.hims.entity.repository;
import com.hims.entity.OtPacInstruction; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import java.util.*;
public interface OtPacInstructionRepository extends JpaRepository<OtPacInstruction,Long> {
 @Query(value="SELECT * FROM public.ot_pac_instruction WHERE pac_assessment_id=:id ORDER BY pac_instruction_id",nativeQuery=true) List<OtPacInstruction> findNativeByAssessmentId(@Param("id") Long id);
 void deleteByPacAssessment_PacAssessmentId(Long id);
}
