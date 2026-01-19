package com.nightflow.venueservice.dto;

import com.nightflow.venueservice.entity.VenueType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VenueRequest {

    @NotBlank(message = "Mekan adı zorunludur")
    private String name;

    private String address;

    @NotBlank(message = "Şehir zorunludur")
    private String city;

    private String district;

    @Min(value = 1, message = "Kapasite en az 1 olmalıdır")
    private Integer capacity;

    private String mapUrl;

    private String imageUrl;

    private VenueType type;
}
