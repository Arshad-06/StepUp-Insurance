package com.infy.newgen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponseDTO {
    private String token;
    private Integer userId;
    private String role;
    private String message;
    private String name;
}