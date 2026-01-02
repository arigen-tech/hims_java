package com.hims.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ObMasCervixPositionRequest {

    @NotBlank(message = "Cervix position is required")
    private String cervixPosition;
}
