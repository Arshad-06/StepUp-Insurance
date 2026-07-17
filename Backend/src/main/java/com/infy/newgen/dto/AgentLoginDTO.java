package com.infy.newgen.dto;

import com.infy.newgen.enums.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AgentLoginDTO {

    @Email(message = "{agent.email.invalid}")
    @NotNull(message = "{agent.email.invalid}")
    private String email;

    @NotNull(message = "{agent.password.invalid}")
    private String password;

    @NotNull(message = "{agent.role.invalid}")
    private Role role;
}