package com.infy.newgen.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.infy.newgen.dto.OTPVerificationDTO;
import com.infy.newgen.exception.NewGenException;
import com.infy.newgen.repository.AgentRepository;
import com.infy.newgen.repository.CustomerRepository;
import com.infy.newgen.service.EmailService;
import com.infy.newgen.service.OtpService;
import com.infy.newgen.utility.JwtUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/otp")
@CrossOrigin(origins = "https://step-up-insurance.vercel.app/")
@Validated
public class OtpAPI {
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private OtpService otpService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private Environment environment;
    @Autowired
    private AgentRepository agentRepository;
    @Autowired
    private CustomerRepository customerRepository;

    @Operation(summary = "Generates & Sends OTP To Email")
    @GetMapping("/generate-otp/{email}")
    @ApiResponse(responseCode = "200", description = "OTP Generated & Sent Successfully")
    @ApiResponse(responseCode = "400", description = "Error In Data Validation")
    public ResponseEntity<String> generateOtp(
            @Email(message = "{email.invalid}") @NotBlank(message = "{email.invalid}") @PathVariable String email)
            throws NewGenException {
        OTPVerificationDTO o = new OTPVerificationDTO();
        o.setEmail(email);
        o.setOtp(otpService.generateOtp(email));

        emailService.sendOtp(o);

        String successMessage = environment.getProperty("Service.OTP_GENERATED_SUCCESS") + ":" + email;
        return new ResponseEntity<>(successMessage, HttpStatus.OK);
    }

    @Operation(summary = "Verifies Given OTP")
    @PostMapping("/verify-otp")
    @ApiResponse(responseCode = "200", description = "OTP Verified Successfully")
    @ApiResponse(responseCode = "400", description = "Error In Data Validation")
    public ResponseEntity<String> verifyOtp(@Valid @RequestBody OTPVerificationDTO dto) throws NewGenException {
        otpService.verifyOtp(dto.getEmail(), dto.getOtp());
        String resetToken = jwtUtil.generateResetToken(dto.getEmail());
        return new ResponseEntity<>(resetToken, HttpStatus.OK);
    }

    @Operation(summary = "Checks User Email Existence Globally")
    @GetMapping("/check-email")
    @ApiResponse(responseCode = "200", description = "User Email Is Valid")
    @ApiResponse(responseCode = "400", description = "Error In Data Validation")
    public ResponseEntity<Boolean> checkEmailExists(@RequestParam String email, @RequestParam String type)
            throws NewGenException {
        Boolean exists = false;
        if ("a".equalsIgnoreCase(type)) {
            exists = agentRepository.findByEmail(email).isPresent();
        } else if ("c".equalsIgnoreCase(type)) {
            exists = customerRepository.findByEmail(email).isPresent();
        }
        return new ResponseEntity<>(exists, HttpStatus.OK);
    }
}