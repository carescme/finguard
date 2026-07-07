package com.finguard.core.dto;

import lombok.Data;

@Data
public class RegisterRequestDTO {
    private String email;
    private String password;
}