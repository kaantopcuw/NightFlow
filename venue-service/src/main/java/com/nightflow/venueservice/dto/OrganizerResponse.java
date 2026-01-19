package com.nightflow.venueservice.dto;

import com.nightflow.venueservice.entity.OrganizerStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizerResponse {

    private Long id;
    private String name;
    private String slug;
    private String email;
    private String phone;
    private String logoUrl;
    private OrganizerStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
