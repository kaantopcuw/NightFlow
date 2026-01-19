package com.nightflow.shoppingcartservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long categoryId;
    private String categoryName;
    private String eventId;
    private String eventName;
    private BigDecimal price;
    private Integer quantity;
    
    // Rezervasyon bilgisi (Ticket Service için unique session id)
    private String reservationId;
    private Long remainingTime; // Saniye cinsinden kalan süre
}
