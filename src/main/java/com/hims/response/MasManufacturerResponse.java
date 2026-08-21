package com.hims.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MasManufacturerResponse {

    private Long manufacturerId;
    private String manufacturerName;
    private String description;
    private String itemType;
}
