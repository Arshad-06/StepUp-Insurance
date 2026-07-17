package com.infy.newgen.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.infy.newgen.dto.OTPVerificationDTO;
import com.infy.newgen.dto.PaymentReminderDTO;

@Service(value = "emailService")
@Transactional
public class EmailServiceImpl implements EmailService {
    @Autowired
    private JavaMailSender mailSender;

    @Override
    @Async
    public void sendOtp(OTPVerificationDTO dto) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(dto.getEmail());
        message.setSubject("StepUp Insurance OTP Verification");
        message.setText(
                """
                        Dear User,

                        Your OTP For Login Is : %s

                        This OTP Is Valid For One-Time Verification Only.

                        Regards,
                        StepUp Insurance Team
                        """
                        .formatted(dto.getOtp()));
        mailSender.send(message);
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
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("StepUp Insurance Payment Reminder");
        message.setText(
                """
                        Dear %s,

                        Your %s Insurance Policy With Policy ID : %s Has Lapsed.

                        Kindly Renew At Your Convenience.

                        Regards,
                        StepUp Insurance Team
                        """
                        .formatted(
                                name,
                                formattedPolicyType,
                                policyId));
        mailSender.send(message);
    }
}