package com.nightflow.venueservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizerRequest {

    @NotBlank(message = "Organizatör adı zorunludur")
    private String name;

    @NotBlank(message = "Slug zorunludur")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug sadece küçük harf, rakam ve tire içerebilir")
    private String slug;

    @Email(message = "Geçerli bir email adresi giriniz")
    private String email;

    private String phone;

    private String logoUrl;
}
