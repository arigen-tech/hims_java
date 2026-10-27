package com.hims.service.impl;


import com.hims.response.LgdDistrictsResponse;
import com.hims.response.LgdStatesResponse;
import com.hims.service.LgdStateAndDistrictApiService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class LgdStateAndDistrictAndDistrictApiServiceImpl implements LgdStateAndDistrictApiService {

    private final RestClient restClient;

    public LgdStateAndDistrictAndDistrictApiServiceImpl() {
        this.restClient = RestClient.builder()
                .baseUrl("https://india-lgd-index-api.vercel.app")
                .build();
    }

    @Override
    @Cacheable(value = "lgdStates", key = "'allStates'")
    public LgdStatesResponse getAllLgdStates() {

        LgdStatesResponse response = restClient.get()
                .uri("/v1/states")
                .retrieve()
                .body(LgdStatesResponse.class);

        if (response == null
                || !Boolean.TRUE.equals(response.getSuccess())
                || response.getData() == null) {

            throw new IllegalStateException(
                    "Unable to retrieve LGD state data"
            );
        }

        return response;
    }

    @Override
    @CacheEvict(value = "lgdStates", key = "'allStates'")
    public void refreshLgdStatesCache() {
        // Evicts the cached state list.
        // The next call to getAllLgdStates() fetches fresh data.
    }

    @Override
    @Cacheable(
            value = "lgdDistricts",
            key = "'state_' + #lgdStateCode"
    )
    public LgdDistrictsResponse getDistrictsByLgdStateCode(
            String lgdStateCode) {

        if (lgdStateCode == null || lgdStateCode.isBlank()) {
            throw new IllegalArgumentException(
                    "LGD state code is required"
            );
        }

        LgdDistrictsResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/districts")
                        .queryParam("state", lgdStateCode)
                        .build())
                .retrieve()
                .body(LgdDistrictsResponse.class);

        if (response == null
                || !Boolean.TRUE.equals(response.getSuccess())
                || response.getData() == null) {

            throw new IllegalStateException(
                    "Unable to retrieve LGD districts for state code: "
                            + lgdStateCode
            );
        }

        return response;
    }

    @Override
    @CacheEvict(
            value = "lgdDistricts",
            key = "'state_' + #lgdStateCode"
    )
    public void refreshDistrictsCache(String lgdStateCode) {
        // The next lookup fetches fresh district data.
    }


}