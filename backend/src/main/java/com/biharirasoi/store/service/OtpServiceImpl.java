package com.biharirasoi.store.service;

import com.biharirasoi.store.dto.LoginReqDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import com.biharirasoi.store.exception.OtpGenerationException;
import com.biharirasoi.store.exception.OtpValidationException;

@Service
public class OtpServiceImpl implements OtpService{
    private static final Logger logger = LoggerFactory.getLogger(OtpServiceImpl.class);

    private final JavaMailSender mailSender;
    private final Map<String, String> otpCache = new ConcurrentHashMap<>();
    private final Map<String, Long> otpExpiry = new ConcurrentHashMap<>();
    private final Random random = new Random();
    private static final long EXPIRY_DURATION = 5 * 60 * 1000; // 5 mins

    public OtpServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void generateOtp(String email) {
        if (email == null || email.trim().isEmpty()) {
            logger.warn("Cannot generate OTP: email is null or empty");
            throw new OtpGenerationException("Email is null or empty");
        }
        try {
            String otp = String.format("%06d", random.nextInt(999999));
            otpCache.put(email, otp);
            otpExpiry.put(email, System.currentTimeMillis() + EXPIRY_DURATION);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Your Login OTP");
            message.setText("Your OTP is: " + otp + "\nThis will expire in 5 minutes.");
            mailSender.send(message);

            logger.info("OTP generated and sent to email: {}", email);
        } catch (Exception e) {
            logger.error("Exception while generating OTP for email: {}", email, e);
            throw new OtpGenerationException("Failed to generate OTP", e);
        }
    }

    public boolean validateOtp(LoginReqDto request) {
        if (request.getEmail() == null || request.getOtp() == null) {
            logger.warn("Cannot validate OTP: email or otp is null");
            throw new OtpValidationException("Email or OTP is null");
        }
        String email = request.getEmail();
        String otp = request.getOtp();
        try {
            String cachedOtp = otpCache.get(email);
            Long expiry = otpExpiry.get(email);

            if (cachedOtp == null || expiry == null) {
                throw new OtpValidationException("No OTP found for email");
            }
            if (System.currentTimeMillis() > expiry) {
                otpCache.remove(email);
                otpExpiry.remove(email);
                throw new OtpValidationException("OTP expired");
            }
            if (!cachedOtp.equals(otp)) {
                throw new OtpValidationException("Invalid OTP");
            }
            otpCache.remove(email);
            otpExpiry.remove(email);
            return true;
        } catch (OtpValidationException e) {
            logger.error("OTP validation failed for email: {}", email, e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during OTP validation for email: {}", email, e);
            throw new OtpValidationException("Unexpected error during OTP validation", e);
        }
    }
}
