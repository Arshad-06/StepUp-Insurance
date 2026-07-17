package com.infy.newgen.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.infy.newgen.exception.NewGenException;

class OtpServiceTest {

    private OtpService otpService;

    @BeforeEach

    void setUp() {

        otpService = new OtpServiceImpl(); // To Clear HashMap Before Each Test

    }

    @Test

    void testGenerateAndVerifyOtpSuccess() throws NewGenException {

        String email = "arshadhm200@gmail.com";

        String otp = otpService.generateOtp(email);

        Assertions.assertNotNull(otp);

        Assertions.assertEquals(4, otp.length());

        Boolean isVerified = otpService.verifyOtp(email, otp);

        Assertions.assertTrue(isVerified);

    }

    @Test

    void testVerifyOtpNotFound() throws NewGenException {

        NewGenException e = Assertions.assertThrows(NewGenException.class,
                () -> otpService.verifyOtp("notfound@gmail.com", "1111"));

        Assertions.assertEquals("Service.OTP_NOT_FOUND", e.getMessage());

    }

    @Test

    void testVerifyOtpInvalid() throws NewGenException {

        String email = "arshadhm200@gmail.com";

        otpService.generateOtp(email);

        NewGenException e = Assertions.assertThrows(NewGenException.class,
                () -> otpService.verifyOtp("arshadhm200@gmail.com", "0000"));

        Assertions.assertEquals("Service.OTP_INVALID", e.getMessage());

    }

}