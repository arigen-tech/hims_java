package com.hims.service;

import com.hims.response.ApiResponse;
import com.hims.response.MasResultFlagResponse;

import java.util.List;

public interface ResultFlagService {

    /** Loads active result flag master rows for the UI dropdown. */
    ApiResponse<List<MasResultFlagResponse>> getResultFlagDropdown();

    /**
     * Detects the correct flag for a given result.
     *
     * @param result      value entered by the user (single or comma/semicolon separated)
     * @param normalRange normal range / reference text from the master
     * @return matched flag (Normal / Low / High / Abnormal), or null when it cannot be determined
     */
    ApiResponse<MasResultFlagResponse> detectResultFlag(String result, String normalRange);
}
