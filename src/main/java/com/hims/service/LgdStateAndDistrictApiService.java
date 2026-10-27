package com.hims.service;
import com.hims.response.LgdDistrictsResponse;
import com.hims.response.LgdStatesResponse;

public interface LgdStateAndDistrictApiService {


    LgdStatesResponse getAllLgdStates();

    void refreshLgdStatesCache();

    LgdDistrictsResponse getDistrictsByLgdStateCode(String lgdStateCode);

    void refreshDistrictsCache(String lgdStateCode);
}