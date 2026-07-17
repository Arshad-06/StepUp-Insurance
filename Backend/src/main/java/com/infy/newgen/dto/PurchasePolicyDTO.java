package com.infy.newgen.dto;

import java.time.LocalDate;

import com.infy.newgen.enums.PolicyType;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PurchasePolicyDTO {

    @NotNull(message = "{policy.type.invalid}")
    private PolicyType policyType;

    @FutureOrPresent(message = "{policy.startDate.invalid}")
    @NotNull(message = "{policy.startDate.invalid}")
    private LocalDate policyStartDate;

    @NotNull(message = "{policy.term.invalid}")
    private Integer policyTerm;

    @Pattern(regexp = "^[A-Z][A-Za-z]+( [A-Z][A-Za-z]+){0,2}$", message = "{agent.name.invalid}")
    @NotNull(message = "{agent.name.invalid}")
    private String nominee;

    @NotNull(message = "{policy.premiumAmount.invalid}")
    private Integer premiumAmount;

}