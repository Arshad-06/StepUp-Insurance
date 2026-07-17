package com.infy.newgen.entity;

import java.time.LocalDate;

import com.infy.newgen.enums.PolicyStatus;
import com.infy.newgen.enums.PolicyType;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class Policy {
    @Id
    private String policyId;

    @Enumerated(EnumType.STRING)
    private PolicyType policyType;

    private LocalDate policyStartDate;

    private LocalDate policyEndDate;

    private LocalDate lastPremiumPaymentDate;

    private Double premiumAmount;

    @Enumerated(EnumType.STRING)
    private PolicyStatus policyStatus;

    private String nominee;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;
}