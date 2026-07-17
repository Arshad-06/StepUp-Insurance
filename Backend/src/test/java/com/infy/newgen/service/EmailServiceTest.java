package com.infy.newgen.service;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import com.infy.newgen.dto.OTPVerificationDTO;
import com.infy.newgen.dto.PaymentReminderDTO;
import com.infy.newgen.enums.PolicyType;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @InjectMocks
    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        // Inject the @Value fields that Spring would normally inject in production
        ReflectionTestUtils.setField(emailService, "fromEmail", "test-sender@stepup.com");
        ReflectionTestUtils.setField(emailService, "sendGridApiKey", "SG.mock_api_key");
        
        // Inject our mocked RestClient into the service field instance
        ReflectionTestUtils.setField(emailService, "restClient", restClient);
    }

    /**
     * Helper method to stub the fluent RestClient builder chain
     */
    private void mockRestClientChain() {
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any(MediaType.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    @SuppressWarnings("unchecked")
    void sendOtpSuccess() {
        // Arrange
        OTPVerificationDTO dto = new OTPVerificationDTO();
        dto.setEmail("arshadhm200@gmail.com");
        dto.setOtp("1234");

        mockRestClientChain();

        // Act
        emailService.sendOtp(dto);

        // Assert and Capture the exact JSON payload sent to SendGrid
        ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.of().getClass());
        verify(requestBodySpec, times(1)).body(payloadCaptor.capture());

        Map<String, Object> capturedPayload = payloadCaptor.getValue();
        
        // Verify Top-Level Structure
        assertEquals("StepUp Insurance OTP Verification", capturedPayload.get("subject"));
        
        // Verify Sender mapping
        Map<String, String> fromMap = (Map<String, String>) capturedPayload.get("from");
        assertEquals("test-sender@stepup.com", fromMap.get("email"));

        // Verify Recipient mapping
        List<Map<String, Object>> personalizations = (List<Map<String, Object>>) capturedPayload.get("personalizations");
        List<Map<String, String>> toList = (List<Map<String, String>>) personalizations.get(0).get("to");
        assertEquals("arshadhm200@gmail.com", toList.get(0).get("email"));

        // Verify Content mapping
        List<Map<String, String>> contentList = (List<Map<String, String>>) capturedPayload.get("content");
        String textBody = contentList.get(0).get("value");
        assertTrue(textBody.contains("Your OTP For Login Is : 1234"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void sendPaymentReminderSuccess() {
        // Arrange
        PaymentReminderDTO dto = new PaymentReminderDTO();
        dto.setCustomerEmail("arshaddpvt@gmail.com");
        dto.setCustomerName("Arshad");
        dto.setPolicyId("POL-123");
        dto.setPolicyType(PolicyType.HEALTH_INSURANCE);

        mockRestClientChain();

        // Act
        emailService.sendPaymentReminder(dto);

        // Assert and Capture
        ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.of().getClass());
        verify(requestBodySpec, times(1)).body(payloadCaptor.capture());

        Map<String, Object> capturedPayload = payloadCaptor.getValue();
        
        assertEquals("StepUp Insurance Payment Reminder", capturedPayload.get("subject"));

        List<Map<String, Object>> personalizations = (List<Map<String, Object>>) capturedPayload.get("personalizations");
        List<Map<String, String>> toList = (List<Map<String, String>>) personalizations.get(0).get("to");
        assertEquals("arshaddpvt@gmail.com", toList.get(0).get("email"));

        List<Map<String, String>> contentList = (List<Map<String, String>>) capturedPayload.get("content");
        String textBody = contentList.get(0).get("value");
        assertTrue(textBody.contains("Dear Arshad"));
        assertTrue(textBody.contains("Health Insurance Policy"));
    }

    @Test
    void sendEmailExceptionHandledGracefully() {
        // Arrange
        OTPVerificationDTO dto = new OTPVerificationDTO();
        dto.setEmail("arshadhm200@gmail.com");
        dto.setOtp("1234");

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        
        // Force the execution step to crash with a runtime network exception
        doThrow(new RuntimeException("SendGrid Server Unreachable")).when(requestBodySpec).header(anyString(), anyString());

        // Act & Assert
        // Since we wrap our execution block inside a try-catch block internally, 
        // the application handles it gracefully without breaking the runtime background thread loop.
        emailService.sendOtp(dto);
        
        verify(requestBodySpec, never()).retrieve();
    }
}