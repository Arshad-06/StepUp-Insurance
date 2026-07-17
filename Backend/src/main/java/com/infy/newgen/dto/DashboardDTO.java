package com.infy.newgen.dto;

import java.util.List;

import lombok.Data;

@Data
public class DashboardDTO {
    private Integer totalCustomers;

    private Long totalPoliciesCurrentYear;

    private Double annualProfit;

    private List<CustomerResponseDTO> customers;

}