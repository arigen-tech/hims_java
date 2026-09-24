package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.MasResultFlag;
import com.hims.entity.repository.MasResultFlagRepository;
import com.hims.helperUtil.ResponseUtils;
import com.hims.response.ApiResponse;
import com.hims.response.MasResultFlagResponse;
import com.hims.service.ResultFlagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.hims.constants.AppConstants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResultFlagServiceImpl implements ResultFlagService {

    private final MasResultFlagRepository masResultFlagRepository;




    @Override
    public ApiResponse<List<MasResultFlagResponse>> getResultFlagDropdown() {
        return ResponseUtils.createSuccessResponse(masResultFlagRepository.findByStatus("Y")
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList()),
                new TypeReference<>() {}
        );
    }

    // ---------------------------------------------------------------------
    // Detect flag
    // ---------------------------------------------------------------------
    @Override
    public ApiResponse<MasResultFlagResponse> detectResultFlag(String result, String normalRange) {
        if (result == null || result.trim().isEmpty()) return null;
        if (normalRange == null || normalRange.trim().isEmpty()) return null;

        String flagCode = calculateFlag(result.trim(), normalRange.trim());
        if (flagCode == null) return null;

        MasResultFlag flag = masResultFlagRepository.findByFlagCode(flagCode);
        return flag != null ?
                ResponseUtils.createSuccessResponse(toResponse(flag),
                        new TypeReference<>() {}
                ) :
                ResponseUtils.createFailureResponse(null,
                        new TypeReference<>() {},
                        INTERNAL_SERVER_ERR_MSG,
                        HttpStatus.INTERNAL_SERVER_ERROR.value()
                );

    }

    // ---------------------------------------------------------------------
    // Core calculation — handles single OR multi-valued result
    // ---------------------------------------------------------------------
    private String calculateFlag(String result, String normalRange) {
        // If the result contains multiple values, evaluate each and take the worst
        if (result.matches(".*" + MULTI_VALUE_REGEX + ".*")) {
            return calculateForMultiple(result, normalRange);
        }
        return calculateForSingle(result, normalRange);
    }

    // ---------------------------------------------------------------------
    // SINGLE value
    // ---------------------------------------------------------------------
    private String calculateForSingle(String result, String normalRange) {

        // 1. Exact text match
        if (result.equalsIgnoreCase(normalRange)) {
            return NORMAL;
        }

        // 2. Numeric range "min - max"
        if (normalRange.matches(NUMERIC_RANGE_REGEX)) {
            String[] parts = normalRange.split("\\s*-\\s*");
            try {
                double min = Double.parseDouble(parts[0]);
                double max = Double.parseDouble(parts[1]);
                double val = Double.parseDouble(result);
                if (val < min) return LOW;
                if (val > max) return HIGH;
                return NORMAL;
            } catch (NumberFormatException ex) {
                return ABNORMAL;
            }
        }

        // 3. Threshold ">=x", "<=x", ">x", "<x"
        if (normalRange.matches(THRESHOLD_REGEX)) {
            String op = normalRange.replaceAll("[0-9.\\-\\s]", "").trim();
            double threshold = Double.parseDouble(normalRange.replaceAll("[^0-9.\\-]", ""));
            try {
                double val = Double.parseDouble(result);
                boolean normal;
                switch (op) {
                    case ">=": normal = val >= threshold; break;
                    case "<=": normal = val <= threshold; break;
                    case ">":  normal = val >  threshold; break;
                    case "<":  normal = val <  threshold; break;
                    default:   return null;
                }
                if (normal) return NORMAL;
                return op.startsWith(">") ? LOW : HIGH;
            } catch (NumberFormatException ex) {
                return ABNORMAL;
            }
        }

        // 4. Non-matching text / dropdown value
        return ABNORMAL;
    }

    // ---------------------------------------------------------------------
    // MULTIPLE values
    // ---------------------------------------------------------------------
    private String calculateForMultiple(String result, String normalRange) {
        List<String> tokens = splitValues(result);
        if (tokens.isEmpty()) return null;

        boolean isRange  = normalRange.matches(NUMERIC_RANGE_REGEX);
        boolean isThresh = normalRange.matches(THRESHOLD_REGEX);

        // Case A: normalRange itself is a list of accepted text values
        if (!isRange && !isThresh && normalRange.matches(".*" + MULTI_VALUE_REGEX + ".*")) {
            Set<String> normalSet = splitValues(normalRange).stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet());

            boolean allNormal = tokens.stream()
                    .allMatch(t -> normalSet.contains(t.toLowerCase()));
            return allNormal ? NORMAL : ABNORMAL;
        }

        // Case B: each token compared against the same range / threshold
        String finalFlag = null;
        for (String token : tokens) {
            String flag = calculateForSingle(token, normalRange);
            finalFlag = worst(finalFlag, flag);
        }
        return finalFlag;
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------
    private List<String> splitValues(String value) {
        return Arrays.stream(value.split(MULTI_VALUE_REGEX))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    /** Worst-case wins: ABNORMAL > HIGH > LOW > NORMAL */
    private String worst(String a, String b) {
        if (a == null) return b;
        if (b == null) return a;
        return priority(a) >= priority(b) ? a : b;
    }

    private int priority(String flagCode) {
        switch (flagCode) {
            case ABNORMAL: return 4;
            case HIGH:     return 3;
            case LOW:      return 2;
            case NORMAL:   return 1;
            default:       return 0;
        }
    }

    private MasResultFlagResponse toResponse(MasResultFlag e) {
        return new MasResultFlagResponse(
                e.getResultFlagId(),
                e.getFlagCode(),
                e.getFlagName(),
                e.getDescription()
        );
    }
}
