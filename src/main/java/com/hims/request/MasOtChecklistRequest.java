package com.hims.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MasOtChecklistRequest {
    private Long checklistId;

    @NotBlank
    @Pattern(regexp = "PRE_OP|INTRA_OP|POST_OP", message = "Stage must be PRE_OP, INTRA_OP or POST_OP")
    private String checklistStage;

    @NotBlank
    @Size(max = 50)
    private String checklistCode;

    @NotBlank
    @Size(max = 250)
    private String checklistName;

    @Pattern(regexp = "(?i)Y|N", message = "isMandatory must be Y or N")
    private String isMandatory = "N";

    @Pattern(regexp = "(?i)Y|N", message = "allowNa must be Y or N")
    private String allowNa = "N";

    @NotNull
    private Integer displayOrder;
}
