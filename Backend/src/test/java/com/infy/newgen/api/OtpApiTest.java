package com.infy.newgen.api;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infy.newgen.dto.OTPVerificationDTO;
import com.infy.newgen.entity.Agent;
import com.infy.newgen.entity.Customer;
import com.infy.newgen.exception.NewGenException;
import com.infy.newgen.repository.AgentRepository;
import com.infy.newgen.repository.CustomerRepository;
import com.infy.newgen.service.EmailService;
import com.infy.newgen.service.OtpService;
import com.infy.newgen.utility.JwtUtil;

import jakarta.validation.ConstraintViolationException;

@WebMvcTest(controllers = OtpAPI.class, excludeAutoConfiguration = { SecurityAutoConfiguration.class })
@AutoConfigureMockMvc(addFilters = false) // Bypass Security Filters For Testing
class OtpApiTest {
    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private OtpService otpService;

    @MockitoBean
    private EmailService emailService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private AgentRepository agentRepository;

    @MockitoBean
    private CustomerRepository customerRepository;

    // Generate OTP Tests

    @Test
    @DisplayName("Generate OTP - Success")
    void generateOtpSuccessTest() throws Exception {
        String email = "customer@test.com";
        String generatedOtp = "1234";

        Mockito.when(otpService.generateOtp(email)).thenReturn(generatedOtp);
        Mockito.doNothing().when(emailService).sendOtp(any(OTPVerificationDTO.class));

        mockMvc.perform(get("/otp/generate-otp/{email}", email))
                .andExpect(status().isOk())
                .andExpect(content().string("Generated OTP for email" + ":" + email));

        Mockito.verify(emailService, Mockito.times(1)).sendOtp(any(OTPVerificationDTO.class));
    }

    @Test
    @DisplayName("Generate OTP - Failure (Invalid Format Email Passed)")
    void generateOtpFailureTest1() throws Exception {
        String email = "invalidemail.com";
        mockMvc.perform(get("/otp/generate-otp/{email}", email))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(result -> assertTrue(
                        result.getResolvedException().getMessage().contains("Please provide a valid email")));
        // Verify that OTP was never sent since generation failed
        Mockito.verify(emailService, Mockito.never()).sendOtp(any());
    }

    @Test
    @DisplayName("Generate OTP - Failure (Blank Email Passed)")
    void generateOtpFailureTest2() throws Exception {
        String email = " ";
        mockMvc.perform(get("/otp/generate-otp/{email}", email))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(result -> assertTrue(
                        result.getResolvedException().getMessage().contains("Please provide a valid email")));
        // Verify that OTP was never sent since generation failed
        Mockito.verify(emailService, Mockito.never()).sendOtp(any());
    }

    // Verify OTP Test

    @Test
    @DisplayName("Verify OTP - Success")
    void verifyOtpSuccessTest() throws Exception {
        OTPVerificationDTO dto = new OTPVerificationDTO();
        dto.setEmail("customer@test.com");
        dto.setOtp("1234");

        Mockito.when(otpService.verifyOtp(dto.getEmail(), dto.getOtp())).thenReturn(true);

        Mockito.when(jwtUtil.generateResetToken(dto.getEmail())).thenReturn("dummy-token");

        mockMvc.perform(
                post("/otp/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("dummy-token"));
    }

    @Test
    @DisplayName("Verify OTP - Failure (Invalid OTP)")
    void verifyOtpFailureTest1() throws Exception {
        OTPVerificationDTO dto = new OTPVerificationDTO();
        dto.setEmail("customer@test.com");
        dto.setOtp("123456"); // Invalid OTP

        Mockito.when(otpService.verifyOtp(dto.getEmail(), dto.getOtp()))
                .thenThrow(new NewGenException("Service.OTP_INVALID"));

        mockMvc.perform(
                post("/otp/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof NewGenException))
                .andExpect(result -> assertTrue(
                        result.getResolvedException().getMessage().contains("Service.OTP_INVALID")));

    }

    @Test
    @DisplayName("Verify OTP - Failure (Null OTP Provided)")
    void verifyOtpFailureTest2() throws Exception {
        OTPVerificationDTO dto = new OTPVerificationDTO();
        dto.setEmail("customer@test.com");

        mockMvc.perform(
                post("/otp/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(
                        result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(result -> assertTrue(
                        result.getResolvedException().getMessage().contains("Please provide a valid otp")));

    }

    @Test
    @DisplayName("Verify OTP - Failure (OTP Does Not Exist)")
    void verifyOtpFailureTest3() throws Exception {
        OTPVerificationDTO dto = new OTPVerificationDTO();
        dto.setEmail("customer@test.com");
        dto.setOtp("123456"); // Not Existing In OTP HashMap

        Mockito.when(otpService.verifyOtp(dto.getEmail(), dto.getOtp()))
                .thenThrow(new NewGenException("Service.OTP_NOT_FOUND"));

        mockMvc.perform(
                post("/otp/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof NewGenException))
                .andExpect(result -> assertTrue(
                        result.getResolvedException().getMessage().contains("Service.OTP_NOT_FOUND")));

    }

    @Test
    @DisplayName("CheckEmailExists - Returns True (Agent Found)")
    void checkEmailExistsTestAgentFound() throws Exception {
        String email = "agent@test.com";
        Mockito.when(agentRepository.findByEmail(email)).thenReturn(Optional.of(new Agent()));

        mockMvc.perform(
                get("/otp/check-email")
                        .param("email", email)
                        .param("type", "a"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("CheckEmailExists - Returns False (Agent Not Found)")
    void checkEmailExistsTestAgentNotFound() throws Exception {
        String email = "notfound@agent.com";
        Mockito.when(agentRepository.findByEmail(email)).thenReturn(Optional.empty());

        mockMvc.perform(
                get("/otp/check-email")
                        .param("email", email)
                        .param("type", "a"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    @DisplayName("CheckEmailExists - Returns True (Customer Found)")
    void checkEmailExistsTestCustomerFound() throws Exception {
        String email = "customer@test.com";
        Mockito.when(customerRepository.findByEmail(email)).thenReturn(Optional.of(new Customer()));

        mockMvc.perform(
                get("/otp/check-email")
                        .param("email", email)
                        .param("type", "c"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("CheckEmailExists - Returns False (Customer Not Found)")
    void checkEmailExistsTestCustomerNotFound() throws Exception {
        String email = "notfound@customer.com";
        Mockito.when(customerRepository.findByEmail(email)).thenReturn(Optional.empty());

        mockMvc.perform(
                get("/otp/check-email")
                        .param("email", email)
                        .param("type", "c"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    @DisplayName("CheckEmailExists - Returns True (Agent Found)")
    void checkEmailExistsTestInvalidType() throws Exception {
        String email = "agent@test.com";

        mockMvc.perform(
                get("/otp/check-email")
                        .param("email", email)
                        .param("type", "b"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }
}