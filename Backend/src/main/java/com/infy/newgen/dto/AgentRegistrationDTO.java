package com.infy.newgen.dto;

import com.infy.newgen.enums.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AgentRegistrationDTO {

    @Pattern(regexp = "^[A-Z][A-Za-z]+( [A-Z][A-Za-z]+){0,2}$", message = "{agent.name.invalid}")
    @NotNull(message = "{agent.name.invalid}")
    private String name;

    @Email(message = "{agent.email.invalid}")
    @NotNull(message = "{agent.email.invalid}")
    private String email;

    @Pattern(regexp = "^[6789]\\d{9}$", message = "{agent.contact.invalid}")
    @NotNull(message = "{agent.contact.invalid}")
    private String contact;

    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&*+=!]).{8,}$", message = "{agent.password.invalid}")
    @NotNull(message = "{agent.password.invalid}")
    private String password;

    @NotNull(message = "{agent.role.invalid}")
    private Role role;

}
