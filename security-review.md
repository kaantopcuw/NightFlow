# 🔒 NightFlow Security Review - Authentication & Authorization Refactor

**Tarih:** 2026-01-20  
**İncelenen Commit:** Uncommitted changes (Security & Ownership Refactor)  
**İnceleme Yapan:** Security Review  

---

## 📋 Executive Summary

Yapılan geliştirme, merkezi kimlik doğrulama (centralized authentication) ve rol tabanlı erişim kontrolü (RBAC) implementasyonunu içeriyor. Mimari olarak **doğru yönde** ancak **3 kritik güvenlik açığı** production'a geçmeden önce mutlaka düzeltilmelidir.

### Risk Özeti
- 🔴 **3 Kritik Açık** (P0 - Hemen düzeltilmeli)
- 🟡 **3 Yüksek Öncelikli** (P1 - Bu sprint'te düzeltilmeli)
- 🟢 **2 Orta Öncelikli** (P2 - Gelecek sprint)

---

## ✅ İyi Yapılan Noktalar

### 1. Merkezi Kimlik Doğrulama (Gateway AuthFilter)
- ✅ Gateway'de tüm istekler için token validasyonu yapılıyor
- ✅ Auth-service'den kullanıcı bilgileri alınıp header'lara ekleniyor (`X-User-Id`, `X-User-Role`)
- ✅ Token geçersizse 401 dönülüyor

### 2. Rol Tabanlı Erişim Kontrolü (RBAC)
- ✅ `@PreAuthorize` annotation'ları ile method-level security
- ✅ ORGANIZER rolü event ve ticket oluşturma için zorunlu
- ✅ GATEKEEPER rolü check-in işlemleri için zorunlu

### 3. Ownership Kontrolü
- ✅ Event'lerde organizerId kontrolü yapılıyor
- ✅ Ticket kategorilerinde event ownership doğrulaması var
- ✅ Order'larda userId ile ownership kontrolü (kısmen)

### 4. E2E Test Modernizasyonu
- ✅ SQL seed'lere bağımlılık kaldırıldı
- ✅ Tamamen API-driven test akışı
- ✅ Gerçekçi kullanıcı senaryoları (Organizer + User flow)

---

## 🚨 KRİTİK GÜVENLİK AÇIKLARI (P0)

### 1. IDOR (Insecure Direct Object Reference) - KRİTİK

**Dosya:** `order-service/src/main/java/com/nightflow/orderservice/controller/OrderController.java`

#### ❌ Mevcut Kod:
```java
@GetMapping("/{orderNumber}")
public ResponseEntity<Order> getOrder(@PathVariable String orderNumber) {
    // IDOR check could be added here
    return ResponseEntity.ok(orderService.getOrder(orderNumber));
}
```

#### 🐛 Sorun:
Herhangi bir kullanıcı, başka kullanıcının order numarasını bilerek siparişini görebilir!

**Saldırı Senaryosu:**
```bash
# User A creates order: order-123
# User B can access User A's order:
curl -H "Authorization: Bearer <user-b-token>" \
     http://localhost:8080/api/orders/order-123
# ❌ Returns User A's order details!
```

#### ✅ Çözüm:
```java
@GetMapping("/{orderNumber}")
public ResponseEntity<Order> getOrder(
        @PathVariable String orderNumber,
        Authentication authentication) {
    String userId = (String) authentication.getPrincipal();
    Order order = orderService.getOrder(orderNumber);
    
    // Ownership check
    if (!order.getUserId().equals(userId)) {
        throw new AccessDeniedException("Bu siparişi görüntüleme yetkiniz yok");
    }
    
    return ResponseEntity.ok(order);
}
```

#### 📝 Yapılacaklar:
- [ ] `OrderController.getOrder()` metoduna ownership kontrolü ekle
- [ ] `TicketController` - Tüm GET endpoint'lerini gözden geçir
- [ ] `EventController.getById()` - Ownership kontrolü gerekli mi değerlendir
- [ ] Security test case'leri ekle (IDOR test)

---

### 2. Header Injection Attack - KRİTİK

**Dosyalar:** 
- `event-catalog-service/src/main/java/com/nightflow/eventcatalogservice/config/SecurityConfig.java`
- `ticket-service/src/main/java/com/nightflow/ticketservice/config/SecurityConfig.java`
- `order-service/src/main/java/com/nightflow/orderservice/config/SecurityConfig.java`

#### ❌ Mevcut Kod:
```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/actuator/**", "/api-docs/**").permitAll()
            .anyRequest().permitAll() // ❌ DANGEROUS!
        )
        .addFilterBefore(new HeaderAuthFilter(), UsernamePasswordAuthenticationFilter.class);
    
    return http.build();
}
```

```java
// HeaderAuthFilter.java
public class HeaderAuthFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(...) {
        String userId = request.getHeader("X-User-Id");
        String userRole = request.getHeader("X-User-Role");
        
        if (userId != null && userRole != null) {
            // ❌ Blindly trusts headers!
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userId, null, 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + userRole))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        
        filterChain.doFilter(request, response);
    }
}
```

#### 🐛 Sorun:
Eğer servisler doğrudan erişilebilir durumdaysa (port açıksa), saldırgan `X-User-Id` ve `X-User-Role` header'larını kendisi ekleyerek herhangi bir kullanıcı gibi davranabilir!

**Saldırı Senaryosu:**
```bash
# Bypass Gateway, directly call service
curl -X POST http://localhost:8092/events \
     -H "X-User-Id: any-user-id" \
     -H "X-User-Role: ORGANIZER" \
     -H "Content-Type: application/json" \
     -d '{"name": "Hacked Event", ...}'
# ❌ Event created without authentication!
```



#### ✅ Çözüm:

**Seçenek A: Network Seviyesi (Önerilen)**
- Servislerin portlarını dışarıya açma (Docker/K8s internal network)
- Sadece Gateway dışarıya açık olsun

**Seçenek B: Shared Secret (Defense in Depth)**
```java
// Gateway AuthFilter - Add internal secret
exchange.getRequest().mutate()
    .header("X-User-Id", userId)
    .header("X-User-Role", role)
    .header("X-Internal-Secret", internalSecret) // Add this
    .build();

// Backend HeaderAuthFilter - Verify secret
String internalSecret = request.getHeader("X-Internal-Secret");
if (!expectedSecret.equals(internalSecret)) {
    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    return;
}
```

#### 📝 Yapılacaklar:
- [ ] Production'da servislerin portlarını dışarıya kapatıldığından emin ol
- [ ] Defense-in-depth için internal secret mekanizması ekle
- [ ] `anyRequest().permitAll()` yerine `anyRequest().authenticated()` kullan
- [ ] Security test: Gateway bypass denemesi

---

### 3. Token Validation Bypass - KRİTİK

**Dosya:** `gateway-service/src/main/java/com/nightflow/gatewayservice/filter/AuthFilter.java`

#### ❌ Mevcut Kod:
```java
@Override
public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    String path = exchange.getRequest().getPath().value();
    
    // Skip auth for public endpoints
    if (isPublicEndpoint(path)) {
        return chain.filter(exchange);
    }
    
    String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
    
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        return chain.filter(exchange); // ❌ Continues without auth!
    }
    // ...
}
```

#### 🐛 Sorun:
Token yoksa veya geçersiz formatta ise, istek **reddedilmek yerine devam ediyor**! Bu, backend servislerin güvenliğine tamamen bağımlı hale getiriyor.

#### ✅ Çözüm:
```java
if (authHeader == null || !authHeader.startsWith("Bearer ")) {
    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
    return exchange.getResponse().setComplete();
}
```

#### 📝 Yapılacaklar:
- [ ] Token yoksa 401 döndür
- [ ] Public endpoint listesini gözden geçir
- [ ] E2E test: Token'sız istek 401 dönmeli

---

## 🟡 YÜKSEK ÖNCELİKLİ SORUNLAR (P1)

### 4. Service-to-Service Authentication Eksik

**Dosya:** `ticket-service/src/main/java/com/nightflow/ticketservice/client/EventServiceClient.java`

#### ❌ Mevcut Kod:
```java
@FeignClient(name = "event-catalog-service")
public interface EventServiceClient {
    @GetMapping("/events/{eventId}")
    EventDto getEvent(@PathVariable("eventId") String eventId);
}
```

#### 🐛 Sorun:
Servisler arası iletişimde authentication yok. Internal network'te olsa bile, zero-trust prensibi gereği servisler birbirini doğrulamalı.

#### ✅ Çözüm:
```java
@Configuration
public class FeignConfig {
    @Value("${security.internal.secret}")
    private String internalSecret;
    
    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {
            template.header("X-Internal-Secret", internalSecret);
            // Optionally add service identity
            template.header("X-Service-Name", "ticket-service");
        };
    }
}
```

#### 📝 Yapılacaklar:
- [ ] Feign client'lara internal secret header ekle
- [ ] Service account token mekanizması değerlendir
- [ ] mTLS değerlendir (uzun vadeli)

---

### 5. JWT Secret Yönetimi

**Dosya:** `auth-service/src/main/resources/application.yml`

#### ❌ Mevcut Kod:
```yaml
jwt:
  secret: ${JWT_SECRET:default-secret-key-for-development}
  expiration: 86400000 # 24 hours
```

#### 🐛 Sorunlar:
1. Default secret production'da kullanılabilir
2. 24 saat çok uzun bir token süresi
3. Secret rotation mekanizması yok

#### ✅ Çözüm:
```yaml
jwt:
  secret: ${JWT_SECRET} # No default! Fail if not set
  expiration: 3600000 # 1 hour
  refresh-expiration: 604800000 # 7 days (for refresh token)
```

#### 📝 Yapılacaklar:
- [ ] Default secret'ı kaldır
- [ ] Token süresini 1 saate düşür
- [ ] Refresh token mekanizması ekle
- [ ] Secret rotation stratejisi belirle

---

### 6. Rate Limiting Eksik

**Dosya:** `gateway-service`

#### 🐛 Sorun:
Auth endpoint'lerinde rate limiting yok. Brute-force saldırılarına açık.

#### ✅ Çözüm:
```yaml
# application.yml
spring:
  cloud:
    gateway:
      routes:
        - id: auth-service
          uri: lb://auth-service
          predicates:
            - Path=/api/auth/**
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 10
                redis-rate-limiter.burstCapacity: 20
                key-resolver: "#{@ipKeyResolver}"
```

#### 📝 Yapılacaklar:
- [ ] Redis entegrasyonu ekle
- [ ] Auth endpoint'lerine rate limiting uygula
- [ ] Login attempt tracking ekle
- [ ] Account lockout mekanizması değerlendir

---

## 🟢 ORTA ÖNCELİKLİ SORUNLAR (P2)

### 7. Audit Logging Eksik

#### 🐛 Sorun:
Güvenlik olayları (login, logout, yetki hataları) loglanmıyor.

#### ✅ Çözüm:
```java
@Aspect
@Component
public class SecurityAuditAspect {
    private static final Logger auditLog = LoggerFactory.getLogger("SECURITY_AUDIT");
    
    @AfterThrowing(pointcut = "@annotation(org.springframework.security.access.prepost.PreAuthorize)", 
                   throwing = "ex")
    public void logAccessDenied(JoinPoint jp, AccessDeniedException ex) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        auditLog.warn("ACCESS_DENIED | user={} | method={} | reason={}", 
            auth.getPrincipal(), jp.getSignature().getName(), ex.getMessage());
    }
}
```

#### 📝 Yapılacaklar:
- [ ] Security audit aspect ekle
- [ ] Login/logout olaylarını logla
- [ ] Failed login attempt'leri logla
- [ ] Log aggregation (ELK/Loki) entegrasyonu

---

### 8. Input Validation Eksiklikleri

**Dosya:** `auth-service/src/main/java/com/nightflow/authservice/controller/AuthController.java`

#### ❌ Mevcut Kod:
```java
@PostMapping("/validate")
public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
    String token = authHeader.replace("Bearer ", "");
    // No validation on token format/length
}
```

#### 🐛 Sorun:
Token formatı ve uzunluğu kontrol edilmiyor. Çok uzun veya malformed token'lar DoS'a yol açabilir.

#### ✅ Çözüm:
```java
@PostMapping("/validate")
public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
    if (authHeader == null || authHeader.length() > 2000) {
        return ResponseEntity.badRequest().body("Invalid token format");
    }
    
    if (!authHeader.startsWith("Bearer ")) {
        return ResponseEntity.badRequest().body("Token must start with 'Bearer '");
    }
    
    String token = authHeader.substring(7);
    if (token.isEmpty() || token.length() < 10) {
        return ResponseEntity.badRequest().body("Invalid token");
    }
    // ...
}
```

#### 📝 Yapılacaklar:
- [ ] Token format/length validation ekle
- [ ] Request body size limitleri koy
- [ ] Global exception handler'da sensitive bilgi sızıntısını önle

---

## 📊 Test Coverage Analizi

### Mevcut Security Test Coverage

| Alan | Durum | Notlar |
|------|-------|--------|
| Authentication Flow | ✅ | E2E testlerde var |
| RBAC (Role checks) | ✅ | E2E testlerde var |
| IDOR | ❌ | Test yok! |
| Header Injection | ❌ | Test yok! |
| Token Bypass | ❌ | Test yok! |
| Rate Limiting | ❌ | Özellik yok |

### Önerilen Security Test Cases

```java
@Test
void shouldRejectAccessToOtherUsersOrder() {
    // Given: User A creates an order
    String userAToken = login("userA@test.com", "password");
    String orderNumber = createOrder(userAToken);
    
    // When: User B tries to access User A's order
    String userBToken = login("userB@test.com", "password");
    
    // Then: Should return 403
    given()
        .header("Authorization", "Bearer " + userBToken)
        .get("/api/orders/" + orderNumber)
        .then()
        .statusCode(403);
}

@Test
void shouldRejectDirectServiceCallWithFakeHeaders() {
    // Given: Direct call to service (bypassing gateway)
    // When: Sending fake X-User-Role header
    given()
        .header("X-User-Id", "fake-user")
        .header("X-User-Role", "ORGANIZER")
        .post("http://localhost:8092/events")
        .then()
        .statusCode(403); // Should reject!
}

@Test
void shouldRejectRequestWithoutToken() {
    // When: Calling protected endpoint without token
    given()
        .get("/api/orders/my-orders")
        .then()
        .statusCode(401);
}
```

---

## 🎯 Aksiyon Planı

### Sprint 1 (Bu Hafta) - P0 Kritik
1. [ ] IDOR fix - OrderController ownership check
2. [ ] Header Injection fix - Internal secret mechanism
3. [ ] Token Bypass fix - Reject requests without valid token
4. [ ] Security test cases ekle

### Sprint 2 (Gelecek Hafta) - P1 Yüksek
5. [ ] Service-to-service auth
6. [ ] JWT secret management
7. [ ] Rate limiting

### Sprint 3 - P2 Orta
8. [ ] Audit logging
9. [ ] Input validation improvements
10. [ ] Security monitoring dashboard

---

## 📝 Sonuç

Yapılan authentication/authorization refactor'ı **doğru mimari kararlar** içeriyor ancak **3 kritik güvenlik açığı** production'a geçmeden önce mutlaka düzeltilmelidir:

1. **IDOR** - Başka kullanıcının verilerine erişim
2. **Header Injection** - Gateway bypass ile yetki yükseltme
3. **Token Bypass** - Token'sız isteklerin geçmesi

Bu açıklar düzeltilmeden **production'a deploy edilmemeli**!

---

*Bu doküman security review sürecinin bir parçasıdır. Sorularınız için security ekibiyle iletişime geçin.*
