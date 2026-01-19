package com.nightflow.checkinservice.dto;

import java.time.LocalDateTime;

public record CheckInResponse(
    CheckInResult result,
    String ticketCode,
    String message,
    LocalDateTime checkedInAt
) {
    public static CheckInResponse success(String ticketCode) {
        return new CheckInResponse(CheckInResult.SUCCESS, ticketCode, "Check-in başarılı!", LocalDateTime.now());
    }
    
    public static CheckInResponse alreadyUsed(String ticketCode) {
        return new CheckInResponse(CheckInResult.ALREADY_USED, ticketCode, "Bu bilet zaten kullanılmış!", null);
    }
    
    public static CheckInResponse invalid(String ticketCode) {
        return new CheckInResponse(CheckInResult.INVALID_TICKET, ticketCode, "Geçersiz bilet kodu!", null);
    }
    
    public static CheckInResponse notSold(String ticketCode) {
        return new CheckInResponse(CheckInResult.NOT_SOLD, ticketCode, "Bu bilet henüz satılmamış!", null);
    }
}
