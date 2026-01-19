package com.nightflow.venueservice.repository;

import com.nightflow.venueservice.entity.Organizer;
import com.nightflow.venueservice.entity.OrganizerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizerRepository extends JpaRepository<Organizer, Long> {

    Optional<Organizer> findBySlug(String slug);

    Optional<Organizer> findByEmail(String email);

    List<Organizer> findByStatus(OrganizerStatus status);

    boolean existsBySlug(String slug);

    boolean existsByEmail(String email);
}
