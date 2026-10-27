package com.hims.service;


public interface LgdStateAndDistrictLookupService {

    String getLgdStateCode(String stateName);

    String getLgdDistrictCode(
            String districtName,
            String lgdStateCode
    );
}
