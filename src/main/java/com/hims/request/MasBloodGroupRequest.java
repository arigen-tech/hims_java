package com.hims.request;

import lombok.Data;

import java.time.Instant;

@Data
public class MasBloodGroupRequest {
    private String bloodGroupCode; // Code for the blood group
    private String bloodGroupName; // Name of the blood group
   // private String hicCode;       // HIC code for the blood group
}