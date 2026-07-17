package com.infy.newgen.dto;

import java.time.LocalDate;

import com.infy.newgen.enums.PolicyStatus;
import com.infy.newgen.enums.PolicyType;

import lombok.Data;

@Data
public class PolicyResponseDTO {

    private String policyId;

    private PolicyType policyType;

    private LocalDate policyStartDate;

    private LocalDate policyEndDate;

    private LocalDate lastPremiumPaymentDate;

    private Double premiumAmount;

    private PolicyStatus policyStatus;

    private String nominee;

}