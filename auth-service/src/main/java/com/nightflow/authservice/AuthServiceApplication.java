package com.nightflow.authservice;

import com.nightflow.authservice.entity.User;
import com.nightflow.authservice.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableDiscoveryClient
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }

    @Bean
    CommandLineRunner commandLineRunner(UserRepository repository, PasswordEncoder passwordEncoder) { // PasswordEncoder eklendi
        return args -> {
            if (repository.count() == 0) {
                User testUser = User.builder()
                        .username("nightwalker")
                        .email("test@nightflow.com")
                        // ŞİFREYİ ARTIK HASHLEYEREK KAYDEDİYORUZ
                        .password(passwordEncoder.encode("sifre123"))
                        .role("USER")
                        .build();

                repository.save(testUser);
                System.out.println("✅ TEST KULLANICISI (HASH'Lİ) OLUŞTURULDU!");
            }
        };
    }
}
