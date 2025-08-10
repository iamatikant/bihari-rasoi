package com.biharirasoi.store.controller;

import com.biharirasoi.store.dto.JwtResponseDto;
import com.biharirasoi.store.dto.LoginReqDto;
import com.biharirasoi.store.service.OtpService;
import com.biharirasoi.store.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.biharirasoi.store.exception.OtpGenerationException;
import com.biharirasoi.store.exception.OtpValidationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/login")
public class LoginController {

    @Autowired
    OtpService otpService;

    @Autowired
    JwtUtil jwtUtil;

    @PostMapping("/send-otp")
    public ResponseEntity<String> generateOtp(@RequestBody String email) {
        try {
            otpService.generateOtp(email);
            return ResponseEntity.ok("OTP sent to " + email);
        } catch (OtpGenerationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/validate-otp")
    public ResponseEntity<?> validateOtp(@RequestBody LoginReqDto request) {
        try {
            if (otpService.validateOtp(request)) {
                String jwtToken = jwtUtil.generateToken(request.getEmail());
                JwtResponseDto jwtResponse = new JwtResponseDto(jwtToken);
                return ResponseEntity.ok(jwtResponse);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid or expired OTP");
            }
        } catch (OtpValidationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

}
