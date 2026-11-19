package com.hims.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class OtChecklistSubmitRequest {
    @NotNull
    private Long otBookingId;

    @NotNull
    @Pattern(regexp = "(?i)PRE_OP|INTRA_OP|POST_OP", message = "checklistStage must be PRE_OP, INTRA_OP or POST_OP")
    private String checklistStage;

    @NotNull
    @Pattern(regexp = "(?i)Y|D", message = "checklistStatus must be Y (Complete) or D (Draft)")
    private String checklistStatus;

    @Size(max = 4000)
    private String remarks;

    @NotNull
    @Valid
    private List<OtChecklistItemSubmitRequest> items;

    @Data
    public static class OtChecklistItemSubmitRequest {
        @NotNull
        private Long checklistId;

        @Pattern(regexp = "(?i)YES|NO|NA", message = "verificationStatus must be YES, NO or NA")
        private String verificationStatus;

        @Size(max = 500)
        private String remarks;
    }
}
