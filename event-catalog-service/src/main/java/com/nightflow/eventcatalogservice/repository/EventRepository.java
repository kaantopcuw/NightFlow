package com.nightflow.eventcatalogservice.repository;

import com.nightflow.eventcatalogservice.document.Event;
import com.nightflow.eventcatalogservice.document.EventCategory;
import com.nightflow.eventcatalogservice.document.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends MongoRepository<Event, String> {

    Optional<Event> findBySlug(String slug);

    List<Event> findByCategory(EventCategory category);

    List<Event> findByStatus(EventStatus status);

    List<Event> findByFeatured(Boolean featured);

    @Query("{ 'eventDate': { $gte: ?0 }, 'status': 'PUBLISHED' }")
    List<Event> findUpcomingEvents(LocalDateTime now);

    @Query("{ 'venueCity': ?0, 'status': 'PUBLISHED' }")
    List<Event> findByCity(String city);

    @Query("{ 'category': ?0, 'status': 'PUBLISHED', 'eventDate': { $gte: ?1 } }")
    List<Event> findByCategoryAndUpcoming(EventCategory category, LocalDateTime now);

    @Query("{ 'eventDate': { $gte: ?0, $lte: ?1 }, 'status': 'PUBLISHED' }")
    List<Event> findByDateRange(LocalDateTime start, LocalDateTime end);

    Page<Event> findByStatusOrderByEventDateAsc(EventStatus status, Pageable pageable);

    boolean existsBySlug(String slug);
}
