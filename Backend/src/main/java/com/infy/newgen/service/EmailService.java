package com.infy.newgen.service;

import com.infy.newgen.dto.OTPVerificationDTO;
import com.infy.newgen.dto.PaymentReminderDTO;

public interface EmailService {
    void sendOtp(OTPVerificationDTO dto);

    void sendPaymentReminder(PaymentReminderDTO dto);
}