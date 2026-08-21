package com.hims.response;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MasBrandResponse {

    private Long brandId;
    private String brandName;
    private String description;
    private String status;
    private String lastUpdatedBy;
    private LocalDateTime lastUpdatedDt;

}
