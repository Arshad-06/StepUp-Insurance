package com.infy.newgen.dto;

import java.util.List;

import lombok.Data;

@Data
public class CustomerResponseDTO {

    private Integer customerId;

    private String name;

    private String email;

    private String contact;

    private List<PolicyResponseDTO> policies;

}