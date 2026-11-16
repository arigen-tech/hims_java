package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasAsaGrade;
import com.hims.entity.repository.MasAsaGradeRepository;
import com.hims.response.ApiResponse;
import com.hims.service.MasAsaGradeService;
import com.hims.utils.ResponseUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MasAsaGradeServiceImpl implements MasAsaGradeService {

    private final MasAsaGradeRepository repository;

    public MasAsaGradeServiceImpl(MasAsaGradeRepository repository) {
        this.repository = repository;
    }

    @Override
    public ApiResponse<List<MasAsaGrade>> getAll(int flag, String gradeName, String gradeCode) {
        try {
            return ResponseUtils.createSuccessResponse(
                    repository.findGrades(flag, normalize(gradeName), normalize(gradeCode)), new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<Page<MasAsaGrade>> getAll(int flag, String gradeName, String gradeCode, int page, int size) {
        try {
            Page<MasAsaGrade> result = repository.findGrades(flag, normalize(gradeName), normalize(gradeCode),
                    PageRequest.of(page, size, Sort.unsorted()));
            return ResponseUtils.createSuccessResponse(result, new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<MasAsaGrade> getById(Long gradeId) {
        try {
            MasAsaGrade grade = repository.findGradeById(gradeId).orElse(null);
            if (grade == null) {
                return ResponseUtils.<MasAsaGrade>createNotFoundResponse("Grade ID not found", HttpStatus.NOT_FOUND.value());
            }
            return ResponseUtils.createSuccessResponse(grade, new TypeReference<MasAsaGrade>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
