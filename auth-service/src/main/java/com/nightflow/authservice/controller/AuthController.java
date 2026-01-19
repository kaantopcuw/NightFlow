package com.nightflow.authservice.controller;

import com.nightflow.authservice.dto.AuthResponse;
import com.nightflow.authservice.dto.LoginRequest;
import com.nightflow.authservice.dto.RegisterRequest;
import com.nightflow.authservice.service.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * Kullanıcı kayıt endpoint'i
     * POST /auth/register
     * @param request Kayıt bilgileri (username, email, password)
     * @return JWT token ile birlikte kullanıcı bilgileri
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authenticationService.registerUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Kullanıcı giriş endpoint'i
     * POST /auth/login
     * @param request Giriş bilgileri (email, password)
     * @return JWT token ile birlikte kullanıcı bilgileri
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authenticationService.loginUser(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Email veya şifre hatalı."));
        }
    }

    /**
     * Test endpoint'i - Kimlik doğrulama gerektiren
     * GET /auth/test
     * @return Basit bir mesaj
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Auth Service çalışıyor! 🚀");
    }

    /**
     * Hata mesajı için basit bir wrapper sınıf
     */
    private record ErrorResponse(String message) {
    }
}

