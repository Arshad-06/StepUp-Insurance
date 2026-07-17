package com.infy.newgen.dto;

import com.infy.newgen.enums.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CustomerLoginDTO {

    @Email(message = "{customer.email.invalid}")
    @NotNull(message = "{customer.email.invalid}")
    private String email;

    @NotNull(message = "{customer.password.invalid}")
    private String password;

    @NotNull(message = "{customer.role.invalid}")
    private Role role;

}