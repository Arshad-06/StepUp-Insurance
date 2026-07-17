package com.infy.newgen.dto;

import com.infy.newgen.enums.Role;

import lombok.Data;

@Data
public class DisplayProfileDTO {

    private Integer customerId;
    private String name;
    private String email;
    private String contact;
    private Role role;
    private Integer agentId;
}