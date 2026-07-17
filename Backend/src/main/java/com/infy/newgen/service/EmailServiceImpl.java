package com.infy.newgen.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import com.infy.newgen.dto.OTPVerificationDTO;
import com.infy.newgen.dto.PaymentReminderDTO;

@Service(value = "emailService")
@Transactional
public class EmailServiceImpl implements EmailService {

    @Value("${MAIL_PASSWORD}") // Maps your SendGrid API key secret
    private String sendGridApiKey;

    @Value("${MAIL_USER}") // Maps your verified SendGrid sender email
    private String fromEmail;

    // Create a single shared instance of RestClient pointing to SendGrid
    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://api.sendgrid.com/v3")
            .build();

    @Override
    @Async
    public void sendOtp(OTPVerificationDTO dto) {
        String emailContent = """
                Dear User,

                Your OTP For Login Is : %s

                This OTP Is Valid For One-Time Verification Only.

                Regards,
                StepUp Insurance Team
                """
                .formatted(dto.getOtp());

        sendEmailViaHttp(dto.getEmail(), "StepUp Insurance OTP Verification", emailContent);
    }

    @Override
    @Async
    public void sendPaymentReminder(PaymentReminderDTO dto) {
        String name = dto.getCustomerName();
        String email = dto.getCustomerEmail();
        String policyType = dto.getPolicyType().toString();
        String policyId = dto.getPolicyId();
        
        String formattedPolicyType = policyType.substring(0, 1)
                + policyType.substring(1, policyType.length() - 10).toLowerCase();

        String emailContent = """
                Dear %s,

                Your %s Insurance Policy With Policy ID : %s Has Lapsed.

                Kindly Renew At Your Convenience.

                Regards,
                StepUp Insurance Team
                """
                .formatted(name, formattedPolicyType, policyId);

        sendEmailViaHttp(email, "StepUp Insurance Payment Reminder", emailContent);
    }

    /**
     * Helper method to compile the SendGrid Payload and execute the HTTP request
     */
    private void sendEmailViaHttp(String toEmail, String subject, String contentText) {
        // Build the nested JSON structure required by SendGrid's API
        Map<String, Object> payload = Map.of(
            "personalizations", List.of(
                Map.of("to", List.of(Map.of("email", toEmail)))
            ),
            "from", Map.of("email", fromEmail, "name", "StepUp Insurance"),
            "subject", subject,
            "content", List.of(
                Map.of("type", "text/plain", "value", contentText)
            )
        );

        try {
            restClient.post()
                    .uri("/mail/send")
                    .header("Authorization", "Bearer " + sendGridApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity(); // Expecting no response body back on a 202 Success
            
            System.out.println("Email successfully delivered to " + toEmail + " via Web API!");
        } catch (Exception e) {
            System.err.println("Failed to transmit email over HTTPS payload: " + e.getMessage());
        }
    }
}