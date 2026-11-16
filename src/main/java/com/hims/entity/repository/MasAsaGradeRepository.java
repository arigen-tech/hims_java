package com.hims.entity.repository;

import com.hims.entity.MasAsaGrade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MasAsaGradeRepository extends JpaRepository<MasAsaGrade, Long> {

    @Query(value = """
            SELECT * FROM public.mas_asa_grade g
            WHERE (:flag <> 1 OR UPPER(g.status) = 'Y')
              AND (:gradeName IS NULL OR LOWER(g.grade_name) LIKE LOWER(CONCAT('%', :gradeName, '%')))
              AND (:gradeCode IS NULL OR LOWER(g.grade_code) LIKE LOWER(CONCAT('%', :gradeCode, '%')))
            ORDER BY g.grade_name ASC, g.grade_id ASC
            """, nativeQuery = true)
    List<MasAsaGrade> findGrades(@Param("flag") int flag,
                                  @Param("gradeName") String gradeName,
                                  @Param("gradeCode") String gradeCode);

    @Query(value = """
            SELECT * FROM public.mas_asa_grade g
            WHERE (:flag <> 1 OR UPPER(g.status) = 'Y')
              AND (:gradeName IS NULL OR LOWER(g.grade_name) LIKE LOWER(CONCAT('%', :gradeName, '%')))
              AND (:gradeCode IS NULL OR LOWER(g.grade_code) LIKE LOWER(CONCAT('%', :gradeCode, '%')))
            ORDER BY g.grade_name ASC, g.grade_id ASC
            """,
            countQuery = """
            SELECT COUNT(*) FROM public.mas_asa_grade g
            WHERE (:flag <> 1 OR UPPER(g.status) = 'Y')
              AND (:gradeName IS NULL OR LOWER(g.grade_name) LIKE LOWER(CONCAT('%', :gradeName, '%')))
              AND (:gradeCode IS NULL OR LOWER(g.grade_code) LIKE LOWER(CONCAT('%', :gradeCode, '%')))
            """, nativeQuery = true)
    Page<MasAsaGrade> findGrades(@Param("flag") int flag,
                                  @Param("gradeName") String gradeName,
                                  @Param("gradeCode") String gradeCode,
                                  Pageable pageable);

    @Query(value = "SELECT * FROM public.mas_asa_grade WHERE grade_id = :gradeId", nativeQuery = true)
    Optional<MasAsaGrade> findGradeById(@Param("gradeId") Long gradeId);
}
