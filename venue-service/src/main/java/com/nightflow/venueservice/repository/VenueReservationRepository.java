package com.nightflow.venueservice.repository;

import com.nightflow.venueservice.entity.ReservationStatus;
import com.nightflow.venueservice.entity.VenueReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VenueReservationRepository extends JpaRepository<VenueReservation, Long> {

    List<VenueReservation> findByOrganizerId(Long organizerId);

    List<VenueReservation> findByVenueId(Long venueId);

    List<VenueReservation> findByStatus(ReservationStatus status);

    @Query("SELECT vr FROM VenueReservation vr WHERE vr.venue.id = :venueId AND vr.eventDate = :eventDate AND vr.status != 'CANCELLED'")
    List<VenueReservation> findActiveReservationsByVenueAndDate(
            @Param("venueId") Long venueId,
            @Param("eventDate") LocalDateTime eventDate
    );

    @Query("SELECT vr FROM VenueReservation vr WHERE vr.organizer.id = :organizerId AND vr.eventDate > :now ORDER BY vr.eventDate ASC")
    List<VenueReservation> findUpcomingReservationsByOrganizer(
            @Param("organizerId") Long organizerId,
            @Param("now") LocalDateTime now
    );
}
