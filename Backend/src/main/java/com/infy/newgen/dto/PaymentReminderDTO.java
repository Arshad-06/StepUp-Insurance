package com.infy.newgen.dto;

import com.infy.newgen.enums.PolicyType;

import lombok.Data;

@Data
public class PaymentReminderDTO {
    private String customerEmail;
    private String customerName;
    private PolicyType policyType;
    private String policyId;
}