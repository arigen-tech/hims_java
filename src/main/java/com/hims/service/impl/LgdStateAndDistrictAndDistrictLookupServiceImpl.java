package com.hims.service.impl;


import com.hims.exception.SDDException;
import com.hims.response.LgdDistrictsResponse;
import com.hims.response.LgdStatesResponse;
import com.hims.service.LgdStateAndDistrictApiService;
import com.hims.service.LgdStateAndDistrictLookupService;
import kong.unirest.HttpStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class LgdStateAndDistrictAndDistrictLookupServiceImpl
        implements LgdStateAndDistrictLookupService {

    private final LgdStateAndDistrictApiService lgdStateAndDistrictApiService;

    @Override
    public String getLgdStateCode(String stateName) {

        if (stateName == null || stateName.isBlank()) {
            throw new IllegalArgumentException(
                    "State name is required"
            );
        }

        LgdStatesResponse response =
                lgdStateAndDistrictApiService.getAllLgdStates();

        String normalizedName = normalize(stateName);

        return response.getData()
                .stream()
                .filter(state ->
                        normalize(state.getName())
                                .equals(normalizedName))
                .map(LgdStatesResponse.LgdState::getCode)
                .filter(code -> code != null && !code.isBlank())
                .findFirst()
                .orElseThrow(() ->
                        new SDDException("LGD State",
                                HttpStatus.BAD_REQUEST,
                                "LGD state not found: " + stateName
                        ));
    }

    @Override
    public String getLgdDistrictCode(
            String districtName,
            String lgdStateCode) {

        if (districtName == null || districtName.isBlank()) {
            throw new IllegalArgumentException(
                    "District name is required"
            );
        }

        if (lgdStateCode == null || lgdStateCode.isBlank()) {
            throw new IllegalArgumentException(
                    "LGD state code is required for district lookup"
            );
        }

        LgdDistrictsResponse response =
                lgdStateAndDistrictApiService.getDistrictsByLgdStateCode(
                        lgdStateCode
                );

        String normalizedName = normalize(districtName);

        return response.getData()
                .stream()
                .filter(district ->
                        normalize(district.getName())
                                .equals(normalizedName))
                .map(LgdDistrictsResponse.LgdDistrict::getCode)
                .filter(code -> code != null && !code.isBlank())
                .findFirst()
                .orElseThrow(() ->
                                new SDDException("LGD District",
                                        HttpStatus.BAD_REQUEST,
                                "LGD district not found: "
                                        + districtName
                                        + " for LGD state code: "
                                        + lgdStateCode
                        ));
    }


    private String normalize(String value) {
        return value == null
                ? ""
                : value.trim()
                .replaceAll("\\s+", " ")
                .toUpperCase(Locale.ROOT);
    }
}