package com.hims.request;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
public class MasManufacturerRequest {
    private String manufacturerName;
    private String description;
    private String address;
    private String contactNumber;
    private String email;

}
