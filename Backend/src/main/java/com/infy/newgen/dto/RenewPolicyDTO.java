package com.infy.newgen.dto;

import com.infy.newgen.enums.PolicyStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RenewPolicyDTO {
    @NotBlank(message = "{policy.id.invalid}")
    private String policyId;

    @NotNull(message = "{policy.status.invalid}")
    private PolicyStatus policyStatus;

    @Pattern(regexp = "\\d{16}", message = "{payment.cardNumber.invalid}")
    @NotNull(message = "{payment.cardNumber.invalid}")
    private String cardNumber;

    @Pattern(regexp = "\\d{3}", message = "{payment.cvv.invalid}")
    @NotNull(message = "{payment.cvv.invalid}")
    private String cvv;

}