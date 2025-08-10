package com.biharirasoi.store.service;

import com.biharirasoi.store.dto.LoginReqDto;

public interface OtpService {
    void generateOtp(String email);
    boolean validateOtp(LoginReqDto request);
}
