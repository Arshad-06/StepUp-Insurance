package com.infy.newgen.service;

public interface OtpService {

    String generateOtp(String email);

    Boolean verifyOtp(String email, String otp);

}