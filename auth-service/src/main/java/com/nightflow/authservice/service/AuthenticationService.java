package com.nightflow.authservice.service;

import com.nightflow.authservice.dto.AuthResponse;
import com.nightflow.authservice.dto.LoginRequest;
import com.nightflow.authservice.dto.RegisterRequest;
import com.nightflow.authservice.entity.User;
import com.nightflow.authservice.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Yeni kullanıcı kaydı
     * @param request Kayıt bilgileri
     * @return JWT token ile birlikte kullanıcı bilgileri
     */
    public AuthResponse registerUser(RegisterRequest request) {
        // Email kontrolü
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Bu email zaten kullanılıyor.");
        }

        // Yeni kullanıcı oluştur
        User newUser = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // Şifreyi hashle
                .role(request.getRole() != null ? request.getRole() : "USER") // Varsayılan rol
                .build();

        // Veritabanına kaydet
        User savedUser = userRepository.save(newUser);

        // JWT token üret
        String token = jwtService.generateToken(savedUser.getUsername());

        // Response oluştur
        return AuthResponse.builder()
                .token(token)
                .username(savedUser.getUsername())
                .id(savedUser.getId())
                .role(savedUser.getRole())
                .build();
    }

    /**
     * Kullanıcı girişi
     * @param request Giriş bilgileri (email ve şifre)
     * @return JWT token ile birlikte kullanıcı bilgileri
     */
    public AuthResponse loginUser(LoginRequest request) {
        // Spring Security ile kimlik doğrulama
        // Email ile giriş yapıyoruz (CustomUserDetailsService'de email kullanıyoruz)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Kullanıcıyı veritabanından çek
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı."));

        // JWT token üret
        String token = jwtService.generateToken(user.getUsername());

        // Response oluştur
        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .id(user.getId())
                .role(user.getRole())
                .build();
    }

    /**
     * Token doğrulama ve kullanıcı bilgisi alma
     * @param token JWT Token
     * @return Kullanıcı bilgileri
     */
    public AuthResponse validateToken(String token) {
        // 1. Token'dan username al
        String username = jwtService.extractUsername(token);

        // 2. Kullanıcıyı bul
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı."));

        // 3. Token geçerli mi?
        if (!jwtService.validateToken(token)) {
             throw new RuntimeException("Geçersiz veya süresi dolmuş token.");
        }

        // 4. Bilgileri dön
        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .id(user.getId())
                .role(user.getRole())
                .build();
    }
}

