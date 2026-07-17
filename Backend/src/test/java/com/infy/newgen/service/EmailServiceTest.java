package com.infy.newgen.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.infy.newgen.dto.OTPVerificationDTO;
import com.infy.newgen.dto.PaymentReminderDTO;
import com.infy.newgen.enums.PolicyType;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {
    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailServiceImpl emailService;

    @Test
    void sendOtpSuccess() {
        OTPVerificationDTO dto = new OTPVerificationDTO();
        dto.setEmail("arshadhm200@gmail.com");
        dto.setOtp("1234");

        emailService.sendOtp(dto);

        // Check Text Formatting Of Mail Message
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        verify(mailSender, times(1)).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();

        Assertions.assertNotNull(sentMessage);
        Assertions.assertEquals("arshadhm200@gmail.com", sentMessage.getTo()[0]);
        Assertions.assertEquals("StepUp Insurance OTP Verification", sentMessage.getSubject());
        Assertions.assertTrue(sentMessage.getText().contains("Your OTP For Login Is : 1234"));
    }

    @Test
    void sendOtpMailException() {
        OTPVerificationDTO dto = new OTPVerificationDTO();
        dto.setEmail("arshadhm200@gmail.com");
        dto.setOtp("1234");

        doThrow(new MailSendException("General.MAIL_EXCEPTION_MESSAGE")).when(mailSender)
                .send(any(SimpleMailMessage.class));

        MailException e = Assertions.assertThrows(MailException.class, () -> emailService.sendOtp(dto));
        Assertions.assertEquals("General.MAIL_EXCEPTION_MESSAGE", e.getMessage());
    }

    @Test
    void sendPaymentReminderSuccess() {
        PaymentReminderDTO dto = new PaymentReminderDTO();
        dto.setCustomerEmail("arshaddpvt@gmail.com");
        dto.setCustomerName("Arshad");
        dto.setPolicyId("POL-123");
        dto.setPolicyType(PolicyType.HEALTH_INSURANCE);

        emailService.sendPaymentReminder(dto);

        // Check Text Formatting Of Mail Message
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        verify(mailSender, times(1)).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();

        Assertions.assertNotNull(sentMessage);
        Assertions.assertEquals("arshaddpvt@gmail.com", sentMessage.getTo()[0]);
        Assertions.assertEquals("StepUp Insurance Payment Reminder", sentMessage.getSubject());
        Assertions.assertTrue(sentMessage.getText().contains("Dear Arshad"));
    }

    @Test
    void sendPaymentReminderMailException() {
        PaymentReminderDTO dto = new PaymentReminderDTO();
        dto.setCustomerEmail("arshaddpvt@gmail.com");
        dto.setCustomerName("Arshad");
        dto.setPolicyId("POL-123");
        dto.setPolicyType(PolicyType.HEALTH_INSURANCE);

        emailService.sendPaymentReminder(dto);

        doThrow(new MailSendException("General.MAIL_EXCEPTION_MESSAGE")).when(mailSender)
                .send(any(SimpleMailMessage.class));

        MailException e = Assertions.assertThrows(MailException.class, () -> emailService.sendPaymentReminder(dto));
        Assertions.assertEquals("General.MAIL_EXCEPTION_MESSAGE", e.getMessage());
    }
}