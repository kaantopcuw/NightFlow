package com.nightflow.shoppingcartservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddToCartRequest {

    @NotBlank(message = "Session ID is required (temporary)")
    private String sessionId;

    @NotBlank(message = "Event ID is required")
    private String eventId;
    
    private String eventName;

    @NotNull(message = "Category ID is required")
    private Long categoryId;
    
    private String categoryName;

    @NotNull(message = "Price is required")
    private BigDecimal price;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Min quantity is 1")
    private Integer quantity;
}
