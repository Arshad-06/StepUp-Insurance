package com.infy.newgen.dto;

import com.infy.newgen.enums.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CustomerRegistrationDTO {

    @Pattern(regexp = "^[A-Z][A-Za-z]+( [A-Z][A-Za-z]+){0,2}$", message = "{customer.name.invalid}")
    @NotNull(message = "{customer.name.invalid}")
    private String name;

    @Email(message = "{customer.email.invalid}")
    @NotNull(message = "{customer.email.invalid}")
    private String email;

    @Pattern(regexp = "^[6789]\\d{9}$", message = "{customer.contact.invalid}")
    @NotNull(message = "{customer.contact.invalid}")
    private String contact;

    @NotNull(message = "{customer.role.invalid}")
    private Role role;

    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$", message = "{customer.password.invalid}")
    @NotNull(message = "{customer.password.invalid}")
    private String password;

    @NotNull(message = "{customer.agentId.invalid}")
    private Integer agentId;
}