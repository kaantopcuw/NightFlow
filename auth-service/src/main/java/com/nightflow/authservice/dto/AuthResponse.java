package com.nightflow.authservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {

    private String token;
    private String username;
    private Long id;
    private String role;
}
