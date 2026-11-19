package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MasBloodDiscardReasonRes {

    private Long reasonId;
    private String reasonCode;
    private String reasonName;
    private String description;
}
