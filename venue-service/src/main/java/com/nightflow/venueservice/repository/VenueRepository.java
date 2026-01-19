package com.nightflow.venueservice.repository;

import com.nightflow.venueservice.entity.Venue;
import com.nightflow.venueservice.entity.VenueType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VenueRepository extends JpaRepository<Venue, Long> {

    List<Venue> findByCity(String city);

    List<Venue> findByCityIgnoreCase(String city);

    List<Venue> findByType(VenueType type);

    List<Venue> findByCityAndType(String city, VenueType type);

    List<Venue> findByCapacityGreaterThanEqual(Integer minCapacity);
}
