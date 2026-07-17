package com.infy.newgen.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OTPVerificationDTO {

    @NotBlank(message = "{email.invalid}")
    private String email;

    @NotBlank(message = "{otp.invalid}")
    private String otp;
}