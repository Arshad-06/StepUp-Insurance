package com.infy.newgen.service;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.infy.newgen.exception.NewGenException;

@Service(value = "otpService")
@Transactional
public class OtpServiceImpl implements OtpService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private final Map<String, String> otpStore = new ConcurrentHashMap<>();

    @Override
    public String generateOtp(String email) {
        String otp = String.format("%04d", RANDOM.nextInt(10000));
        otpStore.put(email, otp);
        return otp;
    }

    @Override
    public Boolean verifyOtp(String email, String otp) {
        String storedOtp = otpStore.get(email);
        if (storedOtp == null) {
            throw new NewGenException("Service.OTP_NOT_FOUND");
        }
        if (!storedOtp.equals(otp)) {
            throw new NewGenException("Service.OTP_INVALID");
        }
        otpStore.remove(email);
        return true;
    }
}