package com.hims.request;

import lombok.Data;

@Data
public class MasWardRequest {
            // for update, null for create

    private String wardName;
    private Long wardCategoryId;
    private Long careLevelId;

}
