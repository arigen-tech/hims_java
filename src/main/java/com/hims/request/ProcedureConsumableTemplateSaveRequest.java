package com.hims.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcedureConsumableTemplateSaveRequest {


    @NotNull(message = "procedure_id is required")
    private Long procedureId;

    @NotBlank(message = "template_code is required")
    @Size(max = 20, message = "template_code max length is 20")
    private String templateCode;

    @NotBlank(message = "template_name is required")
    @Size(max = 200, message = "template_name max length is 200")
    private String templateName;

    @NotEmpty(message = "At least one consumable item detail is required")
    @Valid
    private List<TemplateDetailRequest> details;
}