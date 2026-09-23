package com.hims.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MasResultFlagResponse {
    private Long resultFlagId;
    private String flagCode;
    private String flagName;
    private String description;
}