package com.biharirasoi.store.dto;

import lombok.Data;

@Data
public class LoginReqDto {
    private String email;
    private String otp;
}
