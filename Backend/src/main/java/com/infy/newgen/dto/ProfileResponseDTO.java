package com.infy.newgen.dto;

import lombok.Data;

@Data

public class ProfileResponseDTO {

    private String name;

    private String email;

    private String contact;

    private String role;

    private String token;

    private Integer customerId;

    private Integer agentId;

}