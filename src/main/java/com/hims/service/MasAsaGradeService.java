package com.hims.service;

import com.hims.entity.MasAsaGrade;
import com.hims.response.ApiResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface MasAsaGradeService {
    ApiResponse<List<MasAsaGrade>> getAll(int flag, String gradeName, String gradeCode);
    ApiResponse<Page<MasAsaGrade>> getAll(int flag, String gradeName, String gradeCode, int page, int size);
    ApiResponse<MasAsaGrade> getById(Long gradeId);
}
