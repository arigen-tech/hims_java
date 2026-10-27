package com.hims.response;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class LgdDistrictsResponse {

    private Boolean success;
    private List<LgdDistrict> data;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LgdDistrict {

        private String code;
        private String name;
    }
}
