package com.hims.entity.repository;
import com.hims.entity.OtPacDocument; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import java.util.*;
public interface OtPacDocumentRepository extends JpaRepository<OtPacDocument,Long> {
 @Query(value="SELECT * FROM public.ot_pac_document WHERE pac_assessment_id=:id AND status='Y' ORDER BY pac_document_id",nativeQuery=true) List<OtPacDocument> findNativeByAssessmentId(@Param("id") Long id);
}
