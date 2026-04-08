package com.hims.request;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SaveComponentGenerationRequest {
    private Long donationId;

    private List<ComponentGenerationRequest> components;
}