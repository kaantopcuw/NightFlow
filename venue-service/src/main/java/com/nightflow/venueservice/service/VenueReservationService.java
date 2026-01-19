package com.nightflow.venueservice.service;

import com.nightflow.venueservice.dto.VenueReservationRequest;
import com.nightflow.venueservice.dto.VenueReservationResponse;
import com.nightflow.venueservice.entity.*;
import com.nightflow.venueservice.exception.ResourceAlreadyExistsException;
import com.nightflow.venueservice.exception.ResourceNotFoundException;
import com.nightflow.venueservice.repository.OrganizerRepository;
import com.nightflow.venueservice.repository.VenueRepository;
import com.nightflow.venueservice.repository.VenueReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class VenueReservationService {

    private final VenueReservationRepository reservationRepository;
    private final VenueRepository venueRepository;
    private final OrganizerRepository organizerRepository;

    public VenueReservationResponse create(VenueReservationRequest request) {
        Venue venue = venueRepository.findById(request.getVenueId())
                .orElseThrow(() -> new ResourceNotFoundException("Mekan", request.getVenueId()));

        Organizer organizer = organizerRepository.findById(request.getOrganizerId())
                .orElseThrow(() -> new ResourceNotFoundException("Organizatör", request.getOrganizerId()));

        List<VenueReservation> existingReservations = reservationRepository
                .findActiveReservationsByVenueAndDate(request.getVenueId(), request.getEventDate());

        if (!existingReservations.isEmpty()) {
            throw new ResourceAlreadyExistsException(
                    "Bu mekan bu tarihte zaten rezerve edilmiş: " + request.getEventDate());
        }

        VenueReservation reservation = VenueReservation.builder()
                .venue(venue)
                .organizer(organizer)
                .eventDate(request.getEventDate())
                .status(ReservationStatus.PENDING)
                .build();

        VenueReservation saved = reservationRepository.save(reservation);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<VenueReservationResponse> findByOrganizerId(Long organizerId) {
        return reservationRepository.findByOrganizerId(organizerId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VenueReservationResponse> findUpcomingByOrganizerId(Long organizerId) {
        return reservationRepository.findUpcomingReservationsByOrganizer(organizerId, LocalDateTime.now())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VenueReservationResponse> findByVenueId(Long venueId) {
        return reservationRepository.findByVenueId(venueId).stream()
                .map(this::toResponse)
                .toList();
    }

    public VenueReservationResponse confirm(Long reservationId) {
        VenueReservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Rezervasyon", reservationId));

        reservation.setStatus(ReservationStatus.CONFIRMED);
        VenueReservation updated = reservationRepository.save(reservation);
        return toResponse(updated);
    }

    public VenueReservationResponse cancel(Long reservationId) {
        VenueReservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Rezervasyon", reservationId));

        reservation.setStatus(ReservationStatus.CANCELLED);
        VenueReservation updated = reservationRepository.save(reservation);
        return toResponse(updated);
    }

    private VenueReservationResponse toResponse(VenueReservation reservation) {
        return VenueReservationResponse.builder()
                .id(reservation.getId())
                .venueId(reservation.getVenue().getId())
                .venueName(reservation.getVenue().getName())
                .organizerId(reservation.getOrganizer().getId())
                .organizerName(reservation.getOrganizer().getName())
                .eventDate(reservation.getEventDate())
                .reservedAt(reservation.getReservedAt())
                .status(reservation.getStatus())
                .build();
    }
}
