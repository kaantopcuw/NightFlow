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

import java.util.List;
import java.util.Locale;

@Service
public class AuthenticationService {

    /**
     * Roles a caller is allowed to request through the public self-service
     * registration endpoint.
     *
     * Everything else in the system - notably SYSTEM, which is the identity the
     * services grant themselves for internal-only endpoints such as
     * GET /tickets/event/{id}/all, and ADMIN/GATEKEEPER, which are operator
     * roles - must be assigned out of band, never by the registering client.
     */
    private static final List<String> SELF_SERVICE_ROLES = List.of("USER", "ORGANIZER");

    private static final String DEFAULT_ROLE = "USER";

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
                .role(resolveSelfServiceRole(request.getRole()))
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
     * Validates the role a client asked for during self-service registration.
     *
     * The request body is attacker-controlled, so the requested role is checked
     * against an allow-list instead of being trusted. Requests for a privileged
     * role are rejected with 400 rather than silently downgraded to USER: a
     * silent downgrade would hand back a 201 plus a token that does not carry
     * the requested authority, which is indistinguishable from success for the
     * caller and invisible in the logs. An explicit rejection keeps the security
     * boundary part of the API contract and makes escalation attempts obvious.
     *
     * @param requestedRole role from the registration request, may be null
     * @return the role to persist
     * @throws IllegalArgumentException if a non self-service role was requested
     */
    private String resolveSelfServiceRole(String requestedRole) {
        if (requestedRole == null || requestedRole.isBlank()) {
            return DEFAULT_ROLE;
        }

        String normalized = requestedRole.trim().toUpperCase(Locale.ROOT);
        if (!SELF_SERVICE_ROLES.contains(normalized)) {
            throw new IllegalArgumentException(
                    "Role '" + requestedRole + "' cannot be requested during registration. "
                            + "Allowed roles: " + String.join(", ", SELF_SERVICE_ROLES) + ".");
        }
        return normalized;
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

