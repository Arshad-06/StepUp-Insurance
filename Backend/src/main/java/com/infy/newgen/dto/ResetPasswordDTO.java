package com.infy.newgen.dto;

import lombok.Data;

@Data

public class ResetPasswordDTO {

    private String email;

    private String resetToken;

    private String newPassword;

    private String role;

}