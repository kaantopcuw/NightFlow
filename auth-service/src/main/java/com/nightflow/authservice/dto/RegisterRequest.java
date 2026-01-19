package com.nightflow.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Kullanıcı adı zorunlu")
    @Size(min = 3, max = 50, message = "Kullanıcı adı 3-50 karakter arasında olmalı")
    private String username;

    @NotBlank(message = "Email zorunlu")
    @Email(message = "Geçerli bir email adresi giriniz")
    private String email;

    @NotBlank(message = "Şifre zorunlu")
    @Size(min = 4, message = "Şifre en az 4 karakter olmalı")
    private String password;


}
