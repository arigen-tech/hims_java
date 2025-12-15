package com.hims.request;

import lombok.Data;
import org.springframework.stereotype.Repository;

@Data
public class MasSpecialtyCenterRequest {
    private String centerName;
    private String description;
}
