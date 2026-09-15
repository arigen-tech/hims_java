package com.hims.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {

//    @NotNull
//    @Positive
//    private Long amount;
//
//    @NotNull
//    private Long billingHdId;
@NotEmpty
@Valid
private List<BillingItemRequest> billingItems;

}
